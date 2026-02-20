package io.github.sambouch79.queryforge.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


/**
 * Represents the database schema: base table and joins
 *
 * @author Sam
 */
public class Schema {


    private final FromSource baseTable;

    private final List<Join> joins;

    private final Condition filters;
    private final List<String> groupBy;
    private final   Condition having;
    private final   List<OrderByField> orderBy;
    private final    Integer limit;
    private final  Integer offset;

    @JsonCreator
    public Schema(
            @JsonProperty("baseTable") FromSource  baseTable,
            @JsonProperty("joins")     List<Join> joins,
            @JsonProperty("filters")   Condition filters,
            @JsonProperty("groupBy")   List<String> groupBy,
            @JsonProperty("having")    Condition having,
            @JsonProperty("orderBy")   List<OrderByField> orderBy,
            @JsonProperty("limit")     Integer limit,
            @JsonProperty("offset")    Integer offset
    ) {
        this.baseTable = baseTable;
        this.joins     = joins    != null ? joins    : List.of();
        this.filters   = filters;
        this.groupBy   = groupBy  != null ? groupBy  : List.of();
        this.having    = having;
        this.orderBy   = orderBy  != null ? orderBy  : List.of();
        this.limit     = limit;
        this.offset    = offset;
    }



    public FromSource  getBaseTable() {
        return baseTable;
    }

    public List<Join> getJoins() {
        return joins;
    }

    public Condition getFilters() { return filters; }

    public List<String>  getGroupBy() {
        return groupBy;
    }

    public Condition getHaving() {return having;}
    public List<OrderByField> getOrderBy() { return orderBy; }
    public Integer getLimit() {return limit;}
    public Integer getOffset() {return offset;}

    // Factory methods rétrocompatibles
    public static Schema of(FromSource  baseTable) {
        return new Schema(baseTable, List.of(), null, List.of(), null, List.of(), null, null);
    }

    public static Schema of(FromSource  baseTable, List<Join> joins) {
        return new Schema(baseTable, joins, null, List.of(), null, List.of(), null, null);
    }


    public static Schema of(Table baseTable, List<Join> joins,Condition filters) {
        return new Schema(baseTable, joins, filters, List.of(), null, List.of(), null, null);
    }
}