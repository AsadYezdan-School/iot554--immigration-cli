package immigration.repositories;

import immigration.Config;
import immigration.models.Visa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Read-only in-memory repository for {@link immigration.models.Visa} records
 * loaded from {@code visas.json} at construction time.
 */
public class VisaRepository extends BaseRepository {

    private static final Logger logger = LoggerFactory.getLogger(VisaRepository.class);
    private final List<Visa> visas;

    public VisaRepository() {
        this(Config.VISAS_FILE);
    }

    public VisaRepository(String path) {
        visas = new ArrayList<>();
        for (var obj : loadJsonArray(path)) {
            visas.add(Visa.fromJson(obj));
        }
        logger.info("Loaded {} visas from {}", visas.size(), path);
    }

    /**
     * Finds the visa associated with a given person.
     *
     * @param personId person identifier (e.g. {@code P001})
     * @return the person's visa, or empty if none exists
     */
    public Optional<Visa> findByPersonId(String personId) {
        return visas.stream().filter(v -> v.personId().equals(personId)).findFirst();
    }

    /**
     * Returns an unmodifiable view of all loaded visas.
     *
     * @return all visas
     */
    public List<Visa> findAll() {
        return List.copyOf(visas);
    }
}
