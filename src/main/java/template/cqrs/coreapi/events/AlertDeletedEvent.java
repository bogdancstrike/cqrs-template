package template.cqrs.coreapi.events;

import lombok.Builder;
import lombok.Value;
import template.cqrs.coreapi.common.AlertStatus;

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
