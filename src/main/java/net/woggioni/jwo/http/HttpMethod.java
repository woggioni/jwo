package net.woggioni.jwo.http;

public enum HttpMethod {
    PUT("PUT"),
    GET("GET"),
    POST("POST"),
    DELETE("DELETE"),
    HEAD("HEAD"),
    OPTIONS("OPTIONS");

    public final String text;

    HttpMethod(String text) {
        this.text = text;
    }
}
