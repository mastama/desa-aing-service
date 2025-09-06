package com.yolifay.infrastructure.adapter.in.web;

import com.yolifay.application.query.ListUsersQuery;
import com.yolifay.application.query.handler.ListUsersHandler;
import com.yolifay.common.CommonConstants;
import com.yolifay.common.ResponseService;
import com.yolifay.common.ResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

    private final ListUsersHandler listUsersHandler;

    @Value("${service.id}")
    private String serviceId;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ResponseService> list(
            @RequestParam(required=false) String q,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size
    ) {
        var result = listUsersHandler.handleListUser(new ListUsersQuery(q, page, size));
        log.info("q={}, class={}", q, (q != null ? q.getClass().getName() : "null"));
        return ResponseEntity.ok(ResponseUtil.setResponse(
                HttpStatus.OK.value(), serviceId, CommonConstants.RESPONSE.APPROVED, result)
        );
    }
}
