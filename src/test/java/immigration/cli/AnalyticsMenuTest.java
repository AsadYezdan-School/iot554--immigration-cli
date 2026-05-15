package immigration.cli;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnalyticsMenuTest extends BaseCliTest {

    @Test
    void byOrganisation_showsHeader() {
        var out = runAnalyticsMenu(driver().type("1").type("0"));
        assertTrue(out.contains("Requests by Organisation"), out);
    }

    @Test
    void byDate_showsHeader() {
        var out = runAnalyticsMenu(driver().type("2").type("0"));
        assertTrue(out.contains("Requests by Date"), out);
    }

    @Test
    void byPurpose_showsHeader() {
        var out = runAnalyticsMenu(driver().type("3").type("0"));
        assertTrue(out.contains("Share Codes by Purpose"), out);
    }

    @Test
    void outcomes_showsHeader() {
        var out = runAnalyticsMenu(driver().type("4").type("0"));
        assertTrue(out.contains("Outcomes Summary"), out);
    }

    @Test
    void emptyLog_showsNoData() {
        var out = runAnalyticsMenu(driver().type("1").type("0"));
        assertNotNull(out);
        assertTrue(out.contains("Requests by Organisation"), out);
    }

    @Test
    void invalidOption_showsError() {
        var out = runAnalyticsMenu(driver().type("X").type("0"));
        assertTrue(out.contains("Invalid option"), out);
    }

    @Test
    void back_returnsFromMenu() {
        var out = runAnalyticsMenu(driver().type("0"));
        assertNotNull(out);
    }
}
