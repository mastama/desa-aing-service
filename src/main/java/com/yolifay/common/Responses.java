package com.yolifay.common;

import org.springframework.http.ResponseEntity;

public final class Responses {
    private Responses() {}

    public static ResponseEntity<ResponseService> ok(Object data) {
        return ResponseEntity.ok(ResponseService.of(Constant.RESPONSE.APPROVED, data));
    }

    public static ResponseEntity<ResponseService> created(Object data) {
        return ResponseEntity.status(201).body(ResponseService.of(Constant.RESPONSE.CREATED, data));
    }

    public static ResponseEntity<ResponseService> error(Constant.RESPONSE r, int status) {
        return ResponseEntity.status(status).body(ResponseService.of(r, null));
    }
}
