package template.cqrs.domain.alert.aggregate.events;

import lombok.Builder;
import lombok.Value;
import template.cqrs.domain.alert.value_objects.AlertNoteDto;

import java.util.UUID;

@Value
@Builder
public class NoteAddedToAlertEvent {
    UUID alertId;
    AlertNoteDto note; // Embed the note DTO
    // UUID noteId; // Alternatively, if noteId is generated and passed separately
    // String text;
    // String author;
    // Instant timestamp;
}
