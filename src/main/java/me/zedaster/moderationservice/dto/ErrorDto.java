package me.zedaster.moderationservice.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ErrorDto {
    public final String message;
}
