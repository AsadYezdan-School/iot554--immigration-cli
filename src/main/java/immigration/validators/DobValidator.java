package immigration.validators;

import immigration.models.ValidationResult;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Stateless validator that checks whether a supplied date of birth matches
 * the date of birth held on the person's record.
 *
 */
public final class DobValidator {

    private DobValidator() {}

    /**
     * Validates that {@code inputDob} is a well-formed {@code YYYY-MM-DD} date and
     * matches the person's stored date of birth exactly.
     *
     * @param inputDob  date of birth entered by the checking organisation
     * @param personDob date of birth stored on the person record ({@code YYYY-MM-DD})
     * @return a passing result on exact match; a failing result describing the mismatch otherwise
     */
    public static ValidationResult validate(String inputDob, String personDob) {
        if (inputDob == null || inputDob.isBlank()) {
            return ValidationResult.fail("Date of birth must not be blank");
        }
        LocalDate parsed;
        try {
            parsed = LocalDate.parse(inputDob.trim());
        } catch (DateTimeParseException e) {
            return ValidationResult.fail("Date of birth must be in YYYY-MM-DD format");
        }
        LocalDate expected;
        try {
            expected = LocalDate.parse(personDob);
        } catch (DateTimeParseException e) {
            return ValidationResult.fail("Person record contains invalid date of birth");
        }
        if (!parsed.equals(expected)) {
            return ValidationResult.fail("Date of birth does not match");
        }
        return ValidationResult.pass();
    }
}
