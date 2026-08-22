package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EPermission {
    TEXT_TO_VOICE("TEXT_TO_VOICE"),
    VIDEO_DUBBING("VIDEO_DUBBING"),
    MINIMAX_CLONE_VOICE("MINIMAX_CLONE_VOICE"),
    AUTO_DUBBING("AUTO_DUBBING");

    private final String code;
}