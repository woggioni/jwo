package net.woggioni.jwo.http;

public enum HttpStatus {
    OK(200),
    INTERNAL_SERVER_ERROR(500),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    CONFLICT(409);

    public final int code;

    HttpStatus(int code) {
        this.code = code;
    }
}
