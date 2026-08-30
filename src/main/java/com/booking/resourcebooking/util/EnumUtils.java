package com.booking.resourcebooking.util;

import com.booking.resourcebooking.exception.BadRequestException;

public class EnumUtils {

    private EnumUtils() {
        // Utility class
    }

    public static <E extends Enum<E>> E parseEnum(Class<E> enumClass, String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new BadRequestException(fieldName + " is required");
        }
        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid " + fieldName.toLowerCase() + ": " + value);
        }
    }
}
