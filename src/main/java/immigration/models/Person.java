package immigration.models;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

/**
 * Immutable record representing an individual whose immigration status may be verified.
 *
 * @param id             unique person identifier (e.g. {@code P001})
 * @param fullName       full legal name
 * @param dateOfBirth    date of birth in {@code YYYY-MM-DD} format
 * @param nationality    nationality as a plain string
 * @param passportNumber 9-character alphanumeric passport number
 * @param permitNumber   biometric residence permit number ({@code null} if not held)
 */
public record Person(
    String id,
    String fullName,
    String dateOfBirth,
    String nationality,
    String passportNumber,
    String permitNumber
) {
    /**
     * Deserialises a {@code Person} from a Gson {@link com.google.gson.JsonObject}.
     *
     * @param obj JSON object from {@code persons.json}
     * @return the corresponding {@code Person}
     */
    public static Person fromJson(JsonObject obj) {
        String permit = (obj.has("permitNumber") && !obj.get("permitNumber").isJsonNull())
            ? obj.get("permitNumber").getAsString() : null;
        return new Person(
            obj.get("id").getAsString(),
            obj.get("fullName").getAsString(),
            obj.get("dateOfBirth").getAsString(),
            obj.get("nationality").getAsString(),
            obj.get("passportNumber").getAsString(),
            permit
        );
    }

    /**
     * Serialises this person to a Gson {@link com.google.gson.JsonObject}.
     *
     * @return JSON representation of this person
     */
    public JsonObject toJson() {
        var obj = new JsonObject();
        obj.addProperty("id", id);
        obj.addProperty("fullName", fullName);
        obj.addProperty("dateOfBirth", dateOfBirth);
        obj.addProperty("nationality", nationality);
        obj.addProperty("passportNumber", passportNumber);
        if (permitNumber != null) obj.addProperty("permitNumber", permitNumber);
        else obj.add("permitNumber", JsonNull.INSTANCE);
        return obj;
    }
}