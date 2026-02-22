package io.github.sambouch79.queryforge.model;

/**
 * Utility class for formatting SQL values.
 * Handles: bind params, numerics, column refs, string literals.
 */
public final class ValueFormatter {

    private ValueFormatter() {}

    /**
     * Format a value for SQL output:
     * - null          → NULL
     * - ":param"      → :param      (bind parameter, pas de quotes)
     * - "42" / "3.14" → 42 / 3.14  (numérique, pas de quotes)
     * - "i.NOM"       → i.NOM       (référence colonne, pas de quotes)
     * - "ACTIF"       → 'ACTIF'     (littéral string, avec quotes)
     */
    public static String format(String val) {
        if (val == null)                         return "NULL";
        if (val.startsWith(":"))                 return val;
        if (val.matches("-?\\d+(\\.\\d+)?"))     return val;
        if (val.contains("."))                   return val;
        return "'" + val.replace("'", "''") + "'";
    }
}
