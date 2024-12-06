package me.zedaster.moderationservice.dto;

public enum Role {
    USER(false),
    MODERATOR(true);

    private final boolean canModerate;

    Role(boolean canModerate) {
        this.canModerate = canModerate;
    }

    public boolean canModerate() {
        return canModerate;
    }
}
