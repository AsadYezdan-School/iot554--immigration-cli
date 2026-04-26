package immigration.cli;

import immigration.repositories.PersonRepository;
import immigration.services.ShareCodeService;
import java.util.Scanner;
import java.util.Set;

/**
 * CLI sub-menu for generating share codes.
 *
 * <p>Allows an operator to look up a person by ID or passport number and
 * issue a new share code for a given purpose via
 * {@link immigration.services.ShareCodeService}.</p>
 */
public class ShareCodeMenu {

    private static final Set<String> VALID_PURPOSES = Set.of("EMPLOYMENT", "ACCOMMODATION", "EDUCATION");

    private final ShareCodeService shareCodeService;
    private final PersonRepository personRepo;
    private final Scanner scanner;

    /**
     * @param shareCodeService service used to generate and persist share codes
     * @param personRepo       repository used to look up persons by ID or passport
     * @param scanner          scanner attached to the user's input stream
     */
    public ShareCodeMenu(ShareCodeService shareCodeService, PersonRepository personRepo, Scanner scanner) {
        this.shareCodeService = shareCodeService;
        this.personRepo = personRepo;
        this.scanner = scanner;
    }

    /**
     * Runs the share-code generation flow: prompts for a person (by ID or passport),
     * validates the requested purpose, generates a code, and prints it to the console.
     */
    public void run() {
        System.out.println("\n== Generate Share Code ==");

        System.out.print("Enter person ID (e.g. P001) or leave blank to search by passport: ");
        var input = scanner.nextLine().trim();

        String personId;
        if (input.isEmpty()) {
            System.out.print("Enter passport number: ");
            var passport = scanner.nextLine().trim().toUpperCase();
            var personOpt = personRepo.findByPassportNumber(passport);
            if (personOpt.isEmpty()) {
                System.out.println("No person found with passport number: " + passport);
                return;
            }
            personId = personOpt.get().id();
            System.out.println("Found person: " + personOpt.get().fullName() + " (ID: " + personId + ")");
        } else {
            var personOpt = personRepo.findById(input);
            if (personOpt.isEmpty()) {
                System.out.println("No person found with ID: " + input);
                return;
            }
            personId = personOpt.get().id();
        }

        System.out.print("Enter purpose (EMPLOYMENT / ACCOMMODATION / EDUCATION): ");
        var purpose = scanner.nextLine().trim().toUpperCase();
        if (!VALID_PURPOSES.contains(purpose)) {
            System.out.println("Invalid purpose. Must be EMPLOYMENT, ACCOMMODATION, or EDUCATION.");
            return;
        }

        var sc = shareCodeService.generateCode(personId, purpose);
        System.out.println("\nShare code generated: " + sc.code());
        System.out.println("Valid until:          " + sc.expiresAt().substring(0, 10));
        System.out.println("Purpose:              " + sc.purpose());
    }
}
