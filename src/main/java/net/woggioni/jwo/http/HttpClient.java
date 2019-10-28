package net.woggioni.jwo.http;

import lombok.SneakyThrows;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.OutputStream;

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
}
