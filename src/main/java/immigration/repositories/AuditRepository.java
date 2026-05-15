package immigration.repositories;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import immigration.Config;
import immigration.models.AuditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Append-only repository for {@link immigration.models.AuditEvent} records stored
 * in JSONL format (one JSON object per line) at {@code audit_log.jsonl}.
 *
 */
public class AuditRepository {

    private static final Logger logger = LoggerFactory.getLogger(AuditRepository.class);
    private static final Gson GSON = new Gson();
    private final String path;

    public AuditRepository() {
        this(Config.AUDIT_LOG_FILE);
    }

    public AuditRepository(String path) {
        this.path = path;
        try {
            var p = Path.of(path);
            if (p.getParent() != null) Files.createDirectories(p.getParent());
            if (!Files.exists(p)) Files.createFile(p);
        } catch (IOException e) {
            logger.warn("Could not initialise audit log at {}: {}", path, e.getMessage());
        }
    }

    /**
     * Serialises and appends an audit event as a single line to the log file.
     *
     * @param event the event to record
     */
    public void append(AuditEvent event) {
        String line = GSON.toJson(event.toJson());
        try (var writer = new FileWriter(path, true)) {
            writer.write(line);
            writer.write(System.lineSeparator());
        } catch (IOException e) {
            logger.error("Failed to append audit event: {}", e.getMessage());
        }
    }

    /**
     * Reads and deserialises all non-blank lines from the audit log file.
     *
     * @return list of all recorded audit events; empty list if the file does not exist or is unreadable
     */
    public List<AuditEvent> queryAll() {
        var file = new File(path);
        if (!file.exists()) return new ArrayList<>();
        try (var reader = new BufferedReader(new FileReader(file))) {
            return reader.lines()
                .filter(line -> !line.isBlank())
                .map(line -> AuditEvent.fromJson(JsonParser.parseString(line).getAsJsonObject()))
                .toList();
        } catch (IOException e) {
            logger.error("Failed to read audit log: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
