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

    @ConfigSection(
            name = "Authentication",
            description = "Authentication credentials for Scape Cloud Service",
            position = 0,
            closedByDefault = true
    )
    String authSection = "auth";

    @ConfigItem(
            keyName = "email",
            name = "Email",
            description = "Email for Scape Cloud Service",
            position = 1,
            section = authSection
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
            position = 2,
            section = authSection
    )
    default String password()
    {
        return "";
    }

    @ConfigItem(
            keyName = "includeFrame",
            name = "Include Client Frame",
            description = "Configures whether or not the client frame is included in screenshots",
            position = 3
    )
    default boolean includeFrame()
    {
        return true;
    }

    @ConfigItem(
            keyName = "displayDate",
            name = "Display Date",
            description = "Configures whether or not the report button shows the date the screenshot was taken",
            position = 4
    )
    default boolean displayDate()
    {
        return true;
    }

    @ConfigItem(
            keyName = "notifyWhenTaken",
            name = "Notify When Taken",
            description = "Configures whether or not you are notified when a screenshot has been taken",
            position = 5
    )
    default boolean notifyWhenTaken()
    {
        return true;
    }

    @ConfigItem(
            keyName = "hotkey",
            name = "Screenshot hotkey",
            description = "When you press this key a screenshot will be taken",
            position = 6
    )
    default Keybind hotkey()
    {
        return Keybind.NOT_SET;
    }

    @ConfigSection(
            name = "What to Screenshot",
            description = "All the options that select what to screenshot",
            position = 7
    )
    String whatSection = "what";

    @ConfigItem(
            keyName = "rewards",
            name = "Screenshot Rewards",
            description = "Configures whether screenshots are taken of clues, barrows, and quest completion",
            position = 8,
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
            position = 9,
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
            position = 10,
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
            position = 11,
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
            position = 12,
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
            position = 13,
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
            position = 14,
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
            position = 15,
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
            position = 16,
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
            position = 17,
            section = whatSection
    )
    default boolean screenshotValuableDrop()
    {
        return false;
    }

    @ConfigItem(
            keyName = "untradeableDrop",
            name = "Screenshot Untradeable drops",
            description = "Configures whether or not screenshots are automatically taken when you receive an untradeable drop.",
            position = 18,
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
            position = 19,
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
            position = 20,
            section = whatSection
    )
    default boolean screenshotHighGamble()
    {
        return false;
    }

}
