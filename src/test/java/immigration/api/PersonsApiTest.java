package immigration.api;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PersonsApiTest extends ApiTestBase {

    @Test
    void lookup_knownPassport_returnsPerson() throws Exception {
        var resp = get("/api/v1/persons/lookup?passport=AB1234567");
        assertEquals(200, resp.statusCode());
        var body = mapper.readTree(resp.body());
        assertEquals("P001", body.get("personId").asText());
        assertEquals("Emma Harrison", body.get("fullName").asText());
    }

    @Test
    void lookup_unknownPassport_returns404() throws Exception {
        var resp = get("/api/v1/persons/lookup?passport=ZZ9999999");
        assertEquals(404, resp.statusCode());
    }

    @Test
    void lookup_missingPassportParam_returns400() throws Exception {
        var resp = get("/api/v1/persons/lookup");
        assertEquals(400, resp.statusCode());
    }

    @Test
    void lookup_lowercasePassport_isNormalized() throws Exception {
        // server normalizes passport to uppercase before lookup
        var resp = get("/api/v1/persons/lookup?passport=ab1234567");
        assertEquals(200, resp.statusCode());
        assertEquals("P001", mapper.readTree(resp.body()).get("personId").asText());
    }
}
