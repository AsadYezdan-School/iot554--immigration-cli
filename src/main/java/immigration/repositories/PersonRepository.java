package immigration.repositories;

import immigration.Config;
import immigration.models.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Read-only in-memory repository for {@link immigration.models.Person} records
 * loaded from {@code persons.json} at construction time.
 */
public class PersonRepository extends BaseRepository {

    private static final Logger logger = LoggerFactory.getLogger(PersonRepository.class);
    private final List<Person> persons;

    public PersonRepository() {
        this(Config.PERSONS_FILE);
    }

    public PersonRepository(String path) {
        persons = new ArrayList<>();
        for (var obj : loadJsonArray(path)) {
            persons.add(Person.fromJson(obj));
        }
        logger.info("Loaded {} persons from {}", persons.size(), path);
    }

    /**
     * Finds a person by their unique ID.
     *
     * @param id person identifier (e.g. {@code P001})
     * @return the matching person, or empty if not found
     */
    public Optional<Person> findById(String id) {
        return persons.stream().filter(p -> p.id().equals(id)).findFirst();
    }

    /**
     * Finds a person by their passport number.
     *
     * @param passportNumber 9-character alphanumeric passport number
     * @return the matching person, or empty if not found
     */
    public Optional<Person> findByPassportNumber(String passportNumber) {
        return persons.stream()
            .filter(p -> passportNumber.equals(p.passportNumber()))
            .findFirst();
    }

    /**
     * Finds a person by their biometric residence permit number.
     *
     * @param permitNumber permit number in the format {@code AA0000000}
     * @return the matching person, or empty if not found
     */
    public Optional<Person> findByPermitNumber(String permitNumber) {
        return persons.stream()
            .filter(p -> permitNumber.equals(p.permitNumber()))
            .findFirst();
    }

    /**
     * Returns an unmodifiable view of all loaded persons.
     *
     * @return all persons
     */
    public List<Person> findAll() {
        return List.copyOf(persons);
    }
}
