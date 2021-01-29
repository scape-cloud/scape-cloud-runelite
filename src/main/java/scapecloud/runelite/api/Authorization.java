package scapecloud.runelite.api;

import lombok.Data;

@Data
public class Authorization {

    private final String kind;
    private final String localId;
    private final String email;
    private final String displayName;
    private final String idToken;
    private final boolean registered;
    private final String refreshToken;
    private final int expiresIn;

}
