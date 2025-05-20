package template.cqrs.infrastructure.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CloseAlertRequestDto {
    @NotBlank(message = "ClosedBy user cannot be blank")
    private String closedBy;
    private String reason; // Optional
}
