/*
 * Copyright (c) 2018, Lotto <https://github.com/devLotto>
 * Copyright (c) 2019, Alexsuperfly <https://github.com/Alexsuperfly>
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

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import scapecloud.runelite.api.Image;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.WorldType;
import net.runelite.client.Notifier;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;

import static net.runelite.client.RuneLite.SCREENSHOT_DIR;

@Slf4j
@Singleton
public class ScapeCloudImageCapture {

    private static final DateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    private final Client client;
    private final Notifier notifier;
    private final ScapeCloudAPI api;

    @Inject
    private ScapeCloudImageCapture(Client client, Notifier notifier, ScapeCloudAPI api) {
        this.client = client;
        this.notifier = notifier;
        this.api = api;
    }

    /**
     * Saves a screenshot of the client window to the screenshot folder as a PNG,
     * and optionally uploads it to an image-hosting service.
     *
     * @param screenshot BufferedImage to capture.
     * @param fileName   Filename to use, without file extension.
     * @param subDir     Directory within the player screenshots dir to store the captured screenshot to.
     * @param notify     Send a notification to the system tray when the image is captured.
     */
    public void takeScreenshot(BufferedImage screenshot, String fileName, @Nullable String subDir, boolean notify) {
        if (client.getGameState() == GameState.LOGIN_SCREEN) {
            // Prevent the screenshot from being captured
            log.info("Login screenshot prevented");
            return;
        }

        File playerFolder = SCREENSHOT_DIR;
        if (client.getLocalPlayer() != null && client.getLocalPlayer().getName() != null) {
            final EnumSet<WorldType> worldTypes = client.getWorldType();

            String playerDir = client.getLocalPlayer().getName();
            if (worldTypes.contains(WorldType.DEADMAN)) {
                playerDir += "-Deadman";
            } else if (worldTypes.contains(WorldType.LEAGUE)) {
                playerDir += "-League";
            }

            if (!Strings.isNullOrEmpty(subDir)) {
                playerDir += File.separator + subDir;
            }

            playerFolder = new File(SCREENSHOT_DIR, playerDir);
        }

        playerFolder.mkdirs();

        fileName += (fileName.isEmpty() ? "" : " ") + format(new Date());

        try {
            File screenshotFile = new File(playerFolder, fileName + ".png");

            // To make sure that screenshots don't get overwritten, check if file exists,
            // and if it does create file with same name and suffix.
            int i = 1;
            while (screenshotFile.exists()) {
                screenshotFile = new File(playerFolder, fileName + String.format("(%d)", i++) + ".png");
            }

            ImageIO.write(screenshot, "PNG", screenshotFile);

            System.out.println(screenshotFile.getName() + ", " + api.isAuthenticated());

            if (api.isAuthenticated()) {
                byte[] bytes = Files.readAllBytes(screenshotFile.toPath());
                api.upload(new Image(screenshotFile.getName(), bytes), notify);
            } else if (notify) {
                notifier.notify("A screenshot was saved to " + screenshotFile, TrayIcon.MessageType.INFO);
            }
        } catch (IOException ex) {
            log.warn("error writing screenshot", ex);
        }
    }

    private static String format(Date date)
    {
        synchronized (TIME_FORMAT)
        {
            return TIME_FORMAT.format(date);
        }
    }

}
