package com.WildernessSentinel;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PkerDatabaseMode {
    COMMUNITY("Community"),
    MY_REPORTS("My Reports");

    private final String name;

    @Override
    public String toString() {
        return name;
    }
}
