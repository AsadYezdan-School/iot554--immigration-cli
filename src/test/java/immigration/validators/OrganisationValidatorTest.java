package immigration.validators;

import immigration.models.Organisation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrganisationValidatorTest {

    // --- validateRole (share code route) ---

    @Test
    void employer_passesShareCodeRoute() {
        assertTrue(OrganisationValidator.validateRole(org("EMPLOYER")).ok());
    }

    @Test
    void landlord_passesShareCodeRoute() {
        assertTrue(OrganisationValidator.validateRole(org("LANDLORD")).ok());
    }

    @Test
    void education_passesShareCodeRoute() {
        assertTrue(OrganisationValidator.validateRole(org("EDUCATION")).ok());
    }

    @Test
    void borderControl_failsShareCodeRoute() {
        // DT: BORDER_CONTROL is not a share code route role
        var result = OrganisationValidator.validateRole(org("BORDER_CONTROL"));
        assertFalse(result.ok());
        assertNotNull(result.reason());
    }

    @Test
    void lawEnforcement_failsShareCodeRoute() {
        assertFalse(OrganisationValidator.validateRole(org("LAW_ENFORCEMENT")).ok());
    }

    // --- validatePurpose ---

    @Test
    void employer_employmentPurpose_passes() {
        assertTrue(OrganisationValidator.validatePurpose(org("EMPLOYER"), "EMPLOYMENT").ok());
    }

    @Test
    void employer_accommodationPurpose_fails() {
        // DT: purpose mismatch
        assertFalse(OrganisationValidator.validatePurpose(org("EMPLOYER"), "ACCOMMODATION").ok());
    }

    @Test
    void landlord_accommodationPurpose_passes() {
        assertTrue(OrganisationValidator.validatePurpose(org("LANDLORD"), "ACCOMMODATION").ok());
    }

    @Test
    void landlord_employmentPurpose_fails() {
        assertFalse(OrganisationValidator.validatePurpose(org("LANDLORD"), "EMPLOYMENT").ok());
    }

    @Test
    void education_educationPurpose_passes() {
        assertTrue(OrganisationValidator.validatePurpose(org("EDUCATION"), "EDUCATION").ok());
    }

    @Test
    void employer_unknownPurpose_fails() {
        // EP: purpose not in any allowed set
        assertFalse(OrganisationValidator.validatePurpose(org("EMPLOYER"), "UNKNOWN").ok());
    }

    // --- validateForDocumentRoute ---

    @Test
    void borderControl_passesDocumentRoute() {
        assertTrue(OrganisationValidator.validateForDocumentRoute(org("BORDER_CONTROL")).ok());
    }

    @Test
    void lawEnforcement_passesDocumentRoute() {
        assertTrue(OrganisationValidator.validateForDocumentRoute(org("LAW_ENFORCEMENT")).ok());
    }

    @Test
    void employer_failsDocumentRoute() {
        // DT: employer cannot use document route
        var result = OrganisationValidator.validateForDocumentRoute(org("EMPLOYER"));
        assertFalse(result.ok());
        assertNotNull(result.reason());
    }

    @Test
    void landlord_failsDocumentRoute() {
        assertFalse(OrganisationValidator.validateForDocumentRoute(org("LANDLORD")).ok());
    }

    private static Organisation org(String role) {
        return new Organisation("ORG001", "Test Org", "test@test.com", role, null);
    }
}
