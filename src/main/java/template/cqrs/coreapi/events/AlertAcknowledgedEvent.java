package template.cqrs.coreapi.events;

import lombok.Builder;
import lombok.Value;
import template.cqrs.coreapi.common.AlertStatus;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class AlertAcknowledgedEvent {
    UUID alertId;
    String acknowledgedBy;
    Instant acknowledgedAt;
    AlertStatus newStatus; // e.g., ACKNOWLEDGED
    String notes;
}
