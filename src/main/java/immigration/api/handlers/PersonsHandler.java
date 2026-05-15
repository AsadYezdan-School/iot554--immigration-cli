package immigration.api.handlers;

import immigration.api.dto.ErrorResponse;
import immigration.api.dto.PersonLookupResponse;
import immigration.repositories.PersonRepository;
import io.javalin.http.Context;

public class PersonsHandler {

    private final PersonRepository personRepo;

    public PersonsHandler(PersonRepository personRepo) {
        this.personRepo = personRepo;
    }

    public void lookup(Context ctx) {
        var passport = ctx.queryParam("passport");
        if (passport == null || passport.isBlank()) {
            ctx.status(400).json(new ErrorResponse(400, "Bad Request",
                "passport query parameter is required"));
            return;
        }
        var personOpt = personRepo.findByPassportNumber(passport.toUpperCase());
        if (personOpt.isEmpty()) {
            ctx.status(404).json(new ErrorResponse(404, "Not Found",
                "No person found with passport number: " + passport));
            return;
        }
        var person = personOpt.get();
        ctx.json(new PersonLookupResponse(person.id(), person.fullName()));
    }
}
