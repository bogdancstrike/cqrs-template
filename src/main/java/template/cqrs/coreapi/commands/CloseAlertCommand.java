package template.cqrs.coreapi.commands;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

@Value
@Builder
public class CloseAlertCommand {
    @TargetAggregateIdentifier
    @NotNull(message = "Alert ID cannot be null")
    UUID alertId;

    @NotBlank(message = "ClosedBy user cannot be blank")
    String closedBy;

    String reason; // Optional reason for closing
}