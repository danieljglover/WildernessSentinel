package com.WildernessSentinel;

import java.awt.Color;

public class PkerTierColors {

    public static Color getHullColor(int reportCount, WildernessSentinelConfig config) {
        if (reportCount >= config.tierHighThreshold()) return config.tierHighColor();
        if (reportCount >= config.tierMediumThreshold()) return config.tierMediumColor();
        return config.tierLowColor();
    }

    public static Color getTextColor(int reportCount, WildernessSentinelConfig config) {
        Color hull = getHullColor(reportCount, config);
        // Return an opaque version of the hull color for text
        return new Color(hull.getRed(), hull.getGreen(), hull.getBlue());
    }

    public static Color getDotColor(int reportCount, WildernessSentinelConfig config) {
        Color hull = getHullColor(reportCount, config);
        return new Color(hull.getRed(), hull.getGreen(), hull.getBlue());
    }
}
