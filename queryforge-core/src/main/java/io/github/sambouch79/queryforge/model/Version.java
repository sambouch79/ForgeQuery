package io.github.sambouch79.queryforge.model;

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
    
    int major;
    int minor;
    int patch;
    
    /**
     * Parse a version string (e.g., "1.2.3")
     */
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
    
    /**
     * Create a version from components
     */
    public static Version of(int major, int minor, int patch) {
        if (major < 0 || minor < 0 || patch < 0) {
            throw new IllegalArgumentException("Version components must be >= 0");
        }
        return new Version(major, minor, patch);
    }
    
    /**
     * Check if this version is compatible with another version.
     * Compatible means same major version and this minor >= other minor.
     */
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
    public String toString() {
        return major + "." + minor + "." + patch;
    }
}
