package io.github.sambouch79.queryforge.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a join condition (e.g., "i.ID = d.ID")
 *
 * @author Sam
 */
public class JoinCondition {

    private final String left;
    private final String operator;
    private final String right;

    @JsonCreator
    public JoinCondition(
            @JsonProperty("left") String left,
            @JsonProperty("operator") String operator,
            @JsonProperty("right") String right
    ) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public String getLeft() {
        return left;
    }

    public String getOperator() {
        return operator;
    }

    public String getRight() {
        return right;
    }

    public String toSQL() {
        return left + " " + operator + " " + right;
    }

    public static JoinCondition eq(String left, String right) {
        return new JoinCondition(left, "=", right);
    }
}