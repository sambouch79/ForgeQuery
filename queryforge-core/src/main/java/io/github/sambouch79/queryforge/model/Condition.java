package io.github.sambouch79.queryforge.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Represents a SQL condition used in WHERE or HAVING clauses.
 *
 * Implementations:
 * - SimpleCondition   : field op value      → i.STATUT = 'ACTIF'
 * - CompositeCondition: AND/OR of conditions → (cond1 AND cond2)
 * - InCondition       : field IN (...)       → i.CODE IN ('A', 'B')
 * - IsNullCondition   : IS NULL / NOT NULL   → i.DATE IS NULL
 * - BetweenCondition  : BETWEEN              → d.ANNEE BETWEEN 2020 AND 2024
 * - RawCondition      : raw SQL              → use with caution
 *
 * @author Sam
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SimpleCondition.class,    name = "simple"),
        @JsonSubTypes.Type(value = CompositeCondition.class, name = "and"),
        @JsonSubTypes.Type(value = CompositeCondition.class, name = "or"),
        @JsonSubTypes.Type(value = InCondition.class,        name = "in"),
        @JsonSubTypes.Type(value = IsNullCondition.class,    name = "is_null"),
        @JsonSubTypes.Type(value = BetweenCondition.class,   name = "between"),
        @JsonSubTypes.Type(value = RawCondition.class,       name = "raw"),
        @JsonSubTypes.Type(value = SubqueryCondition.class, name = "subquery_condition")
})
public sealed interface Condition permits
        SimpleCondition, CompositeCondition, InCondition,
        IsNullCondition, BetweenCondition, RawCondition,SubqueryCondition {

    String toSQL();
    String getType();
}