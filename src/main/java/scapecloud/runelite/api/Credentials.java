package scapecloud.runelite.api;

import lombok.Data;

@Data
public class Credentials {

    private final String email;
    private final String password;
    private final boolean returnSecureToken = true;

}
