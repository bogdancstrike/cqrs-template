package template.cqrs.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import template.cqrs.coreapi.common.AlertSeverity;

import java.util.Map;

@Data
public class UpdateAlertRequestDto {
    private AlertSeverity severity; // Optional: only include if updating

    @Size(min = 5, max = 1000)
    private String description; // Optional

    private Map<String, Object> details; // Optional

    private String updatedBy; // Optional
}
