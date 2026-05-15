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
 * {@code share_codes.json}. The {@link #save} operation persists changes to disk immediately.
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
