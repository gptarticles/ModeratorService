package me.zedaster.moderationservice.controller;

import lombok.extern.slf4j.Slf4j;
import me.zedaster.moderationservice.dto.ErrorDto;
import me.zedaster.moderationservice.service.NoSuchArticleException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle access denied exceptions.
     * @param exception The instance of the exception.
     * @return Json with error message from the exception.
     */
    @ExceptionHandler(NoAccessException.class)
    public ResponseEntity<ErrorDto> handleNoAccessException(NoAccessException exception) {
        return new ResponseEntity<>(new ErrorDto(exception.getMessage()), HttpStatus.FORBIDDEN);
    }

    /**
     * Handle article not found exceptions.
     * @param exception The instance of the exception.
     * @return Json with error message from the exception.
     */
    @ExceptionHandler(NoSuchArticleException.class)
    public ResponseEntity<ErrorDto> handleNotFoundException(NoSuchArticleException exception) {
        return new ResponseEntity<>(new ErrorDto(exception.getMessage()), HttpStatus.NOT_FOUND);
    }

    /**
     * Handle internal server error exceptions.
     * @param exception The instance of the exception.
     * @return Json with error message from the exception.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleOtherException(Exception exception) {
        log.error("Internal server error", exception);
        return new ResponseEntity<>(new ErrorDto("Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
