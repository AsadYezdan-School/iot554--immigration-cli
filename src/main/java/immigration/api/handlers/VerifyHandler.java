package immigration.api.handlers;

import immigration.api.dto.DocumentVerifyRequest;
import immigration.api.dto.ErrorResponse;
import immigration.api.dto.ShareCodeVerifyRequest;
import immigration.api.dto.VerificationResponse;
import immigration.models.Organisation;
import immigration.models.VerificationOutcome;
import immigration.services.VerificationService;
import io.javalin.http.Context;

public class VerifyHandler {

    private final VerificationService verificationService;

    public VerifyHandler(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    public void shareCode(Context ctx) {
        var req = ctx.bodyAsClass(ShareCodeVerifyRequest.class);
        var org = ctx.<Organisation>attribute("org");

        if (req.shareCode() == null || req.dateOfBirth() == null) {
            ctx.status(400).json(new ErrorResponse(400, "Bad Request",
                "shareCode and dateOfBirth are required"));
            return;
        }
        if (!req.lawfulPurposeConfirmed()) {
            verificationService.auditConfirmationRefusal(org.id());
            ctx.status(422).json(new ErrorResponse(422, "Unprocessable Entity",
                "lawfulPurposeConfirmed must be true"));
            return;
        }

        var outcome = verificationService.verifyByShareCode(
            org.id(), req.shareCode().toUpperCase(), req.dateOfBirth());
        ctx.status(200).json(toResponse(outcome));
    }

    public void document(Context ctx) {
        var req = ctx.bodyAsClass(DocumentVerifyRequest.class);
        var org = ctx.<Organisation>attribute("org");

        if (req.documentNumber() == null || req.documentType() == null) {
            ctx.status(400).json(new ErrorResponse(400, "Bad Request",
                "documentNumber and documentType are required"));
            return;
        }
        if (!req.lawfulPurposeConfirmed()) {
            verificationService.auditConfirmationRefusal(org.id());
            ctx.status(422).json(new ErrorResponse(422, "Unprocessable Entity",
                "lawfulPurposeConfirmed must be true"));
            return;
        }

        var docType = req.documentType().toUpperCase();
        if (!"PASSPORT".equals(docType) && !"PERMIT".equals(docType)) {
            ctx.status(400).json(new ErrorResponse(400, "Bad Request",
                "documentType must be PASSPORT or PERMIT"));
            return;
        }

        var outcome = verificationService.verifyByDocument(
            org.id(), req.documentNumber().toUpperCase(), docType);
        ctx.status(200).json(toResponse(outcome));
    }

    private static VerificationResponse toResponse(VerificationOutcome outcome) {
        return switch (outcome) {
            case VerificationOutcome.RightToWork r ->
                new VerificationResponse("RIGHT_TO_WORK", r.eligible(), r.expiry(),
                    null, null, null, null, null);
            case VerificationOutcome.RightToRent r ->
                new VerificationResponse("RIGHT_TO_RENT", r.eligible(), null,
                    null, null, null, null, null);
            case VerificationOutcome.EntryPermission e ->
                new VerificationResponse("ENTRY_PERMISSION", null, null,
                    e.permitted(), e.conditions(), null, null, null);
            case VerificationOutcome.StatusValidity s ->
                new VerificationResponse("STATUS_VALIDITY", null, s.expiry(),
                    null, null, s.visaType(), s.valid(), null);
            case VerificationOutcome.Rejected r ->
                new VerificationResponse("REJECTED", null, null,
                    null, null, null, null, r.reason());
        };
    }
}
