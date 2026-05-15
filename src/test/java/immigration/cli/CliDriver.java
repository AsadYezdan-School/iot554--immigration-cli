package immigration.cli;

import java.io.ByteArrayInputStream;
import java.util.Scanner;

/**
 * Builds a scripted input sequence and exposes it as a {@link Scanner}.
 */
class CliDriver {

    private final StringBuilder inputs = new StringBuilder();

    CliDriver type(String line) {
        inputs.append(line).append("\n");
        return this;
    }

    Scanner toScanner() {
        return new Scanner(new ByteArrayInputStream(inputs.toString().getBytes()));
    }
}
