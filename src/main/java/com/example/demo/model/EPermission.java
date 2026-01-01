package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EPermission {
    // Admin permissions
    TEXT_TO_VOICE("TEXT_TO_VOICE"),
    VIDEO_DUBBING("VIDEO_DUBBING"),
    MINIMAX_CLONE_VOICE("MINIMAX_CLONE_VOICE");
    private final String code;
}