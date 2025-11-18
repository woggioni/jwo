package net.woggioni.jwo.url.jpms;

import java.io.IOException;
import java.io.InputStream;
import java.lang.Module;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Optional;

public class Handler extends URLStreamHandler {

    @Override
    protected URLConnection openConnection(final URL u) throws IOException {
        final Module thisModule = getClass().getModule();
        final Module sourceModule = Optional.ofNullable(thisModule)
                .map(Module::getLayer)
                .flatMap(layer -> {
                    final String moduleName = u.getHost();
                    return layer.findModule(moduleName);
                }).orElse(thisModule);
        return new ModuleResourceURLConnection(u, sourceModule);
    }

    private static class ModuleResourceURLConnection extends URLConnection {
        private final Module module;

        private ModuleResourceURLConnection(final URL url, final Module module) {
            super(url);
            this.module = module;
        }

        @Override
        public void connect() {

        }

        @Override
        public InputStream getInputStream() throws IOException {
            return module.getResourceAsStream(getURL().getPath());
        }
    }
}
