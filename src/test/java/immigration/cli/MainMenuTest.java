package immigration.cli;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MainMenuTest extends BaseCliTest {

    @Test
    void exitChoice_showsGoodbye() {
        var out = runMainMenu(driver().type("0"));
        assertTrue(out.contains("Goodbye."), out);
    }

    @Test
    void invalidChoice_showsError() {
        var out = runMainMenu(driver().type("X").type("0"));
        assertTrue(out.contains("Invalid choice"), out);
    }

    @Test
    void multipleInvalidChoices_loopsUntilExit() {
        var out = runMainMenu(driver().type("9").type("Z").type("0"));
        var count = out.split("Invalid choice", -1).length - 1;
        assertEquals(2, count, "Expected 2 invalid-choice messages: " + out);
    }

    @Test
    void navigateToShareCode_showsHeader() {
        var out = runMainMenu(driver()
            .type("1").type("ORG001").type("no")
            .type("0"));
        assertTrue(out.contains("== Share Code Verification =="), out);
    }

    @Test
    void navigateToDocument_showsHeader() {
        var out = runMainMenu(driver()
            .type("2").type("ORG005").type("no")
            .type("0"));
        assertTrue(out.contains("== Document Verification =="), out);
    }

    @Test
    void navigateToGenerateCode_showsHeader() {
        var out = runMainMenu(driver()
            .type("3").type("P999").type("EMPLOYMENT")
            .type("0"));
        assertTrue(out.contains("== Generate Share Code =="), out);
    }
}
