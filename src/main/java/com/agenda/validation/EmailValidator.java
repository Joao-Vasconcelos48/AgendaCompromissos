package com.agenda.validation;

import java.util.regex.Pattern;

public class EmailValidator {
    // Simplified, practical email regex (not full RFC5322)
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /**
     * Returns true if the given email is non-null, non-empty and matches a common email pattern.
     */
    public static boolean isValid(String email) {
        if (email == null) return false;
        String e = email.trim();
        if (e.isEmpty()) return false;
        return EMAIL.matcher(e).matches();
    }
}

