package com.airfitness.airfitness.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;

public final class CookieUtil {

    private CookieUtil() {}

    public static void addRefreshCookie(HttpServletResponse res, String value) {
        Cookie c = new Cookie("refreshToken", value);
        c.setHttpOnly(true);
        c.setSecure(true); // enable in prod
        c.setPath("/auth");
        c.setMaxAge(7 * 24 * 3600);
        res.addCookie(c);
    }

    public static String get(HttpServletRequest req, String name) {
        if (req.getCookies() == null) return null;

        return Arrays.stream(req.getCookies())
                .filter(c -> c.getName().equals(name))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    public static void clear(HttpServletResponse res, String name) {
        Cookie c = new Cookie(name, "");
        c.setPath("/auth");
        c.setMaxAge(0);
        res.addCookie(c);
    }
}

