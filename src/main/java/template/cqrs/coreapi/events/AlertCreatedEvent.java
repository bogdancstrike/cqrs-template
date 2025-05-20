package template.cqrs.coreapi.events;

import lombok.Builder;
import lombok.Value;
import template.cqrs.coreapi.common.AlertDetails;
import template.cqrs.coreapi.common.AlertSeverity;
import template.cqrs.coreapi.common.AlertStatus;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class AlertCreatedEvent {
    UUID alertId;
    AlertSeverity severity;
    String description;
    String source;
    AlertDetails details;
    AlertStatus initialStatus; // Typically ACTIVE
    Instant createdAt; // Timestamp of creation in the system
    Instant eventTimestamp; // Timestamp from the original source event, if provided
    String initiatedBy; // User who initiated the creation, if applicable
}
