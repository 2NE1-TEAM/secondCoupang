package com.toanyone.store.common.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;


@Transactional
class PhoneNumberUtilsTest {

    @Test
    @DisplayName("PhoneNumberUtils 전화번호 -제거 테스트")
    void TelValidationTest() {

        String normalizePhoneNumber1 = PhoneNumberUtils.normalizePhoneNumber("010-1234-5678");
        String normalizePhoneNumber2 = PhoneNumberUtils.normalizePhoneNumber("+82 10-1234-5678");
        String normalizePhoneNumber3 = PhoneNumberUtils.normalizePhoneNumber("02-123-4567");

        assertThat(normalizePhoneNumber1).isEqualTo("01012345678");
        assertThat(normalizePhoneNumber2).isEqualTo("+821012345678");
        assertThat(normalizePhoneNumber3).isEqualTo("021234567");
    }

    @Test
    @DisplayName("PhoneNumberUtils 전화번호 -추가 테스트")
    void TelValidationTest2() {
        String normalizePhoneNumber1 = PhoneNumberUtils.formatPhoneNumber("01012345678");
        String normalizePhoneNumber2 = PhoneNumberUtils.formatPhoneNumber("+821012345678");
        String normalizePhoneNumber3 = PhoneNumberUtils.formatPhoneNumber("021234567");

        assertThat(normalizePhoneNumber1).isEqualTo("010-1234-5678");
        assertThat(normalizePhoneNumber2).isEqualTo("010-1234-5678");
        assertThat(normalizePhoneNumber3).isEqualTo("02-123-4567");
    }
}