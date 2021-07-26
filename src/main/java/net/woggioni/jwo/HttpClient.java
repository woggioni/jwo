package net.woggioni.jwo;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.SneakyThrows;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class HttpClient {

    private SSLSocketFactory socketFactory;

    public HttpClient() {}

    public HttpClient(final SSLContext sslContext) {
        socketFactory = sslContext.getSocketFactory();
    }

    @SneakyThrows
    public HttpsURLConnection call(HttpRequest httpRequest) {
        HttpsURLConnection conn = (HttpsURLConnection) httpRequest.url.openConnection();
        if(socketFactory != null) {
            conn.setSSLSocketFactory(socketFactory);
        }
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod(httpRequest.method.text);
        httpRequest.headers.forEach((key, value) ->
            value.forEach(headerValue -> conn.addRequestProperty(key, headerValue)));
        switch (httpRequest.method) {
            case PUT:
            case POST:
            case DELETE:
                if (httpRequest.body != null) {
                    conn.setDoOutput(true);
                    byte[] buffer = new byte[1024];
                    OutputStream os = conn.getOutputStream();
                    while (true) {
                        int read = httpRequest.body.read(buffer, 0, buffer.length);
                        if (read < 0) break;
                        os.write(buffer, 0, read);
                    }
                }
                break;
            case GET:
            case HEAD:
            case OPTIONS:
                break;
        }
        return conn;
    }

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

    @Builder(builderMethodName = "privateBuilder", access = AccessLevel.PUBLIC)
    public static class HttpRequest {

        final URL url;

        final HttpMethod method = HttpMethod.GET;

        final Map<String, List<String>> headers = Collections.emptyMap();

        final InputStream body = null;

        public static HttpRequestBuilder builder(URL url) {
            return HttpRequest.privateBuilder().url(url);
        }

        public static class Builder {
            private final URL url;

            private HttpMethod method = HttpMethod.GET;

            private Map<String, List<String>> headers = Collections.emptyMap();

            private InputStream body = null;

            private Builder(URL url) {
                this.url = url;
            }

            public Builder method(HttpMethod method) {
                this.method = method;
                return this;
            }
        }
    }
}
