package com.dobbinsoft.gus.remotesales.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

/**
 * describe:
 *
 * @author yanziyu
 * @date 2025/5/21 13:18
 */
public class DateUtils {

    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private DateUtils() {
    }

    public static LocalDateTime dateToLocalDateTime(Date date) {
        if (date == null) {
            return null;
        } else {
            Instant instant = date.toInstant();
            ZoneId zoneId = ZoneId.systemDefault();
            return instant.atZone(zoneId).toLocalDateTime();
        }
    }

    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        } else {
            ZoneId zoneId = ZoneId.systemDefault();
            ZonedDateTime zdt = localDateTime.atZone(zoneId);
            return Date.from(zdt.toInstant());
        }
    }

    public static Date localDateTimeToDate(ZonedDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        } else {
            ZonedDateTime zdt = localDateTime;
            return Date.from(zdt.toInstant());
        }
    }

    public static LocalDateTime getStartTimeOfDay(LocalDate date) {
        return LocalDateTime.of(date, LocalDateTime.MIN.toLocalTime());
    }

    public static LocalDateTime getEndTimeOfDay(LocalDate date) {
        return LocalDateTime.of(date, LocalDateTime.MAX.toLocalTime());
    }

    public static LocalTime stringToLocalTime(String timeString) {
        return stringToLocalTime(timeString, TIME_FORMAT);
    }

    public static LocalTime stringToLocalTime(String timeString, String format) {
        if (timeString != null && !timeString.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            return LocalTime.parse(timeString, formatter);
        } else {
            return null;
        }
    }

    public static LocalDate stringToLocalDate(String dateStr, String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);

        try {
            return LocalDate.parse(dateStr, formatter);
        } catch (DateTimeParseException var4) {
            return null;
        }
    }

    public static LocalDateTime stringToLocalDateTime(String timeString, String format) {
        if (timeString != null && !timeString.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            return LocalDateTime.parse(timeString, formatter);
        } else {
            return null;
        }
    }

    public static String localDateTimeToString(LocalDateTime localDateTime) {
        return localDateTimeToString(localDateTime, DATE_TIME_FORMAT);
    }

    public static String localDateTimeToString(LocalDateTime localDateTime, String format) {
        if (localDateTime == null) {
            return null;
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            return localDateTime.format(formatter);
        }
    }
}
