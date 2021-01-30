package scapecloud.runelite;

import lombok.Data;

@Data
public class ScapeCloudMetadata {

    private final String playerName;
    private final String accountType;
    private final String skullIcon;
    private final String eventType;
    private final String nearbyPlayers;
    private final String worldType;
    private final int[] location;
    private final int combatLevel;
    private final int world;
    private final int totalLevel;
    private final boolean isIronman;

    @Data
    static class OtherPlayer {
        private final String playerName;
        private final boolean isFriend;
        private final boolean isFriendsChat;
        private final int team;
        private final int combatLevel;
    }
}
