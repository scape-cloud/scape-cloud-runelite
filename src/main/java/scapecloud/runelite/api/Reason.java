package scapecloud.runelite.api;

import lombok.Data;

@Data
public class Reason {

    private final String domain;
    private final String reason;
    private final String message;

}
