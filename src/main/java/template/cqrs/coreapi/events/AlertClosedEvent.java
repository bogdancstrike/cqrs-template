package template.cqrs.coreapi.events;

import lombok.Builder;
import lombok.Value;
import template.cqrs.coreapi.common.AlertStatus;

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