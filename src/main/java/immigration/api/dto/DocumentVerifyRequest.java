package immigration.api.dto;

public record DocumentVerifyRequest(
    String orgId,
    String documentNumber,
    String documentType,
    boolean lawfulPurposeConfirmed,
    boolean dataProtectionConfirmed,
    boolean nonDiscriminatoryConfirmed
) {}
