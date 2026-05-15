package immigration.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record VerificationResponse(
    String outcomeType,
    Boolean eligible,
    LocalDate expiry,
    Boolean permitted,
    List<String> conditions,
    String visaType,
    Boolean valid,
    String reason
) {}
