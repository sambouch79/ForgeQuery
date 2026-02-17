package io.github.sambouch79.queryforge.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Value;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a semantic version (MAJOR.MINOR.PATCH)
 * 
 * @author Sam
 */
@Value
public class Version implements Comparable<Version> {

    private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)$");

    private final int major;
    private final int minor;
    private final int patch;

    public Version(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public int getMajor() { return major; }
    public int getMinor() { return minor; }
    public int getPatch() { return patch; }

    /**
     * Parse a version string (e.g., "1.2.3")
     * Cette méthode est utilisée par Jackson pour désérialiser
     */
    @JsonCreator
    public static Version parse(String version) {
        Objects.requireNonNull(version, "Version cannot be null");

        Matcher matcher = VERSION_PATTERN.matcher(version.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Invalid version format: " + version + ". Expected format: MAJOR.MINOR.PATCH"
            );
        }

        return new Version(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3))
        );
    }

    public static Version of(int major, int minor, int patch) {
        if (major < 0 || minor < 0 || patch < 0) {
            throw new IllegalArgumentException("Version components must be >= 0");
        }
        return new Version(major, minor, patch);
    }

    public boolean isCompatibleWith(Version other) {
        return this.major == other.major && this.minor >= other.minor;
    }

    @Override
    public int compareTo(Version other) {
        int majorCmp = Integer.compare(this.major, other.major);
        if (majorCmp != 0) return majorCmp;

        int minorCmp = Integer.compare(this.minor, other.minor);
        if (minorCmp != 0) return minorCmp;

        return Integer.compare(this.patch, other.patch);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Version version = (Version) o;
        return major == version.major && minor == version.minor && patch == version.patch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch);
    }

    /**
     * Cette méthode est utilisée par Jackson pour sérialiser
     */
    @JsonValue
    @Override
    public String toString() {
        return major + "." + minor + "." + patch;
    }
}
