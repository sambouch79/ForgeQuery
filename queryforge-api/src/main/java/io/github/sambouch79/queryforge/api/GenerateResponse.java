package io.github.sambouch79.queryforge.api;


import java.util.List;

public class GenerateResponse {

    public boolean success;
    public String sql;
    public String model;
    public String version;
    public List<String> errors;

    // Factory methods
    public static GenerateResponse ok(String sql, String model, String version) {
        GenerateResponse r = new GenerateResponse();
        r.success = true;
        r.sql     = sql;
        r.model   = model;
        r.version = version != null ? version.toString() : null;
        return r;
    }

    public static GenerateResponse error(List<String> errors) {
        GenerateResponse r = new GenerateResponse();
        r.success = false;
        r.errors  = errors;
        return r;
    }
}
