package template.cqrs.domain.alert.aggregate.commands;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

@Value
@Builder
public class DeleteAlertCommand {
    @TargetAggregateIdentifier
    @NotNull(message = "Alert ID cannot be null")
    UUID alertId;

    String deletedBy; // User performing the deletion
    String reason; // Optional reason for deletion
}