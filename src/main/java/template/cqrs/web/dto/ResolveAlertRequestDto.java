package template.cqrs.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResolveAlertRequestDto {
    @NotBlank(message = "ResolvedBy user cannot be blank")
    private String resolvedBy;
    @NotBlank(message = "Resolution details cannot be blank")
    private String resolutionDetails;
}