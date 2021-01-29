package scapecloud.runelite.api;

import lombok.Data;

import java.util.List;

@Data
public class Type {

    private final int code;
    private final String message;
    private final List<Reason> errors;

}
