package immigration.models;

import com.google.gson.JsonObject;

/**
 * Immutable record representing a registered organisation that may perform
 * immigration status checks.
 *
 * @param id   unique organisation identifier (e.g. {@code ORG001})
 * @param name display name of the organisation
 * @param email contact email address
 * @param role authorisation role determining which verification routes are available
 */
public record Organisation(
    String id,
    String name,
    String email,
    String role
) {
    public static Organisation fromJson(JsonObject obj) {
        return new Organisation(
            obj.get("id").getAsString(),
            obj.get("name").getAsString(),
            obj.get("email").getAsString(),
            obj.get("role").getAsString()
        );
    }

    public JsonObject toJson() {
        var obj = new JsonObject();
        obj.addProperty("id", id);
        obj.addProperty("name", name);
        obj.addProperty("email", email);
        obj.addProperty("role", role);
        return obj;
    }
}
