package immigration;

import immigration.cli.HttpApiClient;
import immigration.cli.MainMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String DEFAULT_SERVER_URL = "http://localhost:7070";

    public static void main(String[] args) {
        logger.info("Immigration CLI starting");
        var serverUrl = parseServerUrl(args);
        var apiClient = new HttpApiClient(serverUrl);
        new MainMenu(apiClient, new Scanner(System.in)).run();
        logger.info("Immigration CLI shutting down");
    }

    private static String parseServerUrl(String[] args) {
        var list = Arrays.asList(args);
        int idx = list.indexOf("--server-url");
        if (idx >= 0 && idx + 1 < list.size()) {
            return list.get(idx + 1);
        }
        return DEFAULT_SERVER_URL;
    }
}
