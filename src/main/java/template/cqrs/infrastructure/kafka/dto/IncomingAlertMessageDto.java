package template.cqrs.infrastructure.kafka.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import template.cqrs.core.value_objects.AlertSeverity;

import java.time.Instant;
import java.util.Map;

/**
 * DTO representing the expected structure of a message from the 'alerts-input-topic'.
 * This DTO will be deserialized from the Kafka message payload.
 */
@Getter
@ToString
// Using @Getter and constructor instead of @Value to allow for @JsonCreator with @JsonProperty
// Or use @Value and ensure Jackson can deserialize it (e.g., if using Lombok's @Builder with @JsonPOJOBuilder)
public class IncomingAlertMessageDto {

    @NotBlank(message = "Message ID cannot be blank")
    private final String messageId; // Unique ID from the source of the message

    @NotBlank(message = "Source system cannot be blank")
    private final String sourceSystem;

    @NotNull(message = "Severity cannot be null")
    private final AlertSeverity severity; // Reusing the enum from core-api

    @NotBlank(message = "Description cannot be blank")
    @Size(min = 5, max = 1000)
    private final String description;

    @NotNull(message = "Timestamp cannot be null")
    private final Instant timestamp; // Timestamp of the event occurrence at the source

    // Using Map<String, Object> for flexibility, will be wrapped in AlertDetails VO
    private final Map<String, Object> details;

    @JsonCreator // Helps Jackson to correctly deserialize
    @Builder // Lombok builder for easier construction, especially in tests
    public IncomingAlertMessageDto(
            @JsonProperty("messageId") String messageId,
            @JsonProperty("sourceSystem") String sourceSystem,
            @JsonProperty("severity") AlertSeverity severity,
            @JsonProperty("description") String description,
            @JsonProperty("timestamp") Instant timestamp,
            @JsonProperty("details") Map<String, Object> details) {
        this.messageId = messageId;
        this.sourceSystem = sourceSystem;
        this.severity = severity;
        this.description = description;
        this.timestamp = timestamp;
        this.details = details; // Can be null
    }
}
