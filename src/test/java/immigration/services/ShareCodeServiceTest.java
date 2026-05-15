package immigration.services;

import immigration.repositories.AuditRepository;
import immigration.repositories.ShareCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.*;

class ShareCodeServiceTest {

    @TempDir
    Path tempDir;

    private ShareCodeService service;
    private ShareCodeRepository shareCodeRepo;

    @BeforeEach
    void setUp() throws IOException {
        Files.writeString(tempDir.resolve("share_codes.json"), "[]");
        Files.writeString(tempDir.resolve("audit_log.jsonl"), "");

        shareCodeRepo = new ShareCodeRepository(tempDir.resolve("share_codes.json").toString());
        var auditRepo = new AuditRepository(tempDir.resolve("audit_log.jsonl").toString());
        service = new ShareCodeService(shareCodeRepo, auditRepo);
    }

    @Test
    void generatedCode_matchesFormat() {
        var sc = service.generateCode("P001", "EMPLOYMENT");
        assertTrue(Pattern.matches("^[A-Z0-9]{9}$", sc.code()),
            "Code '" + sc.code() + "' did not match ^[A-Z0-9]{9}$");
    }

    @Test
    void generatedCode_isPersisted() {
        var sc = service.generateCode("P001", "EMPLOYMENT");
        var found = shareCodeRepo.findByCode(sc.code());
        assertTrue(found.isPresent(), "Generated code not found in repository");
        assertEquals("P001", found.get().personId());
    }

    @Test
    void generatedCode_expiryIsApproximately30Days() {
        var before = Instant.now();
        var sc = service.generateCode("P001", "EMPLOYMENT");
        var after = Instant.now();

        var expiry = Instant.parse(sc.expiresAt());
        var expectedMin = before.plus(30, ChronoUnit.DAYS);
        var expectedMax = after.plus(30, ChronoUnit.DAYS).plus(1, ChronoUnit.SECONDS);

        assertTrue(!expiry.isBefore(expectedMin) && !expiry.isAfter(expectedMax),
            "Expiry " + expiry + " was not ~30 days from now");
    }

    @Test
    void twoCodes_areUnique() {
        var sc1 = service.generateCode("P001", "EMPLOYMENT");
        var sc2 = service.generateCode("P001", "EMPLOYMENT");
        assertNotEquals(sc1.code(), sc2.code());
    }

    @Test
    void generatedCode_purposeIsPreserved() {
        var sc = service.generateCode("P003", "ACCOMMODATION");
        assertEquals("ACCOMMODATION", sc.purpose());
    }
}
