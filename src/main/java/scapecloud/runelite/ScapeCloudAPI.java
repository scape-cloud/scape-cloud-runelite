/*
 * Copyright (c) 2021, ScapeCloud
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package scapecloud.runelite;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.Notifier;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import scapecloud.runelite.api.Authorization;
import scapecloud.runelite.api.Credentials;
import scapecloud.runelite.api.Error;
import scapecloud.runelite.api.Image;
import scapecloud.runelite.api.Link;
import scapecloud.runelite.api.Refresh;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Slf4j
@Singleton
class ScapeCloudAPI {

    private static final String FIREBASE_AUTH = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=AIzaSyD64AKzvmmEiFkn-4U5X54D24He813qCjk";
    private static final String FIREBASE_REFRESH = "https://securetoken.googleapis.com/v1/token?key=AIzaSyD64AKzvmmEiFkn-4U5X54D24He813qCjk";
    private static final String SCAPECLOUD_UPLOAD = "https://scape-cloud.tmwed.vercel.app/api/upload";

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType PNG = MediaType.parse("image/png");
    private static final Gson GSON = new Gson();

    private final Notifier notifier;
    private final OkHttpClient okHttpClient;
    private ScheduledExecutorService executor;

    private final AtomicReference<String> idToken = new AtomicReference<>();
    private final AtomicReference<String> refreshToken = new AtomicReference<>();

    @Inject
    private ScapeCloudAPI(Notifier notifier, OkHttpClient okHttpClient, ScheduledExecutorService executor) {
        this.notifier = notifier;
        this.okHttpClient = okHttpClient;
        this.executor = executor;
    }

    public boolean isAuthenticated() {
        return idToken.get() != null;
    }

    public void authenticate(String email, String password) {
        authenticate(email, password, () -> {}, error -> {});
    }

    public void authenticate(String email, String password, Runnable success, Consumer<Error> failure) {
        Request request = new Request.Builder()
                .url(FIREBASE_AUTH)
                .post(RequestBody.create(JSON, GSON.toJson(new Credentials(email, password))))
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            String json = response.body().string();
            if (response.isSuccessful()) {
                Authorization auth = GSON.fromJson(json, Authorization.class);
                idToken.set(auth.getIdToken());
                refreshToken.set(auth.getIdToken());
                executor.schedule(this::reauthenticate, auth.getExpiresIn() - 300, TimeUnit.SECONDS);
                success.run();
            } else {
                Error error = GSON.fromJson(json, Error.class);
                failure.accept(error);
            }
        } catch (IOException e) {
            log.warn("Exception occurred while authenticating with ScapeCloud.", e);
        }
    }

    public void reauthenticate() {
        RequestBody body = new FormBody.Builder()
                .add("grant_type", "refresh_token")
                .add("refresh_token", refreshToken.get())
                .build();

        Request request = new Request.Builder()
                .url(FIREBASE_REFRESH)
                .post(body)
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            String json = response.body().string();
            if (response.isSuccessful()) {
                Refresh refresh = GSON.fromJson(json, Refresh.class);
                idToken.set(refresh.getIdToken());
                refreshToken.set(refresh.getRefreshToken());
                executor.schedule(this::reauthenticate, refresh.getExpiresIn() - 300, TimeUnit.SECONDS);
            } else {
                GSON.fromJson(json, Error.class);
            }
        } catch (IOException e) {
            log.warn("Exception occurred while re-authenticating with ScapeCloud.", e);
        }
    }

    public void upload(Image image, boolean notify) {
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", image.getName(), RequestBody.create(PNG, image.getFile()))
                .build();

        Request request = new Request.Builder()
                .url(SCAPECLOUD_UPLOAD)
                .header("Authorization", "Bearer " + idToken.get())
                .post(body)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.warn("Exception occurred while uploading to ScapeCloud.", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                Link link = GSON.fromJson(body, Link.class);
                if (response.isSuccessful()) {
                    StringSelection selection = new StringSelection(link.getData());
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(selection, selection);

                    if (notify) {
                        notifier.notify("A screenshot was uploaded and inserted into your clipboard!", TrayIcon.MessageType.INFO);
                    }

                } else {
                    log.warn("ScapeCloud Upload Error: " + link.getMessage());
                }
            }
        });
    }

}