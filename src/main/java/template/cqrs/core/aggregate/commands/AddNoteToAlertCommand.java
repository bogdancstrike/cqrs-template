package template.cqrs.core.aggregate.commands;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

@Value
@Builder
public class AddNoteToAlertCommand {
    @TargetAggregateIdentifier
    @NotNull(message = "Alert ID cannot be null")
    UUID alertId;

    // noteId could be generated in command handler or aggregate if not provided by client
    // UUID noteId;

    @NotBlank(message = "Note text cannot be blank")
    String text;

    @NotBlank(message = "Author cannot be blank")
    String author;
}
