package template.cqrs.infrastructure.kafka.consumer;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import template.cqrs.domain.model.alert.commands.CreateAlertCommand;
import template.cqrs.domain.model.alert.value_objects.AlertDetails;
import template.cqrs.infrastructure.kafka.dto.IncomingAlertMessageDto;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertInputKafkaConsumer {

    private final CommandGateway commandGateway;
    private final Validator validator; // For validating the DTO

    @Value("${app.kafka.topic.alerts.input}")
    private String alertsInputTopic;

    // Consider using a DeadLetterPublishingRecoverer for more robust error handling
    @KafkaListener(
            topics = "${app.kafka.topic.alerts.input}",
            groupId = "${spring.kafka.consumer.group-id}", // Ensure this matches application.properties
            containerFactory = "kafkaListenerContainerFactory" // Default, or specify if custom
    )
    public void receiveAlertMessage(@Payload IncomingAlertMessageDto message,
                                    @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                    @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                    @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("Received message from Kafka: topic={}, partition={}, offset={}, payload={}",
                topic, partition, offset, message);

        // 1. Validate the incoming DTO
        Set<ConstraintViolation<IncomingAlertMessageDto>> violations = validator.validate(message);
        if (!violations.isEmpty()) {
            violations.forEach(violation -> log.error("Validation error for IncomingAlertMessageDto (offset {}): {} - {}",
                    offset, violation.getPropertyPath(), violation.getMessage()));
            // Decide error strategy: DLQ, log and skip, etc.
            // For now, just logging and skipping.
            return;
        }

        // 2. Transform DTO to Command
        // Generate a unique ID for the new alert.
        // The messageId from Kafka could be used for idempotency checks if needed, but alertId should be unique.
        UUID alertId = UUID.randomUUID();

        CreateAlertCommand command = CreateAlertCommand.builder()
                .alertId(alertId)
                .severity(message.getSeverity())
                .description(message.getDescription())
                .source("KafkaInput-" + message.getSourceSystem()) // Prefix to indicate origin
                .details(new AlertDetails(message.getDetails())) // Wrap map in Value Object
                .eventTimestamp(message.getTimestamp()) // Timestamp from the source event
                .initiatedBy("KafkaConsumer:" + message.getMessageId()) // Audit who/what initiated
                .build();

        // 3. Send Command via CommandGateway
        log.debug("Sending CreateAlertCommand for alertId {}: {}", alertId, command);
        try {
            // Asynchronous dispatch, returns a CompletableFuture
            CompletableFuture<Object> future = commandGateway.send(command);

            future.whenComplete((result, exception) -> {
                if (exception != null) {
                    log.error("Error processing CreateAlertCommand for alertId {} (Kafka offset {}): {}",
                            alertId, offset, exception.getMessage(), exception);
                    // Handle command execution failure: retry, DLQ, log.
                    // This indicates an issue in the command handler or aggregate.
                } else {
                    log.info("CreateAlertCommand for alertId {} (Kafka offset {}) processed successfully. Result: {}",
                            alertId, offset, result);
                    // Kafka offset will be committed automatically by Spring Kafka if processing is successful
                    // and no exception is thrown from this listener method.
                }
            });
        } catch (Exception e) {
            // This catch block is for synchronous exceptions from commandGateway.send() itself (rare)
            log.error("Failed to send CreateAlertCommand for alertId {} (Kafka offset {}): {}",
                    alertId, offset, e.getMessage(), e);
            // Consider how to handle this - might require manual offset management or re-throwing to trigger Kafka error handlers
        }
    }
}
