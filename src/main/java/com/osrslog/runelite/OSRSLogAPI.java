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
package com.osrslog.runelite;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageBuilder;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.osrslog.runelite.api.Image;
import com.osrslog.runelite.api.ItemInfo;
import com.osrslog.runelite.api.Link;
import com.osrslog.runelite.api.Metadata;
import com.osrslog.runelite.api.NearbyPlayer;
import com.osrslog.runelite.api.SkillInfo;
import com.osrslog.runelite.api.UploadError;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Singleton
class OSRSLogAPI {

//    private static final String AUTH_ENDPOINT = "http://localhost:3000/api/runelite"
//    private static final String FIREBASE_AUTH = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=AIzaSyD64AKzvmmEiFkn-4U5X54D24He813qCjk";
//    private static final String FIREBASE_REFRESH = "https://securetoken.googleapis.com/v1/token?key=AIzaSyD64AKzvmmEiFkn-4U5X54D24He813qCjk";

    // https://www.osrslog.com/api/upload
    private static final String OSRSLOG_UPLOAD = "http://localhost:3000/api/runelite/upload-media";

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType PNG = MediaType.parse("image/png");
    private static final Gson GSON = new Gson();

//    private static final int REFRESH_OFFSET = (int) TimeUnit.MINUTES.toSeconds(5);

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
    private OSRSLogPlugin plugin;

    private final AtomicReference<String> uploadKey = new AtomicReference<>();

    public boolean isAuthenticated() {
        return uploadKey.get() != null;
    }

    public void logout() {
        uploadKey.set(null);
    }

    public void authenticate(String uploadKey) {
        this.uploadKey.set(uploadKey);
    }

    public void upload(Image image, boolean notify) {
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", image.getName(), RequestBody.create(PNG, image.getFile()))
                .addFormDataPart("metadata", createMetadata())
                .build();

        Request request = new Request.Builder()
                .url(OSRSLOG_UPLOAD)
                .header("X-Upload-Key", "" + uploadKey.get())
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
        if (uploadKey.get() == null || !client.getGameState().equals(GameState.LOGGED_IN)) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
            });
        } else {
            clientThread.invokeLater(() -> {
                client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", new ChatMessageBuilder().append(Color.RED, message).build(), null);
            });
        }
    }

    private String createMetadata() {
        Player player = client.getLocalPlayer();
        if (player == null) return "";

        Skill[] skillNames = Skill.values();
        List<Player> players = client.getPlayers();
        int[] boostedLevels = client.getBoostedSkillLevels();
        int[] exp = client.getSkillExperiences();
        int[] levels = client.getRealSkillLevels();

        WorldPoint pos = player.getWorldLocation();
        String skullIcon = player.getSkullIcon() != null ? player.getSkullIcon().name() : "";

        List<NearbyPlayer> nearby = players.stream()
                .filter(p -> !p.equals(player))
                .map(NearbyPlayer::new)
                .collect(Collectors.toList());

        String eventType = "NOT_IMPLEMENTED";

        List<SkillInfo> skills = IntStream.range(0, skillNames.length)
                .mapToObj(i -> new SkillInfo(skillNames[i].getName(), boostedLevels[i], levels[i], exp[i]))
                .collect(Collectors.toList());

        List<ItemInfo> equipment = new ArrayList<>();
        ItemContainer equipmentContainer = client.getItemContainer(InventoryID.EQUIPMENT);
        if (equipmentContainer != null) {
                Arrays.stream(equipmentContainer.getItems())
                .map(item -> new ItemInfo(client.getItemDefinition(item.getId()), item.getQuantity()))
                .forEachOrdered(equipment::add);
        }

        List<ItemInfo> inventory = new ArrayList<>();
        ItemContainer inventoryContainer = client.getItemContainer(InventoryID.EQUIPMENT);
        if (inventoryContainer != null) {
            Arrays.stream(inventoryContainer.getItems())
                    .map(item -> new ItemInfo(client.getItemDefinition(item.getId()), item.getQuantity()))
                    .forEachOrdered(inventory::add);
        }

        return GSON.toJson(new Metadata(
                player.getName(),
                client.getAccountType().name(),
                skullIcon,
                eventType,
                nearby,
                client.getWorldType(),
                Arrays.asList(pos.getX(), pos.getY(), pos.getPlane()),
                player.getCombatLevel(),
                client.getWorld(),
                client.getTotalLevel(),
                client.getAccountType().isIronman(),
                equipment,
                inventory,
                skills
        ));
    }

}