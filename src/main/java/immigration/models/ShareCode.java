package immigration.models;

import com.google.gson.JsonObject;

public record ShareCode(
    String code,
    String personId,
    String purpose,
    String issuedAt,
    String expiresAt
) {
    public static ShareCode fromJson(JsonObject obj) {
        return new ShareCode(
            obj.get("code").getAsString(),
            obj.get("personId").getAsString(),
            obj.get("purpose").getAsString(),
            obj.get("issuedAt").getAsString(),
            obj.get("expiresAt").getAsString()
        );
    }

    public JsonObject toJson() {
        var obj = new JsonObject();
        obj.addProperty("code", code);
        obj.addProperty("personId", personId);
        obj.addProperty("purpose", purpose);
        obj.addProperty("issuedAt", issuedAt);
        obj.addProperty("expiresAt", expiresAt);
        return obj;
    }
}
