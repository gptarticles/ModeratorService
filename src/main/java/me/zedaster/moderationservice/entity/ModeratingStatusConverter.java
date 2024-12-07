package me.zedaster.moderationservice.entity;

import jakarta.persistence.AttributeConverter;
import me.zedaster.moderationservice.dto.ModerationStatus;

// Converter to convert ModeratingStatus to number and vice versa
public class ModeratingStatusConverter implements AttributeConverter<ModerationStatus, Integer> {
    @Override
    public Integer convertToDatabaseColumn(ModerationStatus status) {
        return status.getId();
    }

    @Override
    public ModerationStatus convertToEntityAttribute(Integer statusId) {
        return ModerationStatus.getById(statusId);
    }
}
