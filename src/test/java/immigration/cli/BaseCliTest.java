package immigration.cli;

import immigration.AppContext;
import immigration.repositories.*;
import immigration.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

abstract class BaseCliTest {

    @TempDir
    Path tempDir;

    PersonRepository personRepo;
    VisaRepository visaRepo;
    ShareCodeRepository shareCodeRepo;
    OrganisationRepository orgRepo;
    AuditRepository auditRepo;
    VerificationService verificationService;
    ShareCodeService shareCodeService;
    AnalyticsService analyticsService;

    @BeforeEach
    void setUpBase() throws IOException {
        Files.writeString(tempDir.resolve("persons.json"), """
            [
              {"id":"P001","fullName":"Emma Harrison","dateOfBirth":"1985-03-22",
               "nationality":"Australian","passportNumber":"AB1234567","permitNumber":null},
              {"id":"P004","fullName":"Luca Ferretti","dateOfBirth":"1968-05-01",
               "nationality":"Italian","passportNumber":"EF3456789","permitNumber":null},
              {"id":"P005","fullName":"Mei Zhang","dateOfBirth":"2000-09-25",
               "nationality":"Chinese","passportNumber":"GH5678901","permitNumber":null}
            ]
            """);

        Files.writeString(tempDir.resolve("visas.json"), """
            [
              {"id":"V001","personId":"P001","visaType":"WORK","expiryDate":"2027-06-30",
               "rightToWork":true,"rightToRent":false,"entryPermitted":true,"conditions":[]},
              {"id":"V004","personId":"P004","visaType":"SETTLEMENT","expiryDate":"2035-12-31",
               "rightToWork":true,"rightToRent":true,"entryPermitted":true,"conditions":[]},
              {"id":"V005","personId":"P005","visaType":"VISITOR","expiryDate":"2026-10-25",
               "rightToWork":false,"rightToRent":false,"entryPermitted":true,
               "conditions":["No employment"]}
            ]
            """);

        Files.writeString(tempDir.resolve("share_codes.json"), """
            [
              {"code":"ABC123XY1","personId":"P001","purpose":"EMPLOYMENT",
               "issuedAt":"2026-01-01T00:00:00Z","expiresAt":"2027-01-01T00:00:00Z","used":false},
              {"code":"KLM012BC4","personId":"P001","purpose":"EMPLOYMENT",
               "issuedAt":"2024-01-01T00:00:00Z","expiresAt":"2024-06-01T00:00:00Z","used":false},
              {"code":"NOP345DE5","personId":"P004","purpose":"ACCOMMODATION",
               "issuedAt":"2026-01-01T00:00:00Z","expiresAt":"2027-01-01T00:00:00Z","used":true},
              {"code":"DEF456YZ2","personId":"P004","purpose":"ACCOMMODATION",
               "issuedAt":"2026-01-01T00:00:00Z","expiresAt":"2027-01-01T00:00:00Z","used":false},
              {"code":"QRS678FG6","personId":"P005","purpose":"EMPLOYMENT",
               "issuedAt":"2026-01-01T00:00:00Z","expiresAt":"2027-01-01T00:00:00Z","used":false}
            ]
            """);

        Files.writeString(tempDir.resolve("organisations.json"), """
            [
              {"id":"ORG001","name":"Acme Ltd","email":"hr@acme.com","role":"EMPLOYER"},
              {"id":"ORG003","name":"City Rentals","email":"lets@cityrentals.com","role":"LANDLORD"},
              {"id":"ORG005","name":"Port Authority","email":"ops@portauth.gov","role":"BORDER_CONTROL"},
              {"id":"ORG006","name":"Metro Police","email":"verify@metropolice.gov","role":"LAW_ENFORCEMENT"}
            ]
            """);

        Files.writeString(tempDir.resolve("audit_log.jsonl"), "");

        personRepo    = new PersonRepository(tempDir.resolve("persons.json").toString());
        visaRepo      = new VisaRepository(tempDir.resolve("visas.json").toString());
        shareCodeRepo = new ShareCodeRepository(tempDir.resolve("share_codes.json").toString());
        orgRepo       = new OrganisationRepository(tempDir.resolve("organisations.json").toString());
        auditRepo     = new AuditRepository(tempDir.resolve("audit_log.jsonl").toString());

        verificationService = new VerificationService(personRepo, visaRepo, shareCodeRepo, orgRepo, auditRepo);
        shareCodeService    = new ShareCodeService(shareCodeRepo, auditRepo);
        analyticsService    = new AnalyticsService(auditRepo);
    }

    CliDriver driver() { return new CliDriver(); }

    String runShareCodeMenu(CliDriver d) {
        Scanner scanner = d.toScanner();
        ShareCodeMenu menu = new ShareCodeMenu(shareCodeService, personRepo, scanner);
        try (CapturedOutput output = new CapturedOutput()) {
            menu.run();
            return output.get();
        }
    }

    String runDocumentMenu(CliDriver d) {
        Scanner scanner = d.toScanner();
        DocumentMenu menu = new DocumentMenu(verificationService, scanner);
        try (CapturedOutput output = new CapturedOutput()) {
            menu.run();
            return output.get();
        }
    }

    String runAdminMenu(CliDriver d) {
        Scanner scanner = d.toScanner();
        AdminMenu menu = new AdminMenu(verificationService, scanner);
        try (CapturedOutput output = new CapturedOutput()) {
            menu.run();
            return output.get();
        }
    }

    String runMainMenu(CliDriver d) {
        Scanner scanner = d.toScanner();
        AppContext ctx = new AppContext(tempDir);
        MainMenu menu = new MainMenu(ctx, scanner);
        try (CapturedOutput output = new CapturedOutput()) {
            menu.run();
            return output.get();
        }
    }
}
