package immigration.cli;

import java.util.Map;
import java.util.Scanner;

public class AnalyticsMenu {

    private final HttpApiClient apiClient;
    private final Scanner scanner;

    public AnalyticsMenu(HttpApiClient apiClient, Scanner scanner) {
        this.apiClient = apiClient;
        this.scanner = scanner;
    }

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
                case "1" -> printTable("Requests by Organisation", apiClient.getAnalyticsByOrganisation().data());
                case "2" -> printTable("Requests by Date",         apiClient.getAnalyticsByDate().data());
                case "3" -> printTable("Share Codes by Purpose",   apiClient.getAnalyticsByPurpose().data());
                case "4" -> printTable("Outcomes Summary",         apiClient.getAnalyticsByOutcome().data());
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
