package me.zedaster.moderationservice;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class TestUtils {
    /**
     * Creates an instance of {@link Instant}.
     * <br/>
     * For example, createDate(2024, 1, 1, 17, 40, 0) returns Date instance of January 1st 2024 17:40:00 UTC.
     * @param year Year
     * @param month Month
     * @param day Day
     * @param hour Hour
     * @param minute Minute
     * @param second Second
     * @return Date
     */
    public static Instant createInstantOf(int year, int month, int day, int hour, int minute, int second) {
        LocalDateTime localDateTime = LocalDateTime.of(year, month, day, hour, minute, second);
        ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, Clock.systemUTC().getZone());
        return zonedDateTime.toInstant();
    }
}
