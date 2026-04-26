package immigration.repositories;

import immigration.Config;
import immigration.models.ShareCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * In-memory repository for {@link immigration.models.ShareCode} records backed by
 * {@code share_codes.json}. Mutating operations ({@link #save} and {@link #update})
 * persist changes to disk immediately.
 */
public class ShareCodeRepository extends BaseRepository {

    private static final Logger logger = LoggerFactory.getLogger(ShareCodeRepository.class);
    private final List<ShareCode> codes;
    private final String path;

    public ShareCodeRepository() {
        this(Config.SHARE_CODES_FILE);
    }

    public ShareCodeRepository(String path) {
        this.path = path;
        codes = new ArrayList<>();
        for (var obj : loadJsonArray(path)) {
            codes.add(ShareCode.fromJson(obj));
        }
        logger.info("Loaded {} share codes from {}", codes.size(), path);
    }

    /**
     * Finds a share code by its 9-character code string.
     *
     * @param code the share code to look up
     * @return the matching share code, or empty if not found
     */
    public Optional<ShareCode> findByCode(String code) {
        return codes.stream().filter(sc -> sc.code().equals(code)).findFirst();
    }

    /**
     * Appends a newly generated share code to the in-memory list and persists it to disk.
     *
     * @param sc the share code to add
     */
    public void save(ShareCode sc) {
        codes.add(sc);
        persist();
        logger.info("Persisted new share code for person {}", sc.personId());
    }

    /**
     * Replaces the stored share code that has the same code string as {@code updated},
     * then persists the change to disk. Used to mark a code as used after verification.
     *
     * @param updated the new state of the share code
     */
    public void update(ShareCode updated) {
        codes.replaceAll(sc -> sc.code().equals(updated.code()) ? updated : sc);
        persist();
        logger.info("Updated share code {}, used={}", updated.code(), updated.used());
    }

    /**
     * Returns an unmodifiable snapshot of all share codes currently in memory.
     *
     * @return all share codes
     */
    public List<ShareCode> findAll() {
        return List.copyOf(codes);
    }

    private void persist() {
        var items = codes.stream().map(ShareCode::toJson).toList();
        saveJsonArray(path, new ArrayList<>(items));
    }
}
