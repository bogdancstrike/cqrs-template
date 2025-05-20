package template.cqrs.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import template.cqrs.coreapi.common.AlertSeverity;

import java.time.Instant;
import java.util.Map;

@Data
public class CreateAlertRequestDto {
    @NotNull(message = "Severity cannot be null")
    private AlertSeverity severity;

    @NotBlank(message = "Description cannot be blank")
    @Size(min = 5, max = 1000)
    private String description;

    @Size(max = 255)
    private String source; // Optional, can be defaulted

    private Map<String, Object> details; // Flexible details

    // Removed @JsonFormat to allow Jackson's default Instant deserialization,
    // which is more flexible with ISO 8601 formats (with/without milliseconds).
    private Instant eventTimestamp; // Optional

    private String initiatedBy; // Optional
}