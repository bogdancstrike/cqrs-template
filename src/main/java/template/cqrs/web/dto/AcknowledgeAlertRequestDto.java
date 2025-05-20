package template.cqrs.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AcknowledgeAlertRequestDto {
    @NotBlank(message = "AcknowledgedBy user cannot be blank")
    private String acknowledgedBy;
    private String notes; // Optional
}