package immigration.api.handlers;

import immigration.api.dto.ErrorResponse;
import immigration.api.dto.GenerateCodeRequest;
import immigration.api.dto.GenerateCodeResponse;
import immigration.repositories.PersonRepository;
import immigration.services.ShareCodeService;
import io.javalin.http.Context;
import java.util.Set;

public class CodesHandler {

    private static final Set<String> VALID_PURPOSES =
        Set.of("EMPLOYMENT", "ACCOMMODATION", "EDUCATION");

    private final ShareCodeService shareCodeService;
    private final PersonRepository personRepo;

    public CodesHandler(ShareCodeService shareCodeService, PersonRepository personRepo) {
        this.shareCodeService = shareCodeService;
        this.personRepo = personRepo;
    }

    public void generate(Context ctx) {
        var req = ctx.bodyAsClass(GenerateCodeRequest.class);

        if (req.personId() == null || req.purpose() == null) {
            ctx.status(400).json(new ErrorResponse(400, "Bad Request",
                "personId and purpose are required"));
            return;
        }

        var purpose = req.purpose().toUpperCase();
        if (!VALID_PURPOSES.contains(purpose)) {
            ctx.status(400).json(new ErrorResponse(400, "Bad Request",
                "purpose must be EMPLOYMENT, ACCOMMODATION, or EDUCATION"));
            return;
        }

        if (personRepo.findById(req.personId()).isEmpty()) {
            ctx.status(404).json(new ErrorResponse(404, "Not Found",
                "No person found with id: " + req.personId()));
            return;
        }

        var sc = shareCodeService.generateCode(req.personId(), purpose);
        ctx.status(201).json(new GenerateCodeResponse(
            sc.code(), sc.personId(), sc.purpose(), sc.issuedAt(), sc.expiresAt()));
    }
}
