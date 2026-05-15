package immigration.validators;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DocumentValidatorTest {

    // --- validatePassport ---

    @Test
    void validPassport_passes() {
        assertTrue(DocumentValidator.validatePassport("AB1234567").ok());
    }

    @Test
    void passportTooShort_fails() {
        assertFalse(DocumentValidator.validatePassport("AB123456").ok());
    }

    @Test
    void passportTooLong_fails() {
        assertFalse(DocumentValidator.validatePassport("AB12345678").ok());
    }

    @Test
    void passportLowercase_fails() {
        assertFalse(DocumentValidator.validatePassport("ab1234567").ok());
    }

    @Test
    void passportWithSpace_fails() {
        assertFalse(DocumentValidator.validatePassport("AB 234567").ok());
    }

    @Test
    void passportNull_fails() {
        assertFalse(DocumentValidator.validatePassport(null).ok());
    }

    // --- validatePermit ---

    @Test
    void validPermit_passes() {
        assertTrue(DocumentValidator.validatePermit("CD1234567").ok());
    }

    @Test
    void permitAllDigits_fails() {
        assertFalse(DocumentValidator.validatePermit("123456789").ok());
    }

    @Test
    void permitOnly6Digits_fails() {
        assertFalse(DocumentValidator.validatePermit("CD123456").ok());
    }

    @Test
    void permitThreeLeadingLetters_fails() {
        assertFalse(DocumentValidator.validatePermit("CDE234567").ok());
    }

    @Test
    void permitLowercasePrefix_fails() {
        assertFalse(DocumentValidator.validatePermit("cd1234567").ok());
    }

    @Test
    void permitNull_fails() {
        assertFalse(DocumentValidator.validatePermit(null).ok());
    }
}
