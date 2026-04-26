package immigration.api.dto;

public record ShareCodeVerifyRequest(
    String shareCode,
    String dateOfBirth,
    boolean lawfulPurposeConfirmed
) {}
