package com.yolifay.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommonConstants {

    @Getter
    public enum RESPONSE {
        APPROVED("00", "Approved"),
        CREATED("201", "Created"),

        BAD_REQUEST("400", "Permintaan tidak valid"),
        UNAUTHORIZED("401", "Unauthorized"),
        FORBIDDEN("403", "Forbidden"),
        HTTP_NOT_FOUND("404", "There is No Resource Path"),

        DATA_EXISTS("15", "Data sudah ada"),
        ACCOUNT_NOT_FOUND("14", "Data tidak ditemukan"),
        INVALID_CREDENTIALS("51", "Username/Password salah"),

        HTTP_INTERNAL_ERROR("X5", "Service Internal Error");

        private final String code;
        private final String description;

        RESPONSE(String code, String description) {
            this.code = code;
            this.description = description;
        }

    }
}