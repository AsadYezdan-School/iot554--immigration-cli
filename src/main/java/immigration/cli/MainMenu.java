package immigration.cli;

import immigration.AppContext;
import java.util.Scanner;

/**
 * Top-level CLI menu that displays the main navigation options and delegates
 * to the appropriate sub-menu for each user choice.
 *
 * <p>Loops until the user selects the exit option (0).</p>
 */
public class MainMenu {

    private final AppContext ctx;
    private final Scanner scanner;

    /**
     * @param ctx     fully wired application context providing services and repositories
     * @param scanner scanner attached to the user's input stream
     */
    public MainMenu(AppContext ctx, Scanner scanner) {
        this.ctx = ctx;
        this.scanner = scanner;
    }

    /**
     * Displays the main menu and routes user input to the appropriate sub-menu.
     * Returns when the user chooses option 0 (Exit).
     */
    public void run() {
        while (true) {
            System.out.println();
            System.out.println("╔══════════════════════════════════════════╗");
            System.out.println("║  Immigration Status Verification System  ║");
            System.out.println("╠══════════════════════════════════════════╣");
            System.out.println("║  1. Verify via Share Code                ║");
            System.out.println("║  2. Verify via Document                  ║");
            System.out.println("║  3. Generate Share Code (Admin)          ║");
            System.out.println("║  4. Analytics                            ║");
            System.out.println("║  0. Exit                                 ║");
            System.out.println("╚══════════════════════════════════════════╝");
            System.out.print("Choice: ");

            var choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> new AdminMenu(ctx.verification, scanner).run();
                case "2" -> new DocumentMenu(ctx.verification, scanner).run();
                case "3" -> new ShareCodeMenu(ctx.shareCodeSvc, ctx.persons, scanner).run();
                case "4" -> new AnalyticsMenu(ctx.analytics, scanner).run();
                case "0" -> {
                    System.out.println("Goodbye.");
                    return;
                }
                default  -> System.out.println("Invalid choice — enter 0-4.");
            }
        }
    }
}
