package template.cqrs.core.aggregate.events;

import lombok.Builder;
import lombok.Value;
import template.cqrs.core.value_objects.AlertDetails;
import template.cqrs.core.value_objects.AlertSeverity;
import template.cqrs.core.value_objects.AlertStatus;

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
