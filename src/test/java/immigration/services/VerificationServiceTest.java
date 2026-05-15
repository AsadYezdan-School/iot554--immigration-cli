package immigration.services;

import immigration.models.VerificationOutcome;
import immigration.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class VerificationServiceTest {

    @TempDir
    Path tempDir;

    private VerificationService service;

    @BeforeEach
    void setUp() throws IOException {
        Files.writeString(tempDir.resolve("persons.json"), """
            [
              {"id":"P001","fullName":"Emma Harrison","dateOfBirth":"1985-03-22",
               "nationality":"Australian","passportNumber":"AB1234567","permitNumber":null},
              {"id":"P005","fullName":"Mei Zhang","dateOfBirth":"2000-09-25",
               "nationality":"Chinese","passportNumber":"GH5678901","permitNumber":null},
              {"id":"P004","fullName":"Luca Ferretti","dateOfBirth":"1968-05-01",
               "nationality":"Italian","passportNumber":"EF3456789","permitNumber":null},
              {"id":"P006","fullName":"Omar Khalid","dateOfBirth":"1990-07-15",
               "nationality":"Jordanian","passportNumber":"JJ9876543","permitNumber":null},
              {"id":"P007","fullName":"Ana Sousa","dateOfBirth":"1995-02-28",
               "nationality":"Portuguese","passportNumber":"PT3456789","permitNumber":"CD7654321"}
            ]
            """);

        Files.writeString(tempDir.resolve("visas.json"), """
            [
              {"id":"V001","personId":"P001","visaType":"WORK","expiryDate":"2027-06-30",
               "rightToWork":true,"rightToRent":false,"entryPermitted":true,"conditions":[]},
              {"id":"V005","personId":"P005","visaType":"VISITOR","expiryDate":"2026-10-25",
               "rightToWork":false,"rightToRent":false,"entryPermitted":true,
               "conditions":["No employment"]},
              {"id":"V004","personId":"P004","visaType":"SETTLEMENT","expiryDate":"2035-12-31",
               "rightToWork":true,"rightToRent":true,"entryPermitted":true,"conditions":[]},
              {"id":"V007","personId":"P007","visaType":"SKILLED_WORKER","expiryDate":"2028-06-30",
               "rightToWork":true,"rightToRent":true,"entryPermitted":true,"conditions":[]}
            ]
            """);

        Files.writeString(tempDir.resolve("share_codes.json"), """
            [
              {"code":"ABC123XY1","personId":"P001","purpose":"EMPLOYMENT",
               "issuedAt":"2026-01-01T00:00:00Z","expiresAt":"2027-01-01T00:00:00Z"},
              {"code":"KLM012BC4","personId":"P001","purpose":"EMPLOYMENT",
               "issuedAt":"2024-01-01T00:00:00Z","expiresAt":"2024-06-01T00:00:00Z"},
              {"code":"DEF456YZ2","personId":"P004","purpose":"ACCOMMODATION",
               "issuedAt":"2026-01-01T00:00:00Z","expiresAt":"2027-01-01T00:00:00Z"},
              {"code":"QRS678FG6","personId":"P005","purpose":"EMPLOYMENT",
               "issuedAt":"2026-01-01T00:00:00Z","expiresAt":"2027-01-01T00:00:00Z"},
              {"code":"EDU789AB3","personId":"P001","purpose":"EDUCATION",
               "issuedAt":"2026-01-01T00:00:00Z","expiresAt":"2027-01-01T00:00:00Z"},
              {"code":"OKL456ZZ9","personId":"P006","purpose":"EMPLOYMENT",
               "issuedAt":"2026-01-01T00:00:00Z","expiresAt":"2027-01-01T00:00:00Z"}
            ]
            """);

        Files.writeString(tempDir.resolve("organisations.json"), """
            [
              {"id":"ORG001","name":"Acme Ltd","email":"hr@acme.com","role":"EMPLOYER"},
              {"id":"ORG003","name":"City Rentals","email":"lets@cityrentals.com","role":"LANDLORD"},
              {"id":"ORG004","name":"City College","email":"admin@citycollege.ac.uk","role":"EDUCATION"},
              {"id":"ORG005","name":"Port Authority","email":"ops@portauth.gov","role":"BORDER_CONTROL"},
              {"id":"ORG006","name":"Metro Police","email":"verify@metropolice.gov","role":"LAW_ENFORCEMENT"}
            ]
            """);

        Files.writeString(tempDir.resolve("audit_log.jsonl"), "");

        var personRepo    = new PersonRepository(tempDir.resolve("persons.json").toString());
        var visaRepo      = new VisaRepository(tempDir.resolve("visas.json").toString());
        var shareCodeRepo = new ShareCodeRepository(tempDir.resolve("share_codes.json").toString());
        var orgRepo       = new OrganisationRepository(tempDir.resolve("organisations.json").toString());
        var auditRepo     = new AuditRepository(tempDir.resolve("audit_log.jsonl").toString());

        service = new VerificationService(personRepo, visaRepo, shareCodeRepo, orgRepo, auditRepo);
    }

    // --- Share code route: success paths ---

    @Test
    void validShareCode_employer_returnsRightToWork() {
        var outcome = service.verifyByShareCode("ORG001", "ABC123XY1", "1985-03-22");
        assertInstanceOf(VerificationOutcome.RightToWork.class, outcome);
        var rtw = (VerificationOutcome.RightToWork) outcome;
        assertTrue(rtw.eligible());
    }

    @Test
    void validShareCode_isReusableWithinWindow() {
        var first = service.verifyByShareCode("ORG001", "ABC123XY1", "1985-03-22");
        assertInstanceOf(VerificationOutcome.RightToWork.class, first);
        var second = service.verifyByShareCode("ORG001", "ABC123XY1", "1985-03-22");
        assertInstanceOf(VerificationOutcome.RightToWork.class, second);
    }

    @Test
    void visitorVisa_employerCheck_rightToWorkFalse() {
        var outcome = service.verifyByShareCode("ORG001", "QRS678FG6", "2000-09-25");
        assertInstanceOf(VerificationOutcome.RightToWork.class, outcome);
        assertFalse(((VerificationOutcome.RightToWork) outcome).eligible());
    }

    @Test
    void validShareCode_landlord_returnsRightToRent() {
        var outcome = service.verifyByShareCode("ORG003", "DEF456YZ2", "1968-05-01");
        assertInstanceOf(VerificationOutcome.RightToRent.class, outcome);
    }

    @Test
    void validShareCode_education_returnsRightToWork() {
        var outcome = service.verifyByShareCode("ORG004", "EDU789AB3", "1985-03-22");
        assertInstanceOf(VerificationOutcome.RightToWork.class, outcome);
        assertTrue(((VerificationOutcome.RightToWork) outcome).eligible());
    }

    // --- Share code route: rejection paths ---

    @Test
    void expiredShareCode_rejected() {
        var outcome = service.verifyByShareCode("ORG001", "KLM012BC4", "1985-03-22");
        assertInstanceOf(VerificationOutcome.Rejected.class, outcome);
        assertTrue(((VerificationOutcome.Rejected) outcome).reason().contains("expired"));
    }

    @Test
    void invalidFormatCode_rejected() {
        var outcome = service.verifyByShareCode("ORG001", "BADCODE", "1985-03-22");
        assertInstanceOf(VerificationOutcome.Rejected.class, outcome);
    }

    @Test
    void wrongDob_rejected() {
        var outcome = service.verifyByShareCode("ORG001", "ABC123XY1", "1999-01-01");
        assertInstanceOf(VerificationOutcome.Rejected.class, outcome);
        assertTrue(((VerificationOutcome.Rejected) outcome).reason().toLowerCase().contains("date of birth"));
    }

    @Test
    void purposeMismatch_rejected() {
        var outcome = service.verifyByShareCode("ORG001", "DEF456YZ2", "1968-05-01");
        assertInstanceOf(VerificationOutcome.Rejected.class, outcome);
        assertTrue(((VerificationOutcome.Rejected) outcome).reason().contains("Purpose"));
    }

    @Test
    void unknownOrg_rejected() {
        var outcome = service.verifyByShareCode("ORG999", "ABC123XY1", "1985-03-22");
        assertInstanceOf(VerificationOutcome.Rejected.class, outcome);
    }

    @Test
    void documentRouteOrg_onShareCodeRoute_rejected() {
        var outcome = service.verifyByShareCode("ORG005", "ABC123XY1", "1985-03-22");
        assertInstanceOf(VerificationOutcome.Rejected.class, outcome);
    }

    @Test
    void personWithNoVisa_shareCodeRoute_rejected() {
        var outcome = service.verifyByShareCode("ORG001", "OKL456ZZ9", "1990-07-15");
        assertInstanceOf(VerificationOutcome.Rejected.class, outcome);
        assertTrue(((VerificationOutcome.Rejected) outcome).reason().toLowerCase().contains("visa"));
    }

    // --- Document route: success paths ---

    @Test
    void validPassport_borderControl_returnsEntryPermission() {
        var outcome = service.verifyByDocument("ORG005", "AB1234567", "PASSPORT");
        assertInstanceOf(VerificationOutcome.EntryPermission.class, outcome);
        assertTrue(((VerificationOutcome.EntryPermission) outcome).permitted());
    }

    @Test
    void validPermit_borderControl_returnsEntryPermission() {
        var outcome = service.verifyByDocument("ORG005", "CD7654321", "PERMIT");
        assertInstanceOf(VerificationOutcome.EntryPermission.class, outcome);
        assertTrue(((VerificationOutcome.EntryPermission) outcome).permitted());
    }

    @Test
    void validPassport_lawEnforcement_returnsStatusValidity() {
        var outcome = service.verifyByDocument("ORG006", "AB1234567", "PASSPORT");
        assertInstanceOf(VerificationOutcome.StatusValidity.class, outcome);
        var sv = (VerificationOutcome.StatusValidity) outcome;
        assertEquals("WORK", sv.visaType());
        assertTrue(sv.valid());
    }

    @Test
    void validPermit_lawEnforcement_returnsStatusValidity() {
        var outcome = service.verifyByDocument("ORG006", "CD7654321", "PERMIT");
        assertInstanceOf(VerificationOutcome.StatusValidity.class, outcome);
        assertEquals("SKILLED_WORKER", ((VerificationOutcome.StatusValidity) outcome).visaType());
    }

    // --- Document route: rejection paths ---

    @Test
    void employer_onDocumentRoute_rejected() {
        var outcome = service.verifyByDocument("ORG001", "AB1234567", "PASSPORT");
        assertInstanceOf(VerificationOutcome.Rejected.class, outcome);
    }

    @Test
    void invalidPassportFormat_rejected() {
        var outcome = service.verifyByDocument("ORG005", "AB123456", "PASSPORT");
        assertInstanceOf(VerificationOutcome.Rejected.class, outcome);
    }

    @Test
    void documentNotFound_rejected() {
        var outcome = service.verifyByDocument("ORG005", "ZZ9999999", "PASSPORT");
        assertInstanceOf(VerificationOutcome.Rejected.class, outcome);
        assertTrue(((VerificationOutcome.Rejected) outcome).reason().contains("not found"));
    }
}
