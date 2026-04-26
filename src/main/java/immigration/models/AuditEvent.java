package immigration.models;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

/**
 * Immutable record representing a single entry in the append-only audit log.
 *
 * @param timestamp       ISO-8601 timestamp of the event (nanosecond precision)
 * @param eventType       category such as {@code SHARE_CODE_VERIFICATION} or {@code CONFIRMATION_REFUSED}
 * @param organisationId  ID of the requesting organisation, or {@code SYSTEM} for generated codes
 * @param maskedPersonId  person ID with all but the first four characters replaced by {@code ****}
 * @param shareCode       share code or document number referenced by the event, may be {@code null}
 * @param outcome         {@code APPROVED}, {@code REJECTED}, or {@code GENERATED}
 * @param detail          human-readable description of the outcome or reason for rejection
 */
public record AuditEvent(
    String timestamp,
    String eventType,
    String organisationId,
    String maskedPersonId,
    String shareCode,
    String outcome,
    String detail
) {
    /**
     * Deserialises an {@code AuditEvent} from a Gson {@link com.google.gson.JsonObject}.
     *
     * @param obj JSON object representing one line of the audit log
     * @return the corresponding {@code AuditEvent}
     */
    public static AuditEvent fromJson(JsonObject obj) {
        return new AuditEvent(
            obj.get("timestamp").getAsString(),
            obj.get("eventType").getAsString(),
            nullableString(obj, "organisationId"),
            nullableString(obj, "maskedPersonId"),
            nullableString(obj, "shareCode"),
            obj.get("outcome").getAsString(),
            nullableString(obj, "detail")
        );
    }

    /**
     * Serialises this event to a Gson {@link com.google.gson.JsonObject} suitable
     * for writing as a single JSONL line.
     *
     * @return JSON representation of this event
     */
    public JsonObject toJson() {
        var obj = new JsonObject();
        obj.addProperty("timestamp", timestamp);
        obj.addProperty("eventType", eventType);
        putNullable(obj, "organisationId", organisationId);
        putNullable(obj, "maskedPersonId", maskedPersonId);
        putNullable(obj, "shareCode", shareCode);
        obj.addProperty("outcome", outcome);
        putNullable(obj, "detail", detail);
        return obj;
    }

    private static String nullableString(JsonObject obj, String key) {
        return (obj.has(key) && !obj.get(key).isJsonNull()) ? obj.get(key).getAsString() : null;
    }

    private static void putNullable(JsonObject obj, String key, String value) {
        if (value != null) obj.addProperty(key, value);
        else obj.add(key, JsonNull.INSTANCE);
    }
}
