package immigration.models;

/**
 * Outcome of a single validation check performed by a validator class.
 *
 * @param ok     {@code true} if the validation passed, {@code false} otherwise
 * @param reason human-readable explanation when {@code ok} is {@code false}; {@code null} on success
 */
public record ValidationResult(boolean ok, String reason) {

    /**
     * Returns a successful validation result.
     *
     * @return result with {@code ok = true}
     */
    public static ValidationResult pass() {
        return new ValidationResult(true, null);
    }

    /**
     * Returns a failed validation result with an explanatory reason.
     *
     * @param reason description of why the validation failed
     * @return result with {@code ok = false}
     */
    public static ValidationResult fail(String reason) {
        return new ValidationResult(false, reason);
    }
}
