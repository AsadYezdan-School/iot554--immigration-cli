package immigration.services;

import immigration.repositories.AuditRepository;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service that derives aggregated statistics from the audit log.
 *
 * <p>Each method re-reads the full audit log via {@link immigration.repositories.AuditRepository#queryAll()}
 * and streams the results into frequency maps, so data is always current.</p>
 */
public class AnalyticsService {

    private final AuditRepository auditRepo;

    public AnalyticsService(AuditRepository auditRepo) {
        this.auditRepo = auditRepo;
    }

    /**
     * Returns the count of audit events grouped by organisation ID,
     * excluding events with a null organisation.
     *
     * @return map of organisation ID to event count
     */
    public Map<String, Long> requestsByOrganisation() {
        return auditRepo.queryAll().stream()
            .filter(e -> e.organisationId() != null)
            .collect(Collectors.groupingBy(e -> e.organisationId(), Collectors.counting()));
    }

    /**
     * Returns the count of audit events grouped by calendar date.
     *
     * @return map of date string to event count
     */
    public Map<String, Long> requestsByDate() {
        return auditRepo.queryAll().stream()
            .collect(Collectors.groupingBy(
                e -> e.timestamp().substring(0, 10),
                Collectors.counting()));
    }

    /**
     * Returns the count of generated share codes grouped by purpose
     * (only {@code SHARE_CODE_GENERATED} events with a non-null detail are counted).
     *
     * @return map of purpose to generation count
     */
    public Map<String, Long> shareCodesByPurpose() {
        return auditRepo.queryAll().stream()
            .filter(e -> "SHARE_CODE_GENERATED".equals(e.eventType()) && e.detail() != null)
            .collect(Collectors.groupingBy(e -> e.detail(), Collectors.counting()));
    }

    /**
     * Returns the count of audit events grouped by outcome value
     * (e.g. {@code APPROVED}, {@code REJECTED}, {@code GENERATED}).
     *
     * @return map of outcome to event count
     */
    public Map<String, Long> outcomesByType() {
        return auditRepo.queryAll().stream()
            .filter(e -> e.outcome() != null)
            .collect(Collectors.groupingBy(e -> e.outcome(), Collectors.counting()));
    }
}
