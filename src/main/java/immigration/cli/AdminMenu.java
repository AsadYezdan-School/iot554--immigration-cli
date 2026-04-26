package immigration.cli;

import immigration.services.VerificationService;
import java.util.Scanner;

/**
 * CLI sub-menu for the individual-initiated share-code verification route.
 *
 * <p>Collects three lawful-purpose confirmations before prompting for a share
 * code and date of birth, then displays the outcome returned by
 * {@link immigration.services.VerificationService}.</p>
 */
public class AdminMenu {

    private static final String[] CONFIRMATIONS = {
        "Do you confirm this check is for a lawful purpose?",
        "Do you confirm data will be handled per data protection obligations?",
        "Do you confirm this check is not discriminatory or unauthorised?"
    };

    private final VerificationService verificationService;
    private final Scanner scanner;

    /**
     * @param verificationService service used to perform the verification
     * @param scanner             scanner attached to the user's input stream
     */
    public AdminMenu(VerificationService verificationService, Scanner scanner) {
        this.verificationService = verificationService;
        this.scanner = scanner;
    }

    /**
     * Runs the share-code verification flow: collects confirmations, prompts for
     * the share code and date of birth, and prints the verification outcome.
     */
    public void run() {
        System.out.println("\n== Share Code Verification ==");
        System.out.print("Enter your organisation ID: ");
        var orgId = scanner.nextLine().trim();

        if (!collectConfirmations()) {
            System.out.println("\nREJECTED: All confirmations must be accepted to proceed.");
            verificationService.auditConfirmationRefusal(orgId);
            return;
        }

        System.out.print("Enter share code: ");
        var code = scanner.nextLine().trim().toUpperCase();

        System.out.print("Enter date of birth (YYYY-MM-DD): ");
        var dob = scanner.nextLine().trim();

        var outcome = verificationService.verifyByShareCode(orgId, code, dob);

        System.out.println("\n--- Result ---");
        System.out.println(verificationService.formatOutcome(outcome));
    }

    private boolean collectConfirmations() {
        System.out.println("\n--- Lawful Purpose Confirmations ---");
        for (int i = 0; i < CONFIRMATIONS.length; i++) {
            System.out.printf("[%d/3] %s (yes/no): ", i + 1, CONFIRMATIONS[i]);
            var response = scanner.nextLine().trim();
            if (!"yes".equalsIgnoreCase(response)) return false;
        }
        return true;
    }
}
