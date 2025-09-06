package com.yolifay.infrastructure.adapter.in.web;

import com.yolifay.common.CommonConstants;
import com.yolifay.common.ResponseService;
import com.yolifay.common.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/actuator")
@RequiredArgsConstructor
public class AdminActuatorController {

    @Value("${service.id}")
    private String serviceId;

    private final HealthEndpoint healthEndpoint;
    private final MetricsEndpoint metricsEndpoint;

    @GetMapping("/health")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ResponseService> health() {
        var health = healthEndpoint.health();
        return ResponseEntity.ok(ResponseUtil.setResponse(
                HttpStatus.OK.value(), serviceId, CommonConstants.RESPONSE.APPROVED, health
        ));
    }

    @GetMapping("/metrics")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ResponseService> listMetrics() {
        var list = metricsEndpoint.listNames();
        return ResponseEntity.ok(ResponseUtil.setResponse(
                HttpStatus.OK.value(), serviceId, CommonConstants.RESPONSE.APPROVED, list
        ));
    }

    @GetMapping("/metrics/{name}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ResponseService> metric(@PathVariable String name,
                                                  @RequestParam(required=false) Map<String,String> tags) {
        var tagList = (tags == null || tags.isEmpty())
                ? List.<String>of()
                : tags.entrySet().stream()
                .map(e -> e.getKey() + ":" + e.getValue())
                .toList();
        var sample = metricsEndpoint.metric(name, tagList);
        return ResponseEntity.ok(ResponseUtil.setResponse(
                HttpStatus.OK.value(), serviceId, CommonConstants.RESPONSE.APPROVED, sample
        ));
    }

}