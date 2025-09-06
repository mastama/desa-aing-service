package com.yolifay.application.query;

public record ListUsersQuery(String q, int page, int size) {
    public ListUsersQuery { if (page < 0) page = 0; if (size <= 0 || size > 200) size = 20; }
}
