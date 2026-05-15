package immigration.api.dto;

public record GenerateCodeResponse(
    String code,
    String personId,
    String purpose,
    String issuedAt,
    String expiresAt
) {}
