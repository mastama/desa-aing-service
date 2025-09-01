package com.yolifay.common;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseService> handleValidation(MethodArgumentNotValidException ex) {
        var fe = ex.getBindingResult().getFieldError(); String msg = fe!=null? fe.getField()+": "+fe.getDefaultMessage():"Validation error";
        var body = ResponseUtil.setResponse(400, "GEN", CommonConstants.RESPONSE.BAD_REQUEST, Map.of("message", msg));
        return ResponseEntity.status(400).body(body);
    }
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResponseService> handleConstraint(ConstraintViolationException ex){
        var body = ResponseUtil.setResponse(400, "GEN", CommonConstants.RESPONSE.BAD_REQUEST, Map.of("message", ex.getMessage()));
        return ResponseEntity.status(400).body(body);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseService> handleIllegalArg(IllegalArgumentException ex){
        var body = ResponseUtil.setResponse(400, "GEN", CommonConstants.RESPONSE.BAD_REQUEST, Map.of("message", ex.getMessage()));
        return ResponseEntity.status(400).body(body);
    }
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseService> handleRuntime(RuntimeException ex){
        var body = ResponseUtil.setResponse(500, "GEN", CommonConstants.RESPONSE.HTTP_INTERNAL_ERROR, Map.of("message", ex.getMessage()));
        return ResponseEntity.status(500).body(body);
    }
}
