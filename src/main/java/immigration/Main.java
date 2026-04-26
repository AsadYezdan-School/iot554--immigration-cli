package immigration;

import immigration.api.ApiServer;
import immigration.cli.MainMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final int DEFAULT_SERVER_PORT = 7070;

    public static void main(String[] args) {
        logger.info("Immigration Status Verification System starting");

        if (Arrays.asList(args).contains("--server")) {
            int port = parsePort(args);
            var appCtx = new AppContext();
            var server = new ApiServer(appCtx);
            server.start(port);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutdown signal received — stopping API server");
                server.stop();
            }));
            // Javalin's embedded Jetty keeps the process alive until SIGINT/SIGTERM
        } else {
            var appCtx = new AppContext();
            var scanner = new Scanner(System.in);
            new MainMenu(appCtx, scanner).run();
            logger.info("Immigration Status Verification System shutting down");
        }
    }

    private static int parsePort(String[] args) {
        var list = Arrays.asList(args);
        int idx = list.indexOf("--port");
        if (idx >= 0 && idx + 1 < list.size()) {
            try { return Integer.parseInt(list.get(idx + 1)); }
            catch (NumberFormatException ignored) {}
        }
        return DEFAULT_SERVER_PORT;
    }
}
