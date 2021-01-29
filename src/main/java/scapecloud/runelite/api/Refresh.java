package scapecloud.runelite.api;

import lombok.Data;

@Data
public class Refresh {

    private final int expiresIn;
    private final String tokenType;
    private final String refreshToken;
    private final String idToken;
    private final String userId;
    private final String projectId;

}
