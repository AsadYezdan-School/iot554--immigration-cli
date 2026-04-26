package immigration.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Immutable record representing an individual's visa and associated immigration permissions.
 *
 * @param id             unique visa identifier (e.g. {@code V001})
 * @param personId       ID of the person this visa belongs to
 * @param visaType       category of visa (e.g. {@code WORK}, {@code VISITOR}, {@code SETTLEMENT})
 * @param expiryDate     expiry date in {@code YYYY-MM-DD} format
 * @param rightToWork    {@code true} if the visa grants the right to work
 * @param rightToRent    {@code true} if the visa grants the right to rent residential property
 * @param entryPermitted {@code true} if the holder is currently permitted to enter
 * @param conditions     any conditions attached to the visa (may be empty)
 */
public record Visa(
    String id,
    String personId,
    String visaType,
    String expiryDate,
    boolean rightToWork,
    boolean rightToRent,
    boolean entryPermitted,
    List<String> conditions
) {
    /**
     * Deserialises a {@code Visa} from a Gson {@link com.google.gson.JsonObject}.
     *
     * @param obj JSON object from {@code visas.json}
     * @return the corresponding {@code Visa}
     */
    public static Visa fromJson(JsonObject obj) {
        var conditions = new ArrayList<String>();
        if (obj.has("conditions")) {
            for (var el : obj.getAsJsonArray("conditions")) {
                conditions.add(el.getAsString());
            }
        }
        return new Visa(
            obj.get("id").getAsString(),
            obj.get("personId").getAsString(),
            obj.get("visaType").getAsString(),
            obj.get("expiryDate").getAsString(),
            obj.get("rightToWork").getAsBoolean(),
            obj.get("rightToRent").getAsBoolean(),
            obj.get("entryPermitted").getAsBoolean(),
            List.copyOf(conditions)
        );
    }

    /**
     * Serialises this visa to a Gson {@link com.google.gson.JsonObject}.
     *
     * @return JSON representation of this visa
     */
    public JsonObject toJson() {
        var obj = new JsonObject();
        obj.addProperty("id", id);
        obj.addProperty("personId", personId);
        obj.addProperty("visaType", visaType);
        obj.addProperty("expiryDate", expiryDate);
        obj.addProperty("rightToWork", rightToWork);
        obj.addProperty("rightToRent", rightToRent);
        obj.addProperty("entryPermitted", entryPermitted);
        var arr = new JsonArray();
        conditions.forEach(arr::add);
        obj.add("conditions", arr);
        return obj;
    }
}
