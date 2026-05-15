package immigration.cli;

import immigration.api.dto.ShareCodeVerifyRequest;
import java.util.Scanner;

public class AdminMenu {

    private static final String[] CONFIRMATIONS = {
        "Do you confirm this check is for a lawful purpose?",
        "Do you confirm data will be handled per data protection obligations?",
        "Do you confirm this check is not discriminatory or unauthorised?"
    };

    private final HttpApiClient apiClient;
    private final Scanner scanner;

    public AdminMenu(HttpApiClient apiClient, Scanner scanner) {
        this.apiClient = apiClient;
        this.scanner = scanner;
    }

    public void run() {
        System.out.println("\n== Share Code Verification ==");
        System.out.print("Enter your organisation ID: ");
        var orgId = scanner.nextLine().trim();

        var confirmed = collectConfirmations();

        String code = null;
        String dob = null;
        if (confirmed[0] && confirmed[1] && confirmed[2]) {
            System.out.print("Enter share code: ");
            code = scanner.nextLine().trim().toUpperCase();
            System.out.print("Enter date of birth (YYYY-MM-DD): ");
            dob = scanner.nextLine().trim();
        }

        var req = new ShareCodeVerifyRequest(orgId, code, dob,
            confirmed[0], confirmed[1], confirmed[2]);

        try {
            var response = apiClient.verifyByShareCode(req);
            System.out.println("\n--- Result ---");
            System.out.println(ResponseFormatter.format(response));
        } catch (ApiException e) {
            System.out.println("\nREJECTED: " + e.getMessage());
        }
    }

    private boolean[] collectConfirmations() {
        System.out.println("\n--- Lawful Purpose Confirmations ---");
        var results = new boolean[CONFIRMATIONS.length];
        for (int i = 0; i < CONFIRMATIONS.length; i++) {
            System.out.printf("[%d/3] %s (yes/no): ", i + 1, CONFIRMATIONS[i]);
            results[i] = "yes".equalsIgnoreCase(scanner.nextLine().trim());
            if (!results[i]) break;
        }
        return results;
    }
}
