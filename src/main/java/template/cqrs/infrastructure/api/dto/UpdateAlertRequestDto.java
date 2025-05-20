package template.cqrs.infrastructure.api.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import template.cqrs.domain.model.alert.value_objects.AlertSeverity;

import java.util.Map;

@Data
public class UpdateAlertRequestDto {
    private AlertSeverity severity; // Optional: only include if updating

    @Size(min = 5, max = 1000)
    private String description; // Optional

    private Map<String, Object> details; // Optional

    private String updatedBy; // Optional
}
