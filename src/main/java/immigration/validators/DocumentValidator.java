package immigration.validators;

import immigration.models.ValidationResult;
import java.util.regex.Pattern;

/**
 * Stateless validator for identity document numbers used on the document verification route.
 *
 * <ul>
 *   <li>Passport: {@code ^[A-Z0-9]{9}$} — 9 uppercase alphanumeric characters</li>
 *   <li>Permit: {@code ^[A-Z]{2}[0-9]{7}$} — 2 uppercase letters followed by 7 digits</li>
 * </ul>
 *
 */
public final class DocumentValidator {

    private static final Pattern PASSPORT = Pattern.compile("^[A-Z0-9]{9}$");
    private static final Pattern PERMIT   = Pattern.compile("^[A-Z]{2}[0-9]{7}$");

    private DocumentValidator() {}

    /**
     * Validates that {@code number} matches the passport format {@code ^[A-Z0-9]{9}$}.
     *
     * @param number passport number to validate
     * @return passing result on match; failing result with reason otherwise
     */
    public static ValidationResult validatePassport(String number) {
        if (number == null || !PASSPORT.matcher(number).matches()) {
            return ValidationResult.fail("Passport number must be 9 uppercase alphanumeric characters");
        }
        return ValidationResult.pass();
    }

    /**
     * Validates that {@code number} matches the biometric residence permit format
     * {@code ^[A-Z]{2}[0-9]{7}$}.
     *
     * @param number permit number to validate
     * @return passing result on match; failing result with reason otherwise
     */
    public static ValidationResult validatePermit(String number) {
        if (number == null || !PERMIT.matcher(number).matches()) {
            return ValidationResult.fail("Permit number must be 2 uppercase letters followed by 7 digits");
        }
        return ValidationResult.pass();
    }
}
