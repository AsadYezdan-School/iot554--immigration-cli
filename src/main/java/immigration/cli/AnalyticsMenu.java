package immigration.cli;

import immigration.services.AnalyticsService;
import java.util.Map;
import java.util.Scanner;

/**
 * CLI sub-menu for viewing aggregated audit-log analytics.
 *
 * <p>Offers four report views sourced from {@link immigration.services.AnalyticsService}:
 * requests by organisation, by date, share codes by purpose, and outcomes summary.
 * Loops until the user selects option 0 (Back).</p>
 */
public class AnalyticsMenu {

    private final AnalyticsService analyticsService;
    private final Scanner scanner;

    /**
     * @param analyticsService service providing the aggregated audit data
     * @param scanner          scanner attached to the user's input stream
     */
    public AnalyticsMenu(AnalyticsService analyticsService, Scanner scanner) {
        this.analyticsService = analyticsService;
        this.scanner = scanner;
    }

    /**
     * Displays the analytics menu and prints the selected report.
     * Returns when the user chooses option 0 (Back).
     */
    public void run() {
        while (true) {
            System.out.println("\n== Analytics ==");
            System.out.println("1. Requests by organisation");
            System.out.println("2. Requests by date");
            System.out.println("3. Share codes by purpose");
            System.out.println("4. Outcomes summary");
            System.out.println("0. Back");
            System.out.print("Choice: ");

            var choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> printTable("Requests by Organisation", analyticsService.requestsByOrganisation());
                case "2" -> printTable("Requests by Date",         analyticsService.requestsByDate());
                case "3" -> printTable("Share Codes by Purpose",   analyticsService.shareCodesByPurpose());
                case "4" -> printTable("Outcomes Summary",         analyticsService.outcomesByType());
                case "0" -> { return; }
                default  -> System.out.println("Invalid option. Choose to view analytics (1-4) or go back (0).");
            }
        }
    }

    private void printTable(String title, Map<String, Long> data) {
        System.out.println("\n--- " + title + " ---");
        if (data.isEmpty()) {
            System.out.println("  (no data)");
            return;
        }
        data.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .forEach(e -> System.out.printf("  %-35s %d%n", e.getKey(), e.getValue()));
    }
}
