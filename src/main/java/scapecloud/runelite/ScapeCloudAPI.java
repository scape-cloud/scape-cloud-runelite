/*
 * Copyright (c) 2021, OSRSLog
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
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageBuilder;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import scapecloud.runelite.api.AuthError;
import scapecloud.runelite.api.Authorization;
import scapecloud.runelite.api.Credentials;
import scapecloud.runelite.api.Image;
import scapecloud.runelite.api.Link;
import scapecloud.runelite.api.Metadata;
import scapecloud.runelite.api.OtherPlayer;
import scapecloud.runelite.api.Refresh;
import scapecloud.runelite.api.UploadError;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Singleton
class ScapeCloudAPI {

    private static final String FIREBASE_AUTH = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=AIzaSyD64AKzvmmEiFkn-4U5X54D24He813qCjk";
    private static final String FIREBASE_REFRESH = "https://securetoken.googleapis.com/v1/token?key=AIzaSyD64AKzvmmEiFkn-4U5X54D24He813qCjk";
    private static final String OSRSLOG_UPLOAD = "https://www.osrslog.com/api/upload";

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType PNG = MediaType.parse("image/png");
    private static final Gson GSON = new Gson();

    private static final Function<Player, OtherPlayer> PLAYER_MAPPER = p -> new OtherPlayer(p.getName(), p.isFriend(), p.isFriendsChatMember(), p.getTeam(), p.getCombatLevel());

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private Notifier notifier;

    @Inject
    private OkHttpClient okHttpClient;

    @Inject
    private ScheduledExecutorService executor;

    @Inject
    private ScapeCloudPlugin plugin;

    private final AtomicReference<String> idToken = new AtomicReference<>();
    private final AtomicReference<String> refreshToken = new AtomicReference<>();
    private ScheduledFuture<?> refreshTask;

    public boolean isAuthenticated() {
        return idToken.get() != null;
    }

    public void logout() {
        idToken.set(null);
        refreshToken.set(null);
        if (refreshTask != null) {
            refreshTask.cancel(false);
            refreshTask = null;
        }
    }

    public void authenticate(String email, String password, Runnable success) {
        Request request = new Request.Builder()
                .url(FIREBASE_AUTH)
                .post(RequestBody.create(JSON, GSON.toJson(new Credentials(email, password))))
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            String json = response.body().string();
            if (response.isSuccessful()) {
                Authorization auth = GSON.fromJson(json, Authorization.class);
                idToken.set(auth.getIdToken());
                refreshToken.set(auth.getRefreshToken());
                refreshTask = executor.schedule(this::reauthenticate, auth.getExpiresIn() - 300, TimeUnit.SECONDS);
                success.run();
            } else {
                AuthError error = GSON.fromJson(json, AuthError.class);
                log.error("Error occurred while authenticating with OSRSLog. " + error.getError().getMessage());
                message("OSRSLog Auth", "Error occurred while authenticating to OSRSLog.");
            }
        } catch (IOException e) {
            log.error("Exception occurred while authenticating with OSRSLog.", e);
            message("OSRSLog Auth", "Exception occurred while authenticating to OSRSLog.");
        }
    }

    public void reauthenticate() {
        RequestBody body = new FormBody.Builder()
                .add("grant_type", "refresh_token")
                .add("refresh_token", refreshToken.get().substring(10))
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
                refreshTask = executor.schedule(this::reauthenticate, refresh.getExpiresIn() - 300, TimeUnit.SECONDS);
            } else {
                AuthError error = GSON.fromJson(json, AuthError.class);
                log.error("Error occurred while re-authenticating with OSRSLog, " + error.getError().getMessage());
                message("OSRSLog Re-auth", "Error occurred while re-authenticating to OSRSLog.");
                logout();
                plugin.addAndRemoveButtons();
            }
        } catch (IOException e) {
            log.error("Exception occurred while re-authenticating with OSRSLog.", e);
            message("OSRSLog Re-auth", "Exception occurred while re-authenticating to OSRSLog.");
        }
    }

    public void upload(Image image, boolean notify) {
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", image.getName(), RequestBody.create(PNG, image.getFile()))
                .addFormDataPart("metadata", createMetadata())
                .build();

        Request request = new Request.Builder()
                .url(OSRSLOG_UPLOAD)
                .header("Authorization", "Bearer " + idToken.get())
                .post(body)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.error("Exception occurred while uploading to OSRSLog.", e);
                message("OSRSLog Upload", "Exception occurred while uploading to OSRSLog.");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                if (response.isSuccessful()) {
                    Link link = GSON.fromJson(body, Link.class);
                    StringSelection selection = new StringSelection(link.getData());
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(selection, selection);

                    if (notify) {
                        notifier.notify("A screenshot was uploaded and inserted into your clipboard!", TrayIcon.MessageType.INFO);
                    }

                    message("OSRSLog Upload", "A screenshot was uploaded and inserted into your clipboard!");
                } else {
                    UploadError error = GSON.fromJson(body, UploadError.class);
                    log.error("Error occurred while uploading to OSRSLog. " + error.getMessage());
                    message("OSRSLog Upload", "Error occurred while uploading to OSRSLog.");
                }
            }
        });
    }

    private void message(String title, String message) {
        // null id means this was called from logging in
        if (idToken.get() == null || !client.getGameState().equals(GameState.LOGGED_IN)) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
            });
        } else {
            clientThread.invokeLater(() -> {
                client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", new ChatMessageBuilder().append(Color.RED, message).build(), null);
            });
        }
    }

    public String createMetadata() {
        Player player = client.getLocalPlayer();
        if (player == null) return "";

        List<Player> players = client.getPlayers();
        WorldPoint point = player.getWorldLocation();
        String skullIcon = player.getSkullIcon() != null ? player.getSkullIcon().name() : "";

        List<OtherPlayer> nearby = players.stream()
                .filter(p -> !p.equals(player))
                .map(PLAYER_MAPPER)
                .collect(Collectors.toList());


        String eventType = "NOT_IMPLEMENTED";

        return GSON.toJson(
                new Metadata(
                        player.getName(),
                        client.getAccountType().name(),
                        skullIcon,
                        eventType,
                        nearby,
                        client.getWorldType(),
                        new int[] { point.getX(), point.getY(), point.getPlane() },
                        player.getCombatLevel(),
                        client.getWorld(),
                        client.getTotalLevel(),
                        client.getAccountType().isIronman()
                )
        );
    }

}