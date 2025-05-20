package template.cqrs.core.aggregate.commands;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

@Value
@Builder
public class AcknowledgeAlertCommand {
    @TargetAggregateIdentifier
    @NotNull(message = "Alert ID cannot be null")
    UUID alertId;

    @NotBlank(message = "AcknowledgedBy user cannot be blank")
    String acknowledgedBy;

    String notes; // Optional notes for acknowledgement
}
