package io.github.sambouch79.queryforge.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for Field implementations
 * 
 * @author Sam
 */
class FieldTest {
    
    @Test
    void simpleField_shouldGenerateDirectColumnReference() {
        SimpleField field = SimpleField.of("i.NOM_INDIVIDU");
        
        assertThat(field.toSQL()).isEqualTo("i.NOM_INDIVIDU");
        assertThat(field.getType()).isEqualTo("field");
    }
    
    @Test
    void simpleField_shouldRejectNullOrEmpty() {
        assertThatThrownBy(() -> SimpleField.of(null))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThatThrownBy(() -> SimpleField.of(""))
            .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    void functionField_shouldGenerateNVLFunction() {
        FunctionField field = FunctionField.of("NVL", "a.COMPL_DEST", "d.COMPL_DEST");
        
        assertThat(field.toSQL()).isEqualTo("NVL(a.COMPL_DEST, d.COMPL_DEST)");
        assertThat(field.getType()).isEqualTo("function");
    }
    
    @Test
    void functionField_shouldHandleLiterals() {
        FunctionField field = FunctionField.of("COALESCE", "i.PRENOM", "Unknown");
        
        // "Unknown" devrait être entouré de quotes car c'est un littéral
        assertThat(field.toSQL()).isEqualTo("COALESCE(i.PRENOM, 'Unknown')");
    }
    
    @Test
    void functionField_shouldEscapeSingleQuotes() {
        FunctionField field = FunctionField.of("NVL", "i.NAME", "O'Brien");
        
        assertThat(field.toSQL()).isEqualTo("NVL(i.NAME, 'O''Brien')");
    }
    
    @Test
    void rawField_shouldReturnRawSQL() {
        RawField field = RawField.of("CASE i.SEXE WHEN 0 THEN 'M' ELSE 'F' END");
        
        assertThat(field.toSQL()).isEqualTo("CASE i.SEXE WHEN 0 THEN 'M' ELSE 'F' END");
        assertThat(field.getType()).isEqualTo("raw");
    }
    
    @Test
    void rawField_shouldRejectNullOrEmpty() {
        assertThatThrownBy(() -> RawField.of(null))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThatThrownBy(() -> RawField.of("  "))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
