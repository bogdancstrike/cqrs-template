package template.cqrs.infrastructure.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssignAlertRequestDto {
    @NotBlank(message = "Assignee cannot be blank")
    private String assignee;
    private String assignedBy; // Optional
}