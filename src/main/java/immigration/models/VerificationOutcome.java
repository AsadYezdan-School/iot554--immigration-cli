package immigration.models;

import java.time.LocalDate;
import java.util.List;

/**
 * Sealed interface representing the outcome of an immigration status verification.
 *
 * <p>Each permitted subtype carries only the information appropriate for the
 * requesting organisation's role, enforcing minimum-disclosure principles:</p>
 * <ul>
 *   <li>{@link RightToWork} — returned to {@code EMPLOYER} and {@code EDUCATION} organisations</li>
 *   <li>{@link RightToRent} — returned to {@code LANDLORD} organisations</li>
 *   <li>{@link EntryPermission} — returned to {@code BORDER_CONTROL} organisations</li>
 *   <li>{@link StatusValidity} — returned to {@code LAW_ENFORCEMENT} organisations</li>
 *   <li>{@link Rejected} — returned when any validation step fails</li>
 * </ul>
 */
public sealed interface VerificationOutcome
        permits VerificationOutcome.RightToWork,
                VerificationOutcome.RightToRent,
                VerificationOutcome.EntryPermission,
                VerificationOutcome.StatusValidity,
                VerificationOutcome.Rejected {

    /**
     * Outcome for employer and education organisations: indicates whether the
     * individual has the right to work and when that right expires.
     *
     * @param eligible {@code true} if the individual may legally work
     * @param expiry   date on which the right to work expires
     */
    record RightToWork(boolean eligible, LocalDate expiry) implements VerificationOutcome {}

    /**
     * Outcome for landlord organisations: indicates whether the individual has
     * the right to rent residential property.
     *
     * @param eligible {@code true} if the individual may legally rent
     */
    record RightToRent(boolean eligible) implements VerificationOutcome {}

    /**
     * Outcome for border control organisations: indicates whether the individual
     * is permitted to enter and lists any associated conditions.
     *
     * @param permitted  {@code true} if entry is permitted
     * @param conditions any conditions attached to the entry permission (may be empty)
     */
    record EntryPermission(boolean permitted, List<String> conditions) implements VerificationOutcome {}

    /**
     * Outcome for law enforcement organisations: provides full visa status details.
     *
     * @param visaType category of visa held (e.g. {@code WORK}, {@code VISITOR})
     * @param expiry   date on which the visa expires
     * @param valid    {@code true} if the visa has not yet expired
     */
    record StatusValidity(String visaType, LocalDate expiry, boolean valid) implements VerificationOutcome {}

    /**
     * Outcome returned when a verification attempt is rejected at any validation step.
     *
     * @param reason human-readable explanation of the rejection
     */
    record Rejected(String reason) implements VerificationOutcome {}
}
