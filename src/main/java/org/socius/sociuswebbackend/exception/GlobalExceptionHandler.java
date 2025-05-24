package org.socius.sociuswebbackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@ControllerAdvice // Annotation này cho phép xử lý các ngoại lệ toàn cục trong ứng dụng
public class GlobalExceptionHandler {

    /**
     * Xử lý ngoại lệ MethodArgumentNotValidException( lỗi xác thực đầu vào)
     * @param ex lỗi xảy ra
     * @return ResponseEntity chứa thông tin lỗi
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Validation failed");
        response.put("error", Objects.requireNonNull(ex.getBindingResult().getFieldError()).getDefaultMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
