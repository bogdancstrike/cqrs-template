package template.cqrs.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import template.cqrs.core.value_objects.AlertDetails;
import template.cqrs.core.value_objects.AlertNoteDto;
import template.cqrs.core.value_objects.AlertSeverity;
import template.cqrs.core.value_objects.AlertStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object representing an Alert for query responses.
 * This structure should mirror the Elasticsearch document.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertDto {
    private UUID alertId;
    private AlertSeverity severity;
    private String description;
    private String source;
    private AlertStatus status;
    private AlertDetails details;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant eventTimestamp;
    private String initiatedBy;
    private String updatedBy;
    private Instant acknowledgedAt;
    private String acknowledgedBy;
    private String acknowledgementNotes;
    private Instant resolvedAt;
    private String resolvedBy;
    private String resolutionDetails;
    private Instant closedAt;
    private String closedBy;
    private String closingReason;
    private String assignee;
    private Instant assignedAt;
    private String assignedBy;
    private List<AlertNoteDto> notes; // List of note DTOs
}

