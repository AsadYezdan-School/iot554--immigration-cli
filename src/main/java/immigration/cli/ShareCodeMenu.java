package immigration.cli;

import immigration.api.dto.GenerateCodeRequest;
import java.util.Scanner;
import java.util.Set;

public class ShareCodeMenu {

    private static final Set<String> VALID_PURPOSES = Set.of("EMPLOYMENT", "ACCOMMODATION", "EDUCATION");

    private final HttpApiClient apiClient;
    private final Scanner scanner;

    public ShareCodeMenu(HttpApiClient apiClient, Scanner scanner) {
        this.apiClient = apiClient;
        this.scanner = scanner;
    }

    public void run() {
        System.out.println("\n== Generate Share Code ==");

        System.out.print("Enter person ID (e.g. P001) or leave blank to search by passport: ");
        var input = scanner.nextLine().trim();

        String personId;
        if (input.isEmpty()) {
            System.out.print("Enter passport number: ");
            var passport = scanner.nextLine().trim().toUpperCase();
            try {
                var lookup = apiClient.lookupByPassport(passport);
                personId = lookup.personId();
                System.out.println("Found person: " + lookup.fullName() + " (ID: " + personId + ")");
            } catch (ApiException e) {
                System.out.println("No person found with passport number: " + passport);
                return;
            }
        } else {
            personId = input;
        }

        System.out.print("Enter purpose (EMPLOYMENT / ACCOMMODATION / EDUCATION): ");
        var purpose = scanner.nextLine().trim().toUpperCase();
        if (!VALID_PURPOSES.contains(purpose)) {
            System.out.println("Invalid purpose. Must be EMPLOYMENT, ACCOMMODATION, or EDUCATION.");
            return;
        }

        try {
            var sc = apiClient.generateCode(new GenerateCodeRequest(personId, purpose));
            System.out.println("\nShare code generated: " + sc.code());
            System.out.println("Valid until:          " + sc.expiresAt().substring(0, 10));
            System.out.println("Purpose:              " + sc.purpose());
        } catch (ApiException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
