package immigration.repositories;

import immigration.Config;
import immigration.models.Organisation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Read-only in-memory repository for {@link immigration.models.Organisation} records
 * loaded from {@code organisations.json} at construction time.
 */
public class OrganisationRepository extends BaseRepository {

    private static final Logger logger = LoggerFactory.getLogger(OrganisationRepository.class);
    private final List<Organisation> organisations;

    public OrganisationRepository() {
        this(Config.ORGANISATIONS_FILE);
    }

    public OrganisationRepository(String path) {
        organisations = new ArrayList<>();
        for (var obj : loadJsonArray(path)) {
            organisations.add(Organisation.fromJson(obj));
        }
        logger.info("Loaded {} organisations from {}", organisations.size(), path);
    }

    /**
     * Finds an organisation by its unique ID.
     *
     * @param id organisation identifier (e.g. {@code ORG001})
     * @return the matching organisation, or empty if not found
     */
    public Optional<Organisation> findById(String id) {
        return organisations.stream().filter(o -> o.id().equals(id)).findFirst();
    }

    /**
     * Finds an organisation by its email address (case-insensitive).
     *
     * @param email contact email of the organisation
     * @return the matching organisation, or empty if not found
     */
    public Optional<Organisation> findByEmail(String email) {
        return organisations.stream()
            .filter(o -> o.email().equalsIgnoreCase(email))
            .findFirst();
    }

    /**
     * Finds an organisation by its API key.
     *
     * @param apiKey the {@code X-API-Key} header value
     * @return the matching organisation, or empty if not found
     */
    public Optional<Organisation> findByApiKey(String apiKey) {
        if (apiKey == null) return Optional.empty();
        return organisations.stream()
            .filter(o -> apiKey.equals(o.apiKey()))
            .findFirst();
    }

    /**
     * Returns an unmodifiable view of all loaded organisations.
     *
     * @return all organisations
     */
    public List<Organisation> findAll() {
        return List.copyOf(organisations);
    }
}
