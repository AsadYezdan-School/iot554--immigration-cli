package immigration.cli;

import immigration.services.VerificationService;
import java.util.Scanner;

/**
 * CLI sub-menu for the authority-initiated document verification route.
 *
 * <p>Collects three operational confirmations before prompting for document
 * type (PASSPORT or PERMIT) and number, then displays the outcome returned
 * by {@link immigration.services.VerificationService}.</p>
 */
public class DocumentMenu {

    private static final String[] CONFIRMATIONS = {
        "Do you confirm this check is for a lawful purpose?",
        "Do you confirm data will be handled per applicable legal frameworks?",
        "Do you confirm the subject is physically present or detained and this check is authorised?"
    };

    private final VerificationService verificationService;
    private final Scanner scanner;

    /**
     * @param verificationService service used to perform the verification
     * @param scanner             scanner attached to the user's input stream
     */
    public DocumentMenu(VerificationService verificationService, Scanner scanner) {
        this.verificationService = verificationService;
        this.scanner = scanner;
    }

    /**
     * Runs the document verification flow: collects confirmations, prompts for
     * document type and number, and prints the verification outcome.
     */
    public void run() {
        System.out.println("\n== Document Verification ==");
        System.out.print("Enter your organisation ID: ");
        var orgId = scanner.nextLine().trim();

        if (!collectConfirmations()) {
            System.out.println("\nREJECTED: All confirmations must be accepted to proceed.");
            verificationService.auditConfirmationRefusal(orgId);
            return;
        }

        System.out.print("Document type (PASSPORT/PERMIT): ");
        var docType = scanner.nextLine().trim().toUpperCase();
        if (!"PASSPORT".equals(docType) && !"PERMIT".equals(docType)) {
            System.out.println("Invalid document type. Must be PASSPORT or PERMIT.");
            return;
        }

        System.out.print("Enter document number: ");
        var docNum = scanner.nextLine().trim().toUpperCase();

        var outcome = verificationService.verifyByDocument(orgId, docNum, docType);

        System.out.println("\n--- Result ---");
        System.out.println(verificationService.formatOutcome(outcome));
    }

    private boolean collectConfirmations() {
        System.out.println("\n--- Operational Confirmations ---");
        for (int i = 0; i < CONFIRMATIONS.length; i++) {
            System.out.printf("[%d/3] %s (yes/no): ", i + 1, CONFIRMATIONS[i]);
            var response = scanner.nextLine().trim();
            if (!"yes".equalsIgnoreCase(response)) return false;
        }
        return true;
    }
}
