package ru.javaops.cloudjava.menuservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DataIntegrityViolationException extends RuntimeException {
    private final HttpStatus status;

    public DataIntegrityViolationException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
