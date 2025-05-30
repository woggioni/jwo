package net.woggioni.jwo;


import java.util.ArrayList;
import java.util.List;

/**
 * An enumeration of Java versions.
 * Before 9: http://www.oracle.com/technetwork/java/javase/versioning-naming-139433.html
 * 9+: http://openjdk.java.net/jeps/223
 */
public enum JavaVersion {
    VERSION_1_1, VERSION_1_2, VERSION_1_3, VERSION_1_4,
    VERSION_1_5, VERSION_1_6, VERSION_1_7, VERSION_1_8,
    VERSION_1_9, VERSION_1_10,
    /**
     * Java 11 major version.
     *
     */
    VERSION_11,

    /**
     * Java 12 major version.
     *
     */
    VERSION_12,

    /**
     * Java 13 major version.
     *
     */
    VERSION_13,

    /**
     * Java 14 major version.
     *
     */
    VERSION_14,

    /**
     * Java 15 major version.
     *
     */
    VERSION_15,

    /**
     * Java 16 major version.
     *
     */
    VERSION_16,

    /**
     * Java 17 major version.
     *
     */
    VERSION_17,

    /**
     * Java 18 major version.
     *
     */
    VERSION_18,

    /**
     * Java 19 major version.
     *
     */
    VERSION_19,

    /**
     * Java 20 major version.
     */
    VERSION_20,

    /**
     * Java 20 major version.
     */
    VERSION_21,

    /**
     * Higher version of Java.
     */
    VERSION_HIGHER;
    // Since Java 9, version should be X instead of 1.X
    // However, to keep backward compatibility, we change from 11
    private static final int FIRST_MAJOR_VERSION_ORDINAL = 10;
    private static LazyValue<JavaVersion> currentJavaVersion = LazyValue.of(
        () -> toVersion(System.getProperty("java.version")),
        LazyValue.ThreadSafetyMode.SYNCHRONIZED
    );
    private final String versionName;

    JavaVersion() {
        this.versionName = ordinal() >= FIRST_MAJOR_VERSION_ORDINAL ? getMajorVersion() : "1." + getMajorVersion();
    }

    /**
     * Converts the given object into a {@code JavaVersion}.
     *
     * @param value An object whose toString() value is to be converted. May be null.
     * @return The version, or null if the provided value is null.
     * @throws IllegalArgumentException when the provided value cannot be converted.
     */
    public static JavaVersion toVersion(Object value) throws IllegalArgumentException {
        if (value == null) {
            return null;
        }
        if (value instanceof JavaVersion) {
            return (JavaVersion) value;
        }
        if (value instanceof Integer) {
            return getVersionForMajor((Integer) value);
        }

        String name = value.toString();

        int firstNonVersionCharIndex = findFirstNonVersionCharIndex(name);

        String[] versionStrings = name.substring(0, firstNonVersionCharIndex).split("\\.");
        List<Integer> versions = convertToNumber(name, versionStrings);

        if (isLegacyVersion(versions)) {
            assertTrue(name, versions.get(1) > 0);
            return getVersionForMajor(versions.get(1));
        } else {
            return getVersionForMajor(versions.get(0));
        }
    }

    /**
     * Returns the version of the current JVM.
     *
     * @return The version of the current JVM.
     */
    public static JavaVersion current() {
        return currentJavaVersion.get();
    }

    static void resetCurrent() {
        currentJavaVersion = null;
    }

    public static JavaVersion forClassVersion(int classVersion) {
        return getVersionForMajor(classVersion - 44); //class file versions: 1.1 == 45, 1.2 == 46...
    }

    public static JavaVersion forClass(byte[] classData) {
        if (classData.length < 8) {
            throw new IllegalArgumentException("Invalid class format. Should contain at least 8 bytes");
        }
        return forClassVersion(classData[7] & 0xFF);
    }

    /**
     * Returns if this version is compatible with the given version
     *
     * @since 6.0
     */
    public boolean isCompatibleWith(JavaVersion otherVersion) {
        return this.compareTo(otherVersion) >= 0;
    }

    @Override
    public String toString() {
        return versionName;
    }

    public String getMajorVersion() {
        return String.valueOf(ordinal() + 1);
    }

    private static JavaVersion getVersionForMajor(int major) {
        return major >= values().length ? JavaVersion.VERSION_HIGHER : values()[major - 1];
    }

    private static void assertTrue(String value, boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException("Could not determine java version from '" + value + "'.");
        }
    }

    private static boolean isLegacyVersion(List<Integer> versions) {
        return 1 == versions.get(0) && versions.size() > 1;
    }

    private static List<Integer> convertToNumber(String value, String[] versionStrs) {
        List<Integer> result = new ArrayList<>();
        for (String s : versionStrs) {
            assertTrue(value, !isNumberStartingWithZero(s));
            try {
                result.add(Integer.parseInt(s));
            } catch (NumberFormatException e) {
                assertTrue(value, false);
            }
        }
        assertTrue(value, !result.isEmpty() && result.get(0) > 0);
        return result;
    }

    private static boolean isNumberStartingWithZero(String number) {
        return number.length() > 1 && number.startsWith("0");
    }

    private static int findFirstNonVersionCharIndex(String s) {
        assertTrue(s, s.length() != 0);

        for (int i = 0; i < s.length(); ++i) {
            if (!isDigitOrPeriod(s.charAt(i))) {
                assertTrue(s, i != 0);
                return i;
            }
        }

        return s.length();
    }

    private static boolean isDigitOrPeriod(char c) {
        return (c >= '0' && c <= '9') || c == '.';
    }
}