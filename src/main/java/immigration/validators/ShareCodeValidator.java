package immigration.validators;

import immigration.models.ShareCode;
import immigration.models.ValidationResult;
import java.time.Instant;
import java.util.regex.Pattern;

public final class ShareCodeValidator {

    private static final Pattern FORMAT = Pattern.compile("^[A-Z0-9]{9}$");

    private ShareCodeValidator() {}

    public static ValidationResult validateFormat(String code) {
        if (code == null || !FORMAT.matcher(code).matches()) {
            return ValidationResult.fail("Share code must be 9 uppercase alphanumeric characters");
        }
        return ValidationResult.pass();
    }

    public static ValidationResult validateNotExpired(ShareCode sc) {
        var expiry = Instant.parse(sc.expiresAt());
        if (Instant.now().isAfter(expiry)) {
            return ValidationResult.fail("Share code has expired");
        }
        return ValidationResult.pass();
    }
}
