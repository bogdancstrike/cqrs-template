package template.cqrs.domain.alert.aggregate.events;

import lombok.Builder;
import lombok.Value;
import template.cqrs.domain.alert.value_objects.AlertStatus;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class AlertResolvedEvent {
    UUID alertId;
    String resolvedBy;
    String resolutionDetails;
    Instant resolvedAt;
    AlertStatus newStatus; // e.g., RESOLVED
}
