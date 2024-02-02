package net.woggioni.jwo;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import org.slf4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.woggioni.jwo.JWO.newThrowable;

public class HttpClient {
    private  static final Logger log = LoggerController.lazyLogger(HttpClient.class);
    private static final String COOKIES_HEADER = "Set-Cookie";
    private final CookieStore cookieStore = new CookieManager().getCookieStore();

    private SSLSocketFactory socketFactory;

    @Getter
    private Map<String, List<String>> stickyHeaders = new TreeMap<>();

    public HttpClient() {}

    public HttpClient(final SSLContext sslContext) {
        socketFactory = sslContext.getSocketFactory();
    }

    @SneakyThrows
    public HttpURLConnection call(HttpRequest httpRequest) {
        HttpURLConnection conn = (HttpURLConnection) httpRequest.url.openConnection();

        if (socketFactory != null && conn instanceof HttpsURLConnection) {
            ((HttpsURLConnection) conn).setSSLSocketFactory(socketFactory);
        }
        conn.setInstanceFollowRedirects(true);
        conn.setRequestMethod(httpRequest.method.text);
        Stream.of(stickyHeaders, httpRequest.headers)
            .map(Map::entrySet)
            .flatMap(Set::stream)
            .forEach(entry ->
                entry.getValue()
                    .forEach(headerValue ->
                        conn.addRequestProperty(entry.getKey(), headerValue)));
        List<HttpCookie> cookies = cookieStore.get(httpRequest.getUrl().toURI());
        if (!cookies.isEmpty()) {
            conn.setRequestProperty("Cookie",
                cookies.stream()
                    .map(HttpCookie::toString)
                    .collect(Collectors.joining(";"))
            );
        }
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
        conn.getResponseCode();
        Map<String, List<String>> headerFields = conn.getHeaderFields();
        List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);
        if (cookiesHeader != null) {
            for (String cookie : cookiesHeader) {
                cookieStore.add(httpRequest.url.toURI(), HttpCookie.parse(cookie).get(0));
            }
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

    @ToString
    public enum HttpStatus {
        OK(200),
        INTERNAL_SERVER_ERROR(500),
        SERVICE_UNAVAILABLE(503),
        BAD_REQUEST(400),
        UNAUTHORIZED(401),
        FORBIDDEN(403),
        NOT_FOUND(404),
        METHOD_NOT_ALLOWED(405),
        CONFLICT(409),
        UNSUPPORTED_MEDIA_TYPE(415),
        GATEWAY_TIMEOUT(504);

        public final int code;

        HttpStatus(int code) {
            this.code = code;
        }

        public static HttpStatus of(int code) {
            return Arrays.stream(values())
                .filter(it -> it.code == code)
                .findFirst()
                .orElseThrow(() -> newThrowable(
                    IllegalArgumentException.class,
                    "HTTP status code %d is not mapped",
                    code)
                );
        }
    }

    @Getter
    @Builder(builderMethodName = "privateBuilder", access = AccessLevel.PUBLIC)
    public static class HttpRequest {

        private final URL url;

        @Builder.Default
        private final HttpMethod method = HttpMethod.GET;

        @Builder.Default
        private final Map<String, List<String>> headers = Collections.emptyMap();

        @Builder.Default
        private final InputStream body = null;

        public static HttpRequestBuilder builder(URL url) {
            return HttpRequest.privateBuilder().url(url);
        }
    }
}
