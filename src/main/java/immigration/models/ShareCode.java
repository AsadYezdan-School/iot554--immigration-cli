package immigration.models;

import com.google.gson.JsonObject;

/**
 * Immutable record representing a single-use share code issued to a person
 * so they can authorise a third party to verify their immigration status.
 *
 * @param code      9-character alphanumeric code drawn from {@link immigration.Config#SHARE_CODE_ALPHABET}
 * @param personId  ID of the person the code was issued to
 * @param purpose   intended verification purpose: {@code EMPLOYMENT}, {@code ACCOMMODATION}, or {@code EDUCATION}
 * @param issuedAt  ISO-8601 timestamp of when the code was generated
 * @param expiresAt ISO-8601 timestamp after which the code is no longer valid
 * @param used      {@code true} once the code has been successfully verified (single-use enforcement)
 */
public record ShareCode(
    String code,
    String personId,
    String purpose,
    String issuedAt,
    String expiresAt,
    boolean used
) {
    /**
     * Returns a copy of this share code with {@code used} set to {@code true}.
     *
     * @return new {@code ShareCode} instance marked as used
     */
    public ShareCode markUsed() {
        return new ShareCode(code, personId, purpose, issuedAt, expiresAt, true);
    }

    /**
     * Deserialises a {@code ShareCode} from a Gson {@link com.google.gson.JsonObject}.
     *
     * @param obj JSON object from {@code share_codes.json}
     * @return the corresponding {@code ShareCode}
     */
    public static ShareCode fromJson(JsonObject obj) {
        return new ShareCode(
            obj.get("code").getAsString(),
            obj.get("personId").getAsString(),
            obj.get("purpose").getAsString(),
            obj.get("issuedAt").getAsString(),
            obj.get("expiresAt").getAsString(),
            obj.get("used").getAsBoolean()
        );
    }

    /**
     * Serialises this share code to a Gson {@link com.google.gson.JsonObject}.
     *
     * @return JSON representation of this share code
     */
    public JsonObject toJson() {
        var obj = new JsonObject();
        obj.addProperty("code", code);
        obj.addProperty("personId", personId);
        obj.addProperty("purpose", purpose);
        obj.addProperty("issuedAt", issuedAt);
        obj.addProperty("expiresAt", expiresAt);
        obj.addProperty("used", used);
        return obj;
    }
}
