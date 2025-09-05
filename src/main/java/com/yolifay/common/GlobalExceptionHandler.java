package com.yolifay.common;

import com.yolifay.application.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.net.ssl.SSLProtocolException;
import java.net.SocketTimeoutException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${service.id}")
    private String serviceId;

    @ExceptionHandler(value = { DataNotFoundException.class })
    public ResponseEntity<ResponseService> dataNotFoundException(DataNotFoundException e) {
        warnNoStack(e);
        ResponseService response = ResponseUtil.setResponse(HttpStatus.NOT_FOUND.value(), serviceId,
                CommonConstants.RESPONSE.ACCOUNT_NOT_FOUND.getCode(), e.getMessage(),
                null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ExceptionHandler(value = { RefreshStoreFailedException.class })
    public ResponseEntity<ResponseService> refreshStoreFailedException(RefreshStoreFailedException e) {
        warnNoStack(e);
        ResponseService response = ResponseUtil.setResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), serviceId,
                CommonConstants.RESPONSE.HTTP_INTERNAL_ERROR.getCode(), e.getMessage(),
                null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ExceptionHandler(value = { DataExistException.class })
    public ResponseEntity<ResponseService> dataExitsException(DataExistException e) {
        warnNoStack(e);
        ResponseService response = ResponseUtil.setResponse(HttpStatus.CONFLICT.value(), serviceId,
                CommonConstants.RESPONSE.DATA_EXISTS.getCode(), e.getMessage(),
                null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ExceptionHandler(value = { UserNotActiveException.class })
    public ResponseEntity<ResponseService> userNotActiveException(UserNotActiveException e) {
        warnNoStack(e);
        ResponseService response = ResponseUtil.setResponse(HttpStatus.BAD_REQUEST.value(), serviceId,
                CommonConstants.RESPONSE.BAD_REQUEST.getCode(), e.getMessage(),
                null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ExceptionHandler(value = { AccessUnauthorizedDeniedException.class })
    public ResponseEntity<ResponseService> accessUnauthorizedDeniedException(AccessUnauthorizedDeniedException e) {
        warnNoStack(e);
        ResponseService response = ResponseUtil.setResponse(HttpStatus.UNAUTHORIZED.value(), serviceId,
                CommonConstants.RESPONSE.UNAUTHORIZED.getCode(), e.getMessage(),
                null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ExceptionHandler(value = { SSLProtocolException.class, SocketTimeoutException.class })
    public ResponseEntity<ResponseService> timeoutErrorHandler(Exception e) {
        warnWithStack(e);
        ResponseService response = ResponseUtil.setResponse(HttpStatus.REQUEST_TIMEOUT.value(), serviceId,
                CommonConstants.RESPONSE.TRANSACTION_TIMEOUT.getCode(),
                e.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ExceptionHandler(value = { TooManyRequestsException.class })
    public ResponseEntity<ResponseService> tooManyRequest(TooManyRequestsException e) {
        warnWithStack(e);
        ResponseService response = ResponseUtil.setResponse(HttpStatus.TOO_MANY_REQUESTS.value(), serviceId,
                CommonConstants.RESPONSE.TOO_MANY_REQUESTS.getCode(),
                e.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ===== Helper methods (tanpa constant) =====

    private static void warnNoStack(Throwable e) {
        log.warn("{} : {}", e.getClass().getSimpleName(), e.getMessage());
    }

    private static void warnWithStack(Throwable e) {
        log.warn("{} : {}", e.getClass().getSimpleName(), e.getMessage(), e);
    }
}
