package template.cqrs.domain.model.alert.events;

import lombok.Builder;
import lombok.Value;
import template.cqrs.domain.model.alert.value_objects.AlertStatus;

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
