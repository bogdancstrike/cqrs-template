package template.cqrs.core.aggregate.events;

import lombok.Builder;
import lombok.Value;
import template.cqrs.core.value_objects.AlertStatus;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class AlertClosedEvent {
    UUID alertId;
    String closedBy;
    Instant closedAt;
    AlertStatus newStatus; // e.g., CLOSED
    String reason;
}