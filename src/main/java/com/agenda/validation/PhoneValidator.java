 package com.agenda.validation;

import java.util.regex.Pattern;

public class PhoneValidator {
    // Accepts common international formats and local numbers with spaces, dashes, parentheses.
    // Examples: +55 11 99999-0001, (11) 99999-0001, 11999990001, 99999-0001
    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9 ()-]{7,25}$");

    public static boolean isValid(String phone) {
        if (phone == null) return false;
        String p = phone.trim();
        if (p.isEmpty()) return false;
        // basic pattern check
        if (!PHONE.matcher(p).matches()) return false;
        // ensure there are at least 7 digits
        int digits = 0;
        for (char c : p.toCharArray()) {
            if (Character.isDigit(c)) digits++;
        }
        return digits >= 7;
    }
}

