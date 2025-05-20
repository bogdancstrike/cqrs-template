package template.cqrs.domain.alert.aggregate.commands;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

@Value
@Builder
public class AssignAlertCommand {
    @TargetAggregateIdentifier
    @NotNull(message = "Alert ID cannot be null")
    UUID alertId;

    @NotBlank(message = "Assignee cannot be blank")
    String assignee; // User or team ID

    String assignedBy; // User performing the assignment
}

