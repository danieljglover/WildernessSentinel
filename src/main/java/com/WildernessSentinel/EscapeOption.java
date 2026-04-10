package com.WildernessSentinel;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class EscapeOption {
    public enum EscapeType { TELEPORT, RUN }

    private final EscapeType type;
    private final String description;
    private final boolean teleblocked;
}
