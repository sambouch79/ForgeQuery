package io.github.sambouch79.queryforge.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for Version class
 * 
 * @author Sam
 */
class VersionTest {
    
    @Test
    void shouldParseValidVersion() {
        Version version = Version.parse("1.2.3");
        
        assertThat(version.getMajor()).isEqualTo(1);
        assertThat(version.getMinor()).isEqualTo(2);
        assertThat(version.getPatch()).isEqualTo(3);
    }
    
    @Test
    void shouldCreateVersionFromComponents() {
        Version version = Version.of(2, 5, 10);
        
        assertThat(version.toString()).isEqualTo("2.5.10");
    }
    
    @Test
    void shouldRejectInvalidVersionFormat() {
        assertThatThrownBy(() -> Version.parse("1.2"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid version format");
        
        assertThatThrownBy(() -> Version.parse("abc"))
            .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    void shouldRejectNegativeComponents() {
        assertThatThrownBy(() -> Version.of(-1, 0, 0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("must be >= 0");
    }
    
    @Test
    void shouldCompareVersionsCorrectly() {
        Version v1 = Version.parse("1.0.0");
        Version v2 = Version.parse("1.0.1");
        Version v3 = Version.parse("1.1.0");
        Version v4 = Version.parse("2.0.0");
        
        assertThat(v1).isLessThan(v2);
        assertThat(v2).isLessThan(v3);
        assertThat(v3).isLessThan(v4);
        assertThat(v1).isEqualByComparingTo(Version.parse("1.0.0"));
    }
    
    @Test
    void shouldCheckCompatibility() {
        Version v1_0_0 = Version.parse("1.0.0");
        Version v1_1_0 = Version.parse("1.1.0");
        Version v1_2_0 = Version.parse("1.2.0");
        Version v2_0_0 = Version.parse("2.0.0");
        
        // Same major, higher or equal minor = compatible
        assertThat(v1_2_0.isCompatibleWith(v1_0_0)).isTrue();
        assertThat(v1_2_0.isCompatibleWith(v1_1_0)).isTrue();
        assertThat(v1_1_0.isCompatibleWith(v1_1_0)).isTrue();
        
        // Same major, lower minor = not compatible
        assertThat(v1_0_0.isCompatibleWith(v1_1_0)).isFalse();
        
        // Different major = not compatible
        assertThat(v2_0_0.isCompatibleWith(v1_0_0)).isFalse();
        assertThat(v1_0_0.isCompatibleWith(v2_0_0)).isFalse();
    }
}
