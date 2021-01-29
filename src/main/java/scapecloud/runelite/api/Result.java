package scapecloud.runelite.api;

import lombok.Data;

@Data
public class Result {

    private final Authorization authorization;
    private final Error error;
    private final Exception exception;

    public boolean isSuccess() {
        return authorization != null;
    }

    public boolean isError() {
        return error != null;
    }

    public boolean isException() {
        return exception != null;
    }

    public static Result ok(Authorization authorization) {
        return new Result(authorization, null, null);
    }

    public static Result error(Error error) {
        return new Result(null, error, null);
    }

    public static Result exception(Exception exception) {
        return new Result(null, null, exception);
    }

}
