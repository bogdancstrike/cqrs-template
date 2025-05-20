package template.cqrs.core.aggregate.commands;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;
import template.cqrs.core.value_objects.AlertDetails;
import template.cqrs.core.value_objects.AlertSeverity;

import java.util.UUID;

@Value
@Builder
public class UpdateAlertCommand {
    @TargetAggregateIdentifier
    @NotNull(message = "Alert ID cannot be null")
    UUID alertId;

    // Fields that can be updated. Make them optional (nullable) in the command
    // The handler will decide which fields to update based on what's provided.
    AlertSeverity severity; // Can be null if not updating severity

    @Size(min = 5, max = 1000, message = "Description must be between 5 and 1000 characters if provided")
    String description; // Can be null if not updating description

    AlertDetails details; // Can be null if not updating details

    String updatedBy; // User performing the update
}
