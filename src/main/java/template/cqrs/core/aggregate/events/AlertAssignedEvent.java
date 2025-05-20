package template.cqrs.core.aggregate.events;

import lombok.Builder;
import lombok.Value;
import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class AlertAssignedEvent {
    UUID alertId;
    String assignee;
    Instant assignedAt;
    String assignedBy;
}