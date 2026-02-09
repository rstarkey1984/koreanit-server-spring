package com.koreanit.spring.common.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.koreanit.spring.common.response.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private String origin(Throwable e) {
    StackTraceElement[] trace = e.getStackTrace();
    if (trace == null || trace.length == 0)
      return "unknown";

    // 우리 코드 위치를 우선으로 찍는다
    for (StackTraceElement el : trace) {
      if (el.getClassName().startsWith("com.koreanit.")) {
        return el.getClassName() + ":" + el.getLineNumber();
      }
    }

    // fallback: 첫 프레임
    StackTraceElement top = trace[0];
    return top.getClassName() + ":" + top.getLineNumber();
  }

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException e) {
    ErrorCode code = e.getErrorCode();

    log.warn("[API_ERROR] code={} message=\"{}\" origin={}",
        code.name(), e.getMessage(), origin(e));

    return ResponseEntity
        .status(code.getStatus())
        .body(ApiResponse.fail(code.name(), e.getMessage()));
  }

  // 핵심: 중복 제약 위반은 409로 매핑하고 warn 1줄만 찍는다
  @ExceptionHandler(DuplicateKeyException.class)
  public ResponseEntity<ApiResponse<Void>> handleDuplicateKey(DuplicateKeyException e) {
    log.warn("[DUPLICATE_KEY] message=\"{}\" origin={}",
        "중복된 값으로 인해 저장할 수 없습니다", origin(e));

    return ResponseEntity
        .status(ErrorCode.DUPLICATE_RESOURCE.getStatus())
        .body(ApiResponse.fail(
            ErrorCode.DUPLICATE_RESOURCE.name(),
            "이미 존재하는 사용자입니다"));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiResponse<Void>> handleBodyMissing(HttpMessageNotReadableException e) {
    log.warn("[INVALID_BODY] origin={}", origin(e));

    return ResponseEntity
        .status(ErrorCode.INVALID_REQUEST.getStatus())
        .body(ApiResponse.fail(
            ErrorCode.INVALID_REQUEST.name(),
            "요청 바디가 비어 있거나 형식이 올바르지 않습니다"));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
    FieldError error = e.getBindingResult().getFieldError();

    String message = (error != null && error.getDefaultMessage() != null && !error.getDefaultMessage().isBlank())
        ? error.getDefaultMessage()
        : "요청 값이 올바르지 않습니다";

    log.warn("[VALIDATION_FAIL] message=\"{}\" origin={}", message, origin(e));

    return ResponseEntity
        .status(ErrorCode.INVALID_REQUEST.getStatus())
        .body(ApiResponse.fail(ErrorCode.INVALID_REQUEST.name(), message));
  }

  @ExceptionHandler({ AuthorizationDeniedException.class, AccessDeniedException.class })
  public ResponseEntity<ApiResponse<Void>> handleForbidden(Exception e) {

    log.warn("[{}] message=\"{}\" origin={}", e.getClass().getName(),
        e.getMessage(), origin(e));

    return ResponseEntity
        .status(ErrorCode.FORBIDDEN.getStatus())
        .body(ApiResponse.fail(
            ErrorCode.FORBIDDEN.name(),
            "권한이 없습니다"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
    // 5xx만 스택트레이스 출력
    log.error("[INTERNAL_ERROR] origin={}", origin(e), e);

    return ResponseEntity
        .status(ErrorCode.INTERNAL_ERROR.getStatus())
        .body(ApiResponse.fail(ErrorCode.INTERNAL_ERROR.name(), "서버 오류"));
  }
}
