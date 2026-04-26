package immigration.validators;

import immigration.models.ShareCode;
import immigration.models.ValidationResult;
import java.time.Instant;
import java.util.regex.Pattern;

/**
 * Stateless validator for share codes used on the individual-initiated verification route.
 *
 * <p>Checks code format, expiry, and single-use status. This class is not instantiable;
 * all members are static.</p>
 */
public final class ShareCodeValidator {

    private static final Pattern FORMAT = Pattern.compile("^[A-Z0-9]{9}$");

    private ShareCodeValidator() {}

    /**
     * Validates that {@code code} matches the required format {@code ^[A-Z0-9]{9}$}.
     *
     * @param code the share code string to validate
     * @return passing result on match; failing result otherwise
     */
    public static ValidationResult validateFormat(String code) {
        if (code == null || !FORMAT.matcher(code).matches()) {
            return ValidationResult.fail("Share code must be 9 uppercase alphanumeric characters");
        }
        return ValidationResult.pass();
    }

    /**
     * Validates that the share code has not passed its expiry instant.
     *
     * @param sc the share code to check
     * @return passing result if still valid; failing result if expired
     */
    public static ValidationResult validateNotExpired(ShareCode sc) {
        var expiry = Instant.parse(sc.expiresAt());
        if (Instant.now().isAfter(expiry)) {
            return ValidationResult.fail("Share code has expired");
        }
        return ValidationResult.pass();
    }

    /**
     * Validates that the share code has not already been consumed by a prior verification.
     *
     * @param sc the share code to check
     * @return passing result if unused; failing result if already used
     */
    public static ValidationResult validateNotUsed(ShareCode sc) {
        if (sc.used()) {
            return ValidationResult.fail("Share code has already been used");
        }
        return ValidationResult.pass();
    }
}
