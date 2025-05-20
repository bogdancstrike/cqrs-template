package template.cqrs.core.aggregate.events;

import lombok.Builder;
import lombok.Value;
import template.cqrs.core.value_objects.AlertDetails;
import template.cqrs.core.value_objects.AlertSeverity;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class AlertUpdatedEvent {
    UUID alertId;
    // Include only fields that were actually updated, or all updatable fields
    // For simplicity, including all potentially updatable fields.
    // Projectors can check for nulls if only partial updates are stored.
    AlertSeverity severity;
    String description;
    AlertDetails details;
    Instant updatedAt;
    String updatedBy;
}
