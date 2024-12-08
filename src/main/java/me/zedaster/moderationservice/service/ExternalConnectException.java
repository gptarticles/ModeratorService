package me.zedaster.moderationservice.service;

public class ExternalConnectException extends RuntimeException {
    public ExternalConnectException(String message, Throwable cause) {
        super(message, cause);
    }
}
