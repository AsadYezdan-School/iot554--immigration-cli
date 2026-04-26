package immigration.services;

import immigration.models.*;
import immigration.repositories.*;
import immigration.validators.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Core application service that orchestrates immigration status verification
 * via two independent routes: share-code and document.
 *
 * <p>Both routes enforce a lawful-purpose check (handled in the CLI layer before
 * this service is called), run a validation chain, query the relevant repositories,
 * and append an {@link immigration.models.AuditEvent} regardless of outcome.</p>
 *
 * <p>Returned {@link immigration.models.VerificationOutcome} subtypes are selected
 * based on the requesting organisation's role, ensuring minimum disclosure.</p>
 */
public class VerificationService {

    private static final Logger logger = LoggerFactory.getLogger(VerificationService.class);

    private final PersonRepository personRepo;
    private final VisaRepository visaRepo;
    private final ShareCodeRepository shareCodeRepo;
    private final OrganisationRepository orgRepo;
    private final AuditRepository auditRepo;

    private final String shareCodeVerification = "SHARE_CODE_VERIFICATION";
    private final String documentVerification = "DOCUMENT_VERIFICATION";

    public VerificationService(PersonRepository personRepo, VisaRepository visaRepo,
                               ShareCodeRepository shareCodeRepo,
                               OrganisationRepository orgRepo, AuditRepository auditRepo) {
        this.personRepo = personRepo;
        this.visaRepo = visaRepo;
        this.shareCodeRepo = shareCodeRepo;
        this.orgRepo = orgRepo;
        this.auditRepo = auditRepo;
    }

    /**
     * Verifies an individual's immigration status using a share code (individual-initiated route).
     *
     * <p>Validates organisation eligibility, code format, expiry, single-use status, date of
     * birth, and purpose before returning a role-appropriate outcome. Marks the code as used
     * on success and always writes an audit event.</p>
     *
     * @param orgId organisation ID performing the check
     * @param code  9-character share code provided by the individual
     * @param dob   date of birth supplied by the checker for identity confirmation (YYYY-MM-DD)
     * @return a {@link immigration.models.VerificationOutcome} appropriate to the organisation's role
     */
    public VerificationOutcome verifyByShareCode(String orgId, String code, String dob) {
        logger.info("Share code verification requested by org={}", orgId);

        var orgOpt = orgRepo.findById(orgId);
        if (orgOpt.isEmpty()) {
            return reject(orgId, shareCodeVerification, null, code, "Organisation not recognised");
        }
        var org = orgOpt.get();

        var roleResult = OrganisationValidator.validateRole(org);
        if (!roleResult.ok()) {
            return reject(orgId, shareCodeVerification, null, code, roleResult.reason());
        }

        var fmtResult = ShareCodeValidator.validateFormat(code);
        if (!fmtResult.ok()) {
            return reject(orgId, shareCodeVerification, null, code, fmtResult.reason());
        }

        var scOpt = shareCodeRepo.findByCode(code);
        if (scOpt.isEmpty()) {
            return reject(orgId, shareCodeVerification, null, code, "Share code not found");
        }
        var sc = scOpt.get();

        var expResult = ShareCodeValidator.validateNotExpired(sc);
        if (!expResult.ok()) {
            return reject(orgId, shareCodeVerification, null, code, expResult.reason());
        }

        var usedResult = ShareCodeValidator.validateNotUsed(sc);
        if (!usedResult.ok()) {
            return reject(orgId, shareCodeVerification, null, code, usedResult.reason());
        }

        var personOpt = personRepo.findById(sc.personId());
        if (personOpt.isEmpty()) {
            return reject(orgId, shareCodeVerification, null, code, "Person record not found");
        }
        var person = personOpt.get();

        var dobResult = DobValidator.validate(dob, person.dateOfBirth());
        if (!dobResult.ok()) {
            return reject(orgId, shareCodeVerification, maskId(person.id()), code, dobResult.reason());
        }

        var purposeResult = OrganisationValidator.validatePurpose(org, sc.purpose());
        if (!purposeResult.ok()) {
            return reject(orgId, shareCodeVerification, maskId(person.id()), code, purposeResult.reason());
        }

        var visaOpt = visaRepo.findByPersonId(person.id());
        if (visaOpt.isEmpty()) {
            return reject(orgId, shareCodeVerification, maskId(person.id()), code, "Visa record not found");
        }
        var visa = visaOpt.get();

        var expiry = LocalDate.parse(visa.expiryDate());
        VerificationOutcome outcome = switch (org.role()) {
            case "EMPLOYER", "EDUCATION" -> new VerificationOutcome.RightToWork(visa.rightToWork(), expiry);
            case "LANDLORD"              -> new VerificationOutcome.RightToRent(visa.rightToRent());
            default -> new VerificationOutcome.Rejected("Unsupported role for share code route");
        };

        shareCodeRepo.update(sc.markUsed());
        audit(orgId, shareCodeVerification, maskId(person.id()), code, "APPROVED", formatOutcome(outcome));
        logger.info("Share code verification outcome={} org={}", formatOutcome(outcome), orgId);
        return outcome;
    }

    /**
     * Verifies an individual's immigration status using a physical document
     * (authority-initiated route, restricted to BORDER_CONTROL and LAW_ENFORCEMENT).
     *
     * <p>Validates organisation role, document format, and person existence before
     * returning a role-appropriate outcome. Always writes an audit event; does not
     * consume or modify the document.</p>
     *
     * @param orgId   organisation ID performing the check
     * @param docNum  document number (passport or permit) to look up
     * @param docType {@code PASSPORT} or {@code PERMIT}
     * @return a {@link immigration.models.VerificationOutcome} appropriate to the organisation's role
     */
    public VerificationOutcome verifyByDocument(String orgId, String docNum, String docType) {
        logger.info("Document verification requested by org={} docType={}", orgId, docType);

        var orgOpt = orgRepo.findById(orgId);
        if (orgOpt.isEmpty()) {
            return reject(orgId, documentVerification, null, docNum, "Organisation not recognised");
        }
        var org = orgOpt.get();

        var roleResult = OrganisationValidator.validateForDocumentRoute(org);
        if (!roleResult.ok()) {
            return reject(orgId, documentVerification, null, docNum, roleResult.reason());
        }

        var docResult = "PASSPORT".equals(docType)
            ? DocumentValidator.validatePassport(docNum)
            : DocumentValidator.validatePermit(docNum);
        if (!docResult.ok()) {
            return reject(orgId, documentVerification, null, docNum, docResult.reason());
        }

        var personOpt = "PASSPORT".equals(docType)
            ? personRepo.findByPassportNumber(docNum)
            : personRepo.findByPermitNumber(docNum);
        if (personOpt.isEmpty()) {
            return reject(orgId, documentVerification, null, docNum, "Person not found for document");
        }
        var person = personOpt.get();

        var visaOpt = visaRepo.findByPersonId(person.id());
        if (visaOpt.isEmpty()) {
            return reject(orgId, documentVerification, maskId(person.id()), docNum, "Visa record not found");
        }
        var visa = visaOpt.get();

        var expiry = LocalDate.parse(visa.expiryDate());
        VerificationOutcome outcome = switch (org.role()) {
            case "BORDER_CONTROL"  -> new VerificationOutcome.EntryPermission(visa.entryPermitted(), visa.conditions());
            case "LAW_ENFORCEMENT" -> new VerificationOutcome.StatusValidity(
                visa.visaType(), expiry, !expiry.isBefore(LocalDate.now()));
            default -> new VerificationOutcome.Rejected("Unsupported role for document route");
        };

        audit(orgId, "DOCUMENT_VERIFICATION", maskId(person.id()), docNum, "APPROVED", formatOutcome(outcome));
        logger.info("Document verification outcome={} org={}", formatOutcome(outcome), orgId);
        return outcome;
    }

    /**
     * Records a {@code CONFIRMATION_REFUSED} audit event when an organisation declines
     * one of the mandatory lawful-purpose confirmations before a verification attempt.
     *
     * @param orgId organisation ID that refused the confirmation
     */
    public void auditConfirmationRefusal(String orgId) {
        audit(orgId, "CONFIRMATION_REFUSED", null, null, "REJECTED", "Confirmation refused by organisation");
        logger.warn("Confirmation refused by org={}", orgId);
    }

    /**
     * Formats a {@link immigration.models.VerificationOutcome} into a human-readable
     * string suitable for display in the CLI.
     *
     * @param o the outcome to format
     * @return formatted string representation of the outcome
     */
    public String formatOutcome(VerificationOutcome o) {
        return switch (o) {
            case VerificationOutcome.RightToWork r ->
                "Right to work: " + r.eligible() + ", expires: " + r.expiry();
            case VerificationOutcome.RightToRent r ->
                "Right to rent: " + r.eligible();
            case VerificationOutcome.EntryPermission e ->
                "Entry permitted: " + e.permitted() + ", conditions: " + e.conditions();
            case VerificationOutcome.StatusValidity s ->
                "Visa type: " + s.visaType() + ", valid: " + s.valid() + ", expires: " + s.expiry();
            case VerificationOutcome.Rejected r ->
                "REJECTED: " + r.reason();
        };
    }

    private VerificationOutcome reject(String orgId, String eventType, String maskedPersonId,
                                        String ref, String reason) {
        audit(orgId, eventType, maskedPersonId, ref, "REJECTED", reason);
        logger.warn("Rejected {} reason={} org={}", eventType, reason, orgId);
        return new VerificationOutcome.Rejected(reason);
    }

    private void audit(String orgId, String eventType, String maskedPersonId,
                        String ref, String outcome, String detail) {
        auditRepo.append(new AuditEvent(
            Instant.now().toString(), eventType, orgId, maskedPersonId, ref, outcome, detail));
    }

    private static String maskId(String personId) {
        if (personId == null || personId.length() < 4) return "****";
        return personId.substring(0, 4) + "****";
    }
}
