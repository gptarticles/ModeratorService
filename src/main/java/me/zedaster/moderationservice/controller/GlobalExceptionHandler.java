package me.zedaster.moderationservice.controller;

import me.zedaster.moderationservice.dto.ErrorDto;
import me.zedaster.moderationservice.service.ArticleNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
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
    @ExceptionHandler(ArticleNotFoundException.class)
    public ResponseEntity<ErrorDto> handleNotFoundException(ArticleNotFoundException exception) {
        return new ResponseEntity<>(new ErrorDto(exception.getMessage()), HttpStatus.NOT_FOUND);
    }
}
