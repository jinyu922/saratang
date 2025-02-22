package com.swyp.saratang.data;

public enum PeriodType {
    YESTERDAY(1),
    WEEK(7),
    MONTH(30),
    YEAR(365);

    private final int days; // 일(day) 단위 값

    // 생성자
    PeriodType(int days) {
        this.days = days;
    }

    // Getter
    public int getDays() {
        return days;
    }

    // 대소문자 구분 없이 유효성 검증
    public static boolean isValid(String value) {
        for (PeriodType period : values()) {
            if (period.name().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
    
    public static PeriodType fromString(String value) {
        try {
            return PeriodType.valueOf(value.toUpperCase()); // 대소문자 구분 없이 처리
        } catch (IllegalArgumentException e) {
            return null; // 잘못된 값은 null 반환
        }
    }
}