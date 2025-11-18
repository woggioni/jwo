package net.woggioni.jwo;

import java.security.CodeSigner;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.regex.Pattern;

/**
 * Helper class to extract signatures from a jar file, it has to be used calling {@link #addEntry} on all of the jar's {@link JarEntry}
 * after having consumed their entry content from the source (@link java.util.jar.JarInputStream}, then {@link #getCertificates()}
 * will return the public keys of the jar's signers.
 */
class SignatureCollector {

    /**
     * @see <https://docs.oracle.com/javase/8/docs/technotes/guides/jar/jar.html#Signed_JAR_File>
     * Additionally accepting *.EC as its valid for [java.util.jar.JarVerifier] and jarsigner @see https://docs.oracle.com/javase/8/docs/technotes/tools/windows/jarsigner.html,
     * temporally treating META-INF/INDEX.LIST as unsignable entry because [java.util.jar.JarVerifier] doesn't load its signers.
     */
    private static final Pattern unsignableEntryName = Pattern.compile("META-INF/(?:(?:.*[.](?:SF|DSA|RSA|EC)|SIG-.*)|INDEX\\.LIST)");

    /**
     * @return if the [entry] [JarEntry] can be signed.
     */
    static boolean isSignable(final JarEntry entry) {
        return !entry.isDirectory() && !unsignableEntryName.matcher(entry.getName()).matches();
    }

    private static Set<PublicKey> signers2OrderedPublicKeys(final CodeSigner[] signers) {
        final Set<PublicKey> result = new LinkedHashSet<>();
        for (final CodeSigner signer : signers) {
            result.add((signer.getSignerCertPath().getCertificates().get(0)).getPublicKey());
        }
        return Collections.unmodifiableSet(result);
    }

    private String firstSignedEntry = null;
    private CodeSigner[] codeSigners = null;
    private Set<Certificate> _certificates;

    public final Set<Certificate> getCertificates() {
        return Collections.unmodifiableSet(_certificates);
    }

    public void addEntry(final JarEntry jarEntry) {
        if (isSignable(jarEntry)) {
            final CodeSigner[] entrySigners = jarEntry.getCodeSigners() != null ? jarEntry.getCodeSigners() : new CodeSigner[0];
            if (codeSigners == null) {
                codeSigners = entrySigners;
                firstSignedEntry = jarEntry.getName();
                for (final CodeSigner signer : entrySigners) {
                    _certificates.add(signer.getSignerCertPath().getCertificates().get(0));
                }
            }
            if (!Arrays.equals(codeSigners, entrySigners)) {
                throw new IllegalArgumentException(String.format(
                    "Mismatch between signers %s for file %s and signers %s for file %s",
                    signers2OrderedPublicKeys(codeSigners),
                    firstSignedEntry,
                    signers2OrderedPublicKeys(entrySigners),
                    jarEntry.getName()));
            }
        }
    }
}

