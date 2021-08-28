/*
 * Copyright (c) 2018, Lotto <https://github.com/devLotto>
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

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Keybind;
import net.runelite.client.util.ImageUploadStyle;

@ConfigGroup("scape-cloud")
public interface ScapeCloudConfig extends Config
{

    @ConfigItem(
            keyName = "includeFrame",
            name = "Include Client Frame",
            description = "Configures whether or not the client frame is included in screenshots",
            position = 1
    )
    default boolean includeFrame()
    {
        return false;
    }

    @ConfigItem(
            keyName = "displayDate",
            name = "Display Date",
            description = "Configures whether or not the report button shows the date the screenshot was taken",
            position = 2
    )
    default boolean displayDate()
    {
        return true;
    }

    @ConfigItem(
            keyName = "notifyWhenTaken",
            name = "Notify When Taken",
            description = "Configures whether or not you are notified when a screenshot has been taken",
            position = 3
    )
    default boolean notifyWhenTaken()
    {
        return true;
    }

    @ConfigItem(
            keyName = "hotkey",
            name = "Screenshot hotkey",
            description = "When you press this key a screenshot will be taken",
            position = 4
    )
    default Keybind hotkey()
    {
        return Keybind.NOT_SET;
    }

    @ConfigSection(
            name = "What to Screenshot",
            description = "All the options that select what to screenshot",
            position = 5
    )
    String whatSection = "what";

    @ConfigItem(
            keyName = "rewards",
            name = "Screenshot Rewards",
            description = "Configures whether screenshots are taken of clues, barrows, and quest completion",
            position = 6,
            section = whatSection
    )
    default boolean screenshotRewards()
    {
        return true;
    }

    @ConfigItem(
            keyName = "levels",
            name = "Screenshot Levels",
            description = "Configures whether screenshots are taken of level ups",
            position = 7,
            section = whatSection
    )
    default boolean screenshotLevels()
    {
        return true;
    }

    @ConfigItem(
            keyName = "kingdom",
            name = "Screenshot Kingdom Reward",
            description = "Configures whether screenshots are taken of Kingdom Reward",
            position = 8,
            section = whatSection
    )
    default boolean screenshotKingdom()
    {
        return true;
    }

    @ConfigItem(
            keyName = "pets",
            name = "Screenshot Pet",
            description = "Configures whether screenshots are taken of receiving pets",
            position = 9,
            section = whatSection
    )
    default boolean screenshotPet()
    {
        return true;
    }

    @ConfigItem(
            keyName = "kills",
            name = "Screenshot PvP Kills",
            description = "Configures whether or not screenshots are automatically taken of PvP kills",
            position = 10,
            section = whatSection
    )
    default boolean screenshotKills()
    {
        return false;
    }

    @ConfigItem(
            keyName = "boss",
            name = "Screenshot Boss Kills",
            description = "Configures whether or not screenshots are automatically taken of boss kills",
            position = 11,
            section = whatSection
    )
    default boolean screenshotBossKills()
    {
        return false;
    }

    @ConfigItem(
            keyName = "playerDeath",
            name = "Screenshot Deaths",
            description = "Configures whether or not screenshots are automatically taken when you die.",
            position = 12,
            section = whatSection
    )
    default boolean screenshotPlayerDeath()
    {
        return false;
    }

    @ConfigItem(
            keyName = "friendDeath",
            name = "Screenshot Friend Deaths",
            description = "Configures whether or not screenshots are automatically taken when friends or friends chat members die.",
            position = 13,
            section = whatSection
    )
    default boolean screenshotFriendDeath()
    {
        return false;
    }

    @ConfigItem(
            keyName = "duels",
            name = "Screenshot Duels",
            description = "Configures whether or not screenshots are automatically taken of the duel end screen.",
            position = 14,
            section = whatSection
    )
    default boolean screenshotDuels()
    {
        return false;
    }

    @ConfigItem(
            keyName = "valuableDrop",
            name = "Screenshot Valuable drops",
            description = "Configures whether or not screenshots are automatically taken when you receive a valuable drop.",
            position = 15,
            section = whatSection
    )
    default boolean screenshotValuableDrop()
    {
        return false;
    }

    @ConfigItem(
            keyName = "valuableDropThreshold",
            name = "Valuable Threshold",
            description = "The minimum value to save screenshots of valuable drops.",
            position = 16,
            section = whatSection
    )
    default int valuableDropThreshold()
    {
        return 0;
    }

    @ConfigItem(
            keyName = "untradeableDrop",
            name = "Screenshot Untradeable drops",
            description = "Configures whether or not screenshots are automatically taken when you receive an untradeable drop.",
            position = 17,
            section = whatSection
    )
    default boolean screenshotUntradeableDrop()
    {
        return false;
    }

    @ConfigItem(
            keyName = "ccKick",
            name = "Screenshot Kicks from FC",
            description = "Take a screenshot when you kick a user from a friends chat.",
            position = 18,
            section = whatSection
    )
    default boolean screenshotKick()
    {
        return false;
    }

    @ConfigItem(
            keyName = "baHighGamble",
            name = "Screenshot BA high gambles",
            description = "Take a screenshot of your reward from a high gamble at Barbarian Assault.",
            position = 19,
            section = whatSection
    )
    default boolean screenshotHighGamble()
    {
        return false;
    }

    @ConfigItem(
            keyName = "collectionLogEntries",
            name = "Screenshot collection log entries",
            description = "Take a screenshot when completing an entry in the collection log",
            position = 20,
            section = whatSection
    )
    default boolean screenshotCollectionLogEntries()
    {
        return true;
    }

    @ConfigItem(
            keyName = "email",
            name = "Email",
            description = "Email for Scape Cloud Service",
            hidden = true,
            position = 21
    )
    default String email()
    {
        return "";
    }

    @ConfigItem(
            keyName = "password",
            name = "Password",
            description = "Password for Scape Cloud Service",
            secret = true,
            hidden = true,
            position = 22
    )
    default String password()
    {
        return "";
    }

}
