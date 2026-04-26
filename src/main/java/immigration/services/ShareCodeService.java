package immigration.services;

import immigration.Config;
import immigration.models.AuditEvent;
import immigration.models.ShareCode;
import immigration.repositories.AuditRepository;
import immigration.repositories.ShareCodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Service responsible for generating single-use share codes.
 *
 * <p>Codes are 9 characters drawn from {@link immigration.Config#SHARE_CODE_ALPHABET}
 * using a {@link java.security.SecureRandom}, valid for {@link immigration.Config#SHARE_CODE_EXPIRY_DAYS}
 * days from the time of generation. Each generated code is persisted immediately and
 * recorded in the audit log.</p>
 */
public class ShareCodeService {

    private static final Logger logger = LoggerFactory.getLogger(ShareCodeService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ShareCodeRepository shareCodeRepo;
    private final AuditRepository auditRepo;

    public ShareCodeService(ShareCodeRepository shareCodeRepo, AuditRepository auditRepo) {
        this.shareCodeRepo = shareCodeRepo;
        this.auditRepo = auditRepo;
    }

    /**
     * Generates a new share code for the given person and purpose, saves it to the
     * repository, and writes a {@code SHARE_CODE_GENERATED} audit event.
     *
     * @param personId ID of the person the code is issued to
     * @param purpose  intended verification purpose: {@code EMPLOYMENT}, {@code ACCOMMODATION},
     *                 or {@code EDUCATION}
     * @return the newly created and persisted {@link immigration.models.ShareCode}
     */
    public ShareCode generateCode(String personId, String purpose) {
        var code = buildCode();
        var now = Instant.now();
        var sc = new ShareCode(
            code, personId, purpose,
            now.toString(),
            now.plus(Config.SHARE_CODE_EXPIRY_DAYS, ChronoUnit.DAYS).toString(),
            false
        );
        shareCodeRepo.save(sc);
        auditRepo.append(new AuditEvent(
            now.toString(), "SHARE_CODE_GENERATED", "SYSTEM",
            maskId(personId), code, "GENERATED", purpose));
        logger.info("Generated share code for person={} purpose={}", maskId(personId), purpose);
        return sc;
    }

    private static String buildCode() {
        var sb = new StringBuilder(Config.SHARE_CODE_LENGTH);
        for (int i = 0; i < Config.SHARE_CODE_LENGTH; i++) {
            sb.append(Config.SHARE_CODE_ALPHABET.charAt(
                RANDOM.nextInt(Config.SHARE_CODE_ALPHABET.length())));
        }
        return sb.toString();
    }

    private static String maskId(String personId) {
        if (personId == null || personId.length() < 4) return "****";
        return personId.substring(0, 4) + "****";
    }
}
