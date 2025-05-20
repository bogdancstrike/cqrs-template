package template.cqrs.core.aggregate.events;

import lombok.Builder;
import lombok.Value;
import template.cqrs.core.value_objects.AlertStatus;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class AlertDeletedEvent {
    UUID alertId;
    AlertStatus newStatus; // e.g., DELETED or ARCHIVED
    Instant deletedAt;
    String deletedBy;
    String reason;
}
