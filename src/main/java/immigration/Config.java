package immigration;

/**
 * Application-wide constants: file paths, share-code generation rules, and
 * the role-to-purpose permission mappings used by the validation layer.
 *
 * <p>This class is not instantiable; all members are static.</p>
 */
public final class Config {

    private Config() {}

    public static final String DATA_DIR = "data/";
    public static final String PERSONS_FILE = DATA_DIR + "persons.json";
    public static final String VISAS_FILE = DATA_DIR + "visas.json";
    public static final String SHARE_CODES_FILE = DATA_DIR + "share_codes.json";
    public static final String ORGANISATIONS_FILE = DATA_DIR + "organisations.json";
    public static final String AUDIT_LOG_FILE = DATA_DIR + "audit_log.jsonl";
    public static final String LOG_DIR = "logs/";
    public static final String LOG_FILE = LOG_DIR + "app.log";

    public static final int SHARE_CODE_EXPIRY_DAYS = 30;
    public static final String SHARE_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    public static final int SHARE_CODE_LENGTH = 9;

    public static final java.util.Map<String, java.util.Set<String>> ALLOWED_PURPOSES =
        java.util.Map.of(
            "EMPLOYER",       java.util.Set.of("EMPLOYMENT"),
            "LANDLORD",       java.util.Set.of("ACCOMMODATION"),
            "EDUCATION",      java.util.Set.of("EDUCATION")
        );

    public static final java.util.Set<String> DOCUMENT_ROUTE_ROLES =
        java.util.Set.of("BORDER_CONTROL", "LAW_ENFORCEMENT");

    public static final java.util.Set<String> SHARE_CODE_ROUTE_ROLES =
        java.util.Set.of("EMPLOYER", "LANDLORD", "EDUCATION");
}