package immigration.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Redirects {@code System.out} to an in-memory buffer for the duration of a
 * try-with-resources block, then restores the original stream on close.
 */
class CapturedOutput implements AutoCloseable {

    private final PrintStream original = System.out;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

     CapturedOutput() {
        System.setOut(new PrintStream(buffer));
    }

    String get() {
        return buffer.toString();
    }

    @Override
    public void close() {
        System.setOut(original);
    }
}
