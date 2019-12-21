package net.woggioni.jwo.http;

import lombok.AccessLevel;
import lombok.Builder;

import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Builder(builderMethodName = "privateBuilder", access = AccessLevel.PUBLIC)
public class HttpRequest {

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

