package immigration.repositories;

import com.google.gson.*;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base for all file-backed repositories.
 *
 * <p>Provides shared helpers for reading a JSON array from disk and writing
 * it back using Gson pretty-printing. Concrete subclasses are responsible for
 * deserialising domain objects from the loaded {@link com.google.gson.JsonObject} elements.</p>
 */
public abstract class BaseRepository {

    /**
     * Reads a JSON file and returns its top-level array as a list of objects.
     *
     * @param path absolute or relative path to the JSON file
     * @return list of JSON objects; empty list if the file does not exist or is not a JSON array
     * @throws RuntimeException if the file exists but cannot be read
     */
    protected List<JsonObject> loadJsonArray(String path) {
        var file = new File(path);
        if (!file.exists()) return new ArrayList<>();
        try (var reader = new FileReader(file)) {
            var element = JsonParser.parseReader(reader);
            if (!element.isJsonArray()) return new ArrayList<>();
            var list = new ArrayList<JsonObject>();
            for (var el : element.getAsJsonArray()) {
                list.add(el.getAsJsonObject());
            }
            return list;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + path, e);
        }
    }

    /**
     * Serialises a list of JSON objects to a file as a pretty-printed JSON array,
     * creating any missing parent directories.
     *
     * @param path  absolute or relative path to write to
     * @param items objects to serialise
     * @throws RuntimeException if the file cannot be written
     */
    protected void saveJsonArray(String path, List<JsonObject> items) {
        var gson = new GsonBuilder().setPrettyPrinting().create();
        var array = new JsonArray();
        items.forEach(array::add);
        try {
            var p = Path.of(path);
            if (p.getParent() != null) Files.createDirectories(p.getParent());
            Files.writeString(p, gson.toJson(array));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save " + path, e);
        }
    }
}
