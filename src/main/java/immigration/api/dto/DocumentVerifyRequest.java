package immigration.api.dto;

public record DocumentVerifyRequest(
    String documentNumber,
    String documentType,
    boolean lawfulPurposeConfirmed
) {}
