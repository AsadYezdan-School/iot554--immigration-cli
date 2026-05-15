package immigration.api.dto;

public record GenerateCodeRequest(
    String personId,
    String purpose
) {}
