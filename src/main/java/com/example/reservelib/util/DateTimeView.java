package com.example.reservelib.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class DateTimeView {
    private static final ZoneId MOSCOW_ZONE = ZoneId.of("Europe/Moscow");
    private static final DateTimeFormatter HOUR_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:00 'МСК'");

    private DateTimeView() {
    }

    public static String moscowHour(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault())
                .withZoneSameInstant(MOSCOW_ZONE)
                .format(HOUR_FORMAT);
    }
}
