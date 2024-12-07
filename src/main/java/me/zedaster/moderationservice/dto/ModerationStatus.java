package me.zedaster.moderationservice.dto;

import lombok.Getter;

@Getter
public enum ModerationStatus {
    MODERATING(0),
    EDIT_REQUESTED(1);

    private static final ModerationStatus[] values = values();

    private final int id;

    ModerationStatus(int id) {
        this.id = id;
    }

    public static ModerationStatus getById(int id) {
        return values[id];
    }
}
