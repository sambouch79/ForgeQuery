package io.github.sambouch79.queryforge.api;

import java.util.List;

public class ValidateResponse {

    public boolean valid;
    public List<String> errors;

    public static ValidateResponse ok() {
        ValidateResponse r = new ValidateResponse();
        r.valid  = true;
        r.errors = List.of();
        return r;
    }

    public static ValidateResponse error(List<String> errors) {
        ValidateResponse r = new ValidateResponse();
        r.valid  = false;
        r.errors = errors;
        return r;
    }
}