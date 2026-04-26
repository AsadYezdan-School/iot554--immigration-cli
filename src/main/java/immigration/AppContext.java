package immigration;

import immigration.repositories.*;
import immigration.services.*;
import java.nio.file.Path;

/**
 * Minimal dependency-injection container that wires together all repositories
 * and services for the application.
 *
 * <p>Exposes fully constructed collaborators as public final fields so that
 * CLI menu classes can obtain them without a framework.</p>
 */
public final class AppContext {

    /** Repository for person records. */
    public final PersonRepository persons;
    /** Repository for visa records. */
    public final VisaRepository visas;
    /** Repository for share codes. */
    public final ShareCodeRepository shareCodes;
    /** Repository for registered organisations. */
    public final OrganisationRepository organisations;
    /** Append-only repository for audit events. */
    public final AuditRepository audit;
    /** Service handling both verification routes. */
    public final VerificationService verification;
    /** Service for generating share codes. */
    public final ShareCodeService shareCodeSvc;
    /** Service providing audit-log analytics. */
    public final AnalyticsService analytics;

    /**
     * Creates a context wired to the default data-file paths from {@link immigration.Config}.
     */
    public AppContext() {
        persons       = new PersonRepository();
        visas         = new VisaRepository();
        shareCodes    = new ShareCodeRepository();
        organisations = new OrganisationRepository();
        audit         = new AuditRepository();
        verification  = new VerificationService(persons, visas, shareCodes, organisations, audit);
        shareCodeSvc  = new ShareCodeService(shareCodes, audit);
        analytics     = new AnalyticsService(audit);
    }

    /**
     * Creates a context wired to JSON files found inside the given directory.
     * Intended for testing with a temporary directory.
     *
     * @param dataDir directory containing the data JSON files
     */
    public AppContext(Path dataDir) {
        persons       = new PersonRepository(dataDir.resolve("persons.json").toString());
        visas         = new VisaRepository(dataDir.resolve("visas.json").toString());
        shareCodes    = new ShareCodeRepository(dataDir.resolve("share_codes.json").toString());
        organisations = new OrganisationRepository(dataDir.resolve("organisations.json").toString());
        audit         = new AuditRepository(dataDir.resolve("audit_log.jsonl").toString());
        verification  = new VerificationService(persons, visas, shareCodes, organisations, audit);
        shareCodeSvc  = new ShareCodeService(shareCodes, audit);
        analytics     = new AnalyticsService(audit);
    }
}
