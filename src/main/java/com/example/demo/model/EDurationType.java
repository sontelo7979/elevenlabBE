package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;
@Getter
@AllArgsConstructor
public enum EDurationType {
    ONE_MONTH("1MONTH", Duration.ofDays(30)),
    THREE_MONTHS("3MONTHS", Duration.ofDays(90)),
    ONE_YEAR("1YEAR", Duration.ofDays(365));

    private final String code;
    private final Duration duration;
}
