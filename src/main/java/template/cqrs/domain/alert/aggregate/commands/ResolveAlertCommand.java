package template.cqrs.domain.alert.aggregate.commands;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

@Value
@Builder
public class ResolveAlertCommand {
    @TargetAggregateIdentifier
    @NotNull(message = "Alert ID cannot be null")
    UUID alertId;

    @NotBlank(message = "ResolvedBy user cannot be blank")
    String resolvedBy;

    @NotBlank(message = "Resolution details cannot be blank")
    String resolutionDetails;
}

