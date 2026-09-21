package org.example.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleNoEncontrado(RecursoNoEncontradoException ex) {
        log.error("Recurso no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
            "timestamp", LocalDateTime.now().toString(),
            "status", HttpStatus.NOT_FOUND.value(),
            "error", "Recurso no encontrado",
            "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<Map<String, Object>> handleStockInsuficiente(StockInsuficienteException ex) {
        log.error("Stock insuficiente: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
            "timestamp", LocalDateTime.now().toString(),
            "status", HttpStatus.CONFLICT.value(),
            "error", "Stock insuficiente",
            "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        log.error("Error de negocio: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "timestamp", LocalDateTime.now().toString(),
            "status", HttpStatus.BAD_REQUEST.value(),
            "error", "Error de negocio",
            "message", ex.getMessage()
        ));
    }
}
