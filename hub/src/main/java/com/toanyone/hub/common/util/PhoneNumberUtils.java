package com.toanyone.hub.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhoneNumberUtils {
    public static String normalizePhoneNumber(String phoneNumber) {
        return phoneNumber.replaceAll("[^0-9+]", "");
    }

    public static String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber.startsWith("+82")) {  // 한국 국제번호
            phoneNumber = phoneNumber.replaceFirst("\\+82", "0"); // +82 → 0으로 변환
        }

        if (phoneNumber.startsWith("02")) {  // 서울 지역번호
            if (phoneNumber.length() == 9) { // 02-XXX-XXXX
                return phoneNumber.replaceFirst("(\\d{2})(\\d{3})(\\d{4})", "$1-$2-$3");
            } else if (phoneNumber.length() == 10) { // 02-XXXX-XXXX
                return phoneNumber.replaceFirst("(\\d{2})(\\d{4})(\\d{4})", "$1-$2-$3");
            }
        } else if (phoneNumber.length() == 10) {  // 일반 지역번호 (031, 051 등)
            return phoneNumber.replaceFirst("(\\d{3})(\\d{3})(\\d{4})", "$1-$2-$3");
        } else if (phoneNumber.length() == 11) {  // 휴대폰 번호
            return phoneNumber.replaceFirst("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
        }

        return phoneNumber; // 변환 불가능하면 원본 반환
    }
}