package immigration.cli;

import immigration.api.dto.DocumentVerifyRequest;
import java.util.Scanner;

public class DocumentMenu {

    private static final String[] CONFIRMATIONS = {
        "Do you confirm this check is for a lawful purpose?",
        "Do you confirm data will be handled per applicable legal frameworks?",
        "Do you confirm the subject is physically present or detained and this check is authorised?"
    };

    private final HttpApiClient apiClient;
    private final Scanner scanner;

    public DocumentMenu(HttpApiClient apiClient, Scanner scanner) {
        this.apiClient = apiClient;
        this.scanner = scanner;
    }

    public void run() {
        System.out.println("\n== Document Verification ==");
        System.out.print("Enter your organisation ID: ");
        var orgId = scanner.nextLine().trim();

        var confirmed = collectConfirmations();

        String docType = null;
        String docNum = null;
        if (confirmed[0] && confirmed[1] && confirmed[2]) {
            System.out.print("Document type (PASSPORT/PERMIT): ");
            docType = scanner.nextLine().trim().toUpperCase();
            if (!"PASSPORT".equals(docType) && !"PERMIT".equals(docType)) {
                System.out.println("Invalid document type. Must be PASSPORT or PERMIT.");
                return;
            }
            System.out.print("Enter document number: ");
            docNum = scanner.nextLine().trim().toUpperCase();
        }

        var req = new DocumentVerifyRequest(orgId, docNum, docType,
            confirmed[0], confirmed[1], confirmed[2]);

        try {
            var response = apiClient.verifyByDocument(req);
            System.out.println("\n--- Result ---");
            System.out.println(ResponseFormatter.format(response));
        } catch (ApiException e) {
            System.out.println("\nREJECTED: " + e.getMessage());
        }
    }

    private boolean[] collectConfirmations() {
        System.out.println("\n--- Operational Confirmations ---");
        var results = new boolean[CONFIRMATIONS.length];
        for (int i = 0; i < CONFIRMATIONS.length; i++) {
            System.out.printf("[%d/3] %s (yes/no): ", i + 1, CONFIRMATIONS[i]);
            results[i] = "yes".equalsIgnoreCase(scanner.nextLine().trim());
            if (!results[i]) break;
        }
        return results;
    }
}
