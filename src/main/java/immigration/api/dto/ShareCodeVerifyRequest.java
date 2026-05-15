package immigration.api.dto;

public record ShareCodeVerifyRequest(
    String orgId,
    String shareCode,
    String dateOfBirth,
    boolean lawfulPurposeConfirmed,
    boolean dataProtectionConfirmed,
    boolean nonDiscriminatoryConfirmed
) {}
