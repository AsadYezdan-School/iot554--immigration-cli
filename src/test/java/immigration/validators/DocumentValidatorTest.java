package immigration.validators;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DocumentValidatorTest {

    // --- validatePassport ---

    @Test
    void validPassport_passes() {
        // EP: valid 9-char uppercase alphanumeric
        assertTrue(DocumentValidator.validatePassport("AB1234567").ok());
    }

    @Test
    void passportTooShort_fails() {
        // BVA: 8 chars (boundary below 9)
        assertFalse(DocumentValidator.validatePassport("AB123456").ok());
    }

    @Test
    void passportTooLong_fails() {
        // BVA: 10 chars (boundary above 9)
        assertFalse(DocumentValidator.validatePassport("AB12345678").ok());
    }

    @Test
    void passportLowercase_fails() {
        // EP: lowercase letters are invalid class
        assertFalse(DocumentValidator.validatePassport("ab1234567").ok());
    }

    @Test
    void passportWithSpace_fails() {
        // EP: spaces not in valid character class
        assertFalse(DocumentValidator.validatePassport("AB 234567").ok());
    }

    @Test
    void passportNull_fails() {
        // EP: null input
        assertFalse(DocumentValidator.validatePassport(null).ok());
    }

    // --- validatePermit ---

    @Test
    void validPermit_passes() {
        // EP: 2 uppercase letters + 7 digits
        assertTrue(DocumentValidator.validatePermit("CD1234567").ok());
    }

    @Test
    void permitAllDigits_fails() {
        // EP: no leading letters
        assertFalse(DocumentValidator.validatePermit("123456789").ok());
    }

    @Test
    void permitOnly6Digits_fails() {
        // BVA: 2 letters + 6 digits = 8 chars (boundary below 9)
        assertFalse(DocumentValidator.validatePermit("CD123456").ok());
    }

    @Test
    void permitThreeLeadingLetters_fails() {
        // EP: 3 leading letters violates the 2-letter rule
        assertFalse(DocumentValidator.validatePermit("CDE234567").ok());
    }

    @Test
    void permitLowercasePrefix_fails() {
        // EP: lowercase letters invalid
        assertFalse(DocumentValidator.validatePermit("cd1234567").ok());
    }

    @Test
    void permitNull_fails() {
        // EP: null input
        assertFalse(DocumentValidator.validatePermit(null).ok());
    }
}
