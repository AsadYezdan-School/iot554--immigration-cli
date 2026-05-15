package immigration.cli;

import immigration.api.dto.VerificationResponse;

class ResponseFormatter {

    private ResponseFormatter() {}

    static String format(VerificationResponse r) {
        return switch (r.outcomeType()) {
            case "RIGHT_TO_WORK"    -> "Right to work: " + r.eligible() + ", expires: " + r.expiry();
            case "RIGHT_TO_RENT"    -> "Right to rent: " + r.eligible();
            case "ENTRY_PERMISSION" -> "Entry permitted: " + r.permitted() + ", conditions: " + r.conditions();
            case "STATUS_VALIDITY"  -> "Visa type: " + r.visaType() + ", valid: " + r.valid() + ", expires: " + r.expiry();
            case "REJECTED"         -> "REJECTED: " + r.reason();
            default                 -> "Unknown outcome: " + r.outcomeType();
        };
    }
}
