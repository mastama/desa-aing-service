package com.yolifay.common;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Value("${service.id}")
    private String serviceId;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseService> handleValidation(MethodArgumentNotValidException ex) {
        var field = ex.getBindingResult().getFieldError();
        String msg = field != null ? field.getField()+": "+field.getDefaultMessage() : "Validation error";
        var body = ResponseUtil.setResponse(400, serviceId, CommonConstants.RESPONSE.BAD_REQUEST, Map.of("message", msg));
        return ResponseEntity.status(400).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResponseService> handleConstraint(ConstraintViolationException ex) {
        var body = ResponseUtil.setResponse(400, serviceId, CommonConstants.RESPONSE.BAD_REQUEST, Map.of("message", ex.getMessage()));
        return ResponseEntity.status(400).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseService> handleIllegalArg(IllegalArgumentException ex) {
        var body = ResponseUtil.setResponse(400, serviceId, CommonConstants.RESPONSE.BAD_REQUEST, Map.of("message", ex.getMessage()));
        return ResponseEntity.status(400).body(body);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseService> handleRuntime(RuntimeException ex) {
        var body = ResponseUtil.setResponse(500, serviceId, CommonConstants.RESPONSE.HTTP_INTERNAL_ERROR, Map.of("message", ex.getMessage()));
        return ResponseEntity.status(500).body(body);
    }
}
