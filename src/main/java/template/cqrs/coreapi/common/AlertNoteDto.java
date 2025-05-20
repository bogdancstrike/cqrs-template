package template.cqrs.coreapi.common;

import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
public class AlertNoteDto {
    UUID noteId;
    String text;
    String author;
    Instant timestamp;
}
