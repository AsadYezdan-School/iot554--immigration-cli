package immigration.validators;

import immigration.Config;
import immigration.models.Organisation;
import immigration.models.ValidationResult;

/**
 * Stateless validator that checks whether an organisation's role authorises it
 * to use a specific verification route or purpose.
 *
 * <p>Permission mappings are sourced from {@link immigration.Config}. This class is
 * not instantiable; all members are static.</p>
 */
public final class OrganisationValidator {

    private OrganisationValidator() {}

    /**
     * Checks that the organisation's role is permitted to use the share-code route
     * (i.e. is one of {@code EMPLOYER}, {@code LANDLORD}, or {@code EDUCATION}).
     *
     * @param org the organisation to validate
     * @return passing result if the role is authorised; failing result otherwise
     */
    public static ValidationResult validateRole(Organisation org) {
        if (!Config.SHARE_CODE_ROUTE_ROLES.contains(org.role())) {
            return ValidationResult.fail(
                "Organisation role '" + org.role() + "' is not authorised for the share code route");
        }
        return ValidationResult.pass();
    }

    /**
     * Checks that the share-code purpose is permitted for the organisation's role
     * according to {@link immigration.Config#ALLOWED_PURPOSES}.
     *
     * @param org     the requesting organisation
     * @param purpose the purpose declared on the share code (e.g. {@code EMPLOYMENT})
     * @return passing result if the purpose is allowed; failing result otherwise
     */
    public static ValidationResult validatePurpose(Organisation org, String purpose) {
        var allowed = Config.ALLOWED_PURPOSES.get(org.role());
        if (allowed == null || !allowed.contains(purpose)) {
            return ValidationResult.fail(
                "Purpose '" + purpose + "' is not permitted for role '" + org.role() + "'");
        }
        return ValidationResult.pass();
    }

    /**
     * Checks that the organisation's role is permitted to use the document route
     * (i.e. is one of {@code BORDER_CONTROL} or {@code LAW_ENFORCEMENT}).
     *
     * @param org the organisation to validate
     * @return passing result if the role is authorised; failing result otherwise
     */
    public static ValidationResult validateForDocumentRoute(Organisation org) {
        if (!Config.DOCUMENT_ROUTE_ROLES.contains(org.role())) {
            return ValidationResult.fail(
                "Organisation role '" + org.role() + "' is not authorised for the document route");
        }
        return ValidationResult.pass();
    }
}
