package template.cqrs.infrastructure.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import template.cqrs.domain.alert.aggregate.commands.*;
import template.cqrs.domain.alert.value_objects.AlertDetails;
import template.cqrs.api.dto.*;
import template.cqrs.infrastructure.api.dto.*;
import template.cqrs.infrastructure.kafka.api.dto.*;

import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Alert Commands API", description = "Endpoints for creating and modifying alerts.")
public class AlertCommandController {

    private final CommandGateway commandGateway;

    @PostMapping
    @Operation(summary = "Create a new alert",
            description = "Creates a new alert in the system. Returns the ID of the created alert.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Alert created successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            })
    public CompletableFuture<ResponseEntity<Map<String, String>>> createAlert(
            @Valid @RequestBody CreateAlertRequestDto requestDto) {
        log.info("Received request to create alert: {}", requestDto);

        UUID alertId = UUID.randomUUID(); // Generate ID here or let the command/aggregate do it
        CreateAlertCommand command = CreateAlertCommand.builder()
                .alertId(alertId)
                .severity(requestDto.getSeverity())
                .description(requestDto.getDescription())
                .source(requestDto.getSource() != null ? requestDto.getSource() : "APISource")
                .details(new AlertDetails(requestDto.getDetails()))
                .eventTimestamp(requestDto.getEventTimestamp()) // Can be null
                .initiatedBy(requestDto.getInitiatedBy() != null ? requestDto.getInitiatedBy() : "APIUser")
                .build();

        return commandGateway.send(command)
                .thenApply(result -> {
                    UUID resultId = (UUID) result; // Assuming command handler returns the ID
                    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                            .path("/{id}")
                            .buildAndExpand(resultId)
                            .toUri();
                    log.info("Alert created successfully with ID: {}", resultId);
                    return ResponseEntity.created(location).body(Map.of("alertId", resultId.toString()));
                })
                .exceptionally(ex -> {
                    log.error("Error creating alert: {}", ex.getMessage(), ex);
                    // Distinguish between validation errors (client) and other errors (server)
                    // For simplicity, returning 500 for any command execution error.
                    // A more sophisticated error handler would map Axon exceptions to HTTP statuses.
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("error", "Failed to create alert: " + ex.getMessage()));
                });
    }

    @PutMapping("/{alertId}")
    @Operation(summary = "Update an existing alert",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Alert updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input or alert ID"),
                    @ApiResponse(responseCode = "404", description = "Alert not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            })
    public CompletableFuture<ResponseEntity<Void>> updateAlert(
            @Parameter(description = "ID of the alert to update") @PathVariable UUID alertId,
            @Valid @RequestBody UpdateAlertRequestDto requestDto) {
        log.info("Received request to update alert {}: {}", alertId, requestDto);

        UpdateAlertCommand command = UpdateAlertCommand.builder()
                .alertId(alertId)
                .severity(requestDto.getSeverity())
                .description(requestDto.getDescription())
                .details(requestDto.getDetails() != null ? new AlertDetails(requestDto.getDetails()) : null)
                .updatedBy(requestDto.getUpdatedBy() != null ? requestDto.getUpdatedBy() : "APIUser")
                .build();

        return commandGateway.send(command)
                .thenApply(result -> ResponseEntity.ok().<Void>build())
                .exceptionally(ex -> handleCommandException(ex, "update alert " + alertId));
    }

    @PostMapping("/{alertId}/acknowledge")
    @Operation(summary = "Acknowledge an alert",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Alert acknowledged successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input or alert ID"),
                    @ApiResponse(responseCode = "404", description = "Alert not found / cannot be acknowledged"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            })
    public CompletableFuture<ResponseEntity<Void>> acknowledgeAlert(
            @PathVariable UUID alertId,
            @Valid @RequestBody AcknowledgeAlertRequestDto requestDto) {
        log.info("Received request to acknowledge alert {}: {}", alertId, requestDto);
        AcknowledgeAlertCommand command = AcknowledgeAlertCommand.builder()
                .alertId(alertId)
                .acknowledgedBy(requestDto.getAcknowledgedBy())
                .notes(requestDto.getNotes())
                .build();
        return commandGateway.send(command)
                .thenApply(result -> ResponseEntity.ok().<Void>build())
                .exceptionally(ex -> handleCommandException(ex, "acknowledge alert " + alertId));
    }

    @PostMapping("/{alertId}/resolve")
    @Operation(summary = "Resolve an alert")
    public CompletableFuture<ResponseEntity<Void>> resolveAlert(
            @PathVariable UUID alertId,
            @Valid @RequestBody ResolveAlertRequestDto requestDto) {
        log.info("Received request to resolve alert {}: {}", alertId, requestDto);
        ResolveAlertCommand command = ResolveAlertCommand.builder()
                .alertId(alertId)
                .resolvedBy(requestDto.getResolvedBy())
                .resolutionDetails(requestDto.getResolutionDetails())
                .build();
        return commandGateway.send(command)
                .thenApply(result -> ResponseEntity.ok().<Void>build())
                .exceptionally(ex -> handleCommandException(ex, "resolve alert " + alertId));
    }

    @PostMapping("/{alertId}/close")
    @Operation(summary = "Close an alert")
    public CompletableFuture<ResponseEntity<Void>> closeAlert(
            @PathVariable UUID alertId,
            @Valid @RequestBody CloseAlertRequestDto requestDto) {
        log.info("Received request to close alert {}: {}", alertId, requestDto);
        CloseAlertCommand command = CloseAlertCommand.builder()
                .alertId(alertId)
                .closedBy(requestDto.getClosedBy())
                .reason(requestDto.getReason())
                .build();
        return commandGateway.send(command)
                .thenApply(result -> ResponseEntity.ok().<Void>build())
                .exceptionally(ex -> handleCommandException(ex, "close alert " + alertId));
    }

    @PostMapping("/{alertId}/notes")
    @Operation(summary = "Add a note to an alert")
    public CompletableFuture<ResponseEntity<Void>> addNoteToAlert(
            @PathVariable UUID alertId,
            @Valid @RequestBody AddNoteRequestDto requestDto) {
        log.info("Received request to add note to alert {}: {}", alertId, requestDto);
        AddNoteToAlertCommand command = AddNoteToAlertCommand.builder()
                .alertId(alertId)
                .text(requestDto.getText())
                .author(requestDto.getAuthor())
                .build();
        return commandGateway.send(command)
                .thenApply(result -> ResponseEntity.ok().<Void>build())
                .exceptionally(ex -> handleCommandException(ex, "add note to alert " + alertId));
    }

    @PostMapping("/{alertId}/assign")
    @Operation(summary = "Assign an alert")
    public CompletableFuture<ResponseEntity<Void>> assignAlert(
            @PathVariable UUID alertId,
            @Valid @RequestBody AssignAlertRequestDto requestDto) {
        log.info("Received request to assign alert {}: {}", alertId, requestDto);
        AssignAlertCommand command = AssignAlertCommand.builder()
                .alertId(alertId)
                .assignee(requestDto.getAssignee())
                .assignedBy(requestDto.getAssignedBy())
                .build();
        return commandGateway.send(command)
                .thenApply(result -> ResponseEntity.ok().<Void>build())
                .exceptionally(ex -> handleCommandException(ex, "assign alert " + alertId));
    }


    @DeleteMapping("/{alertId}")
    @Operation(summary = "Logically delete an alert",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Alert deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Alert not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            })
    public CompletableFuture<ResponseEntity<Void>> deleteAlert(
            @Parameter(description = "ID of the alert to delete") @PathVariable UUID alertId,
            @RequestParam(required = false) String deletedBy,
            @RequestParam(required = false) String reason) {
        log.info("Received request to delete alert {}", alertId);

        DeleteAlertCommand command = DeleteAlertCommand.builder()
                .alertId(alertId)
                .deletedBy(deletedBy != null ? deletedBy : "APIUser")
                .reason(reason)
                .build();

        return commandGateway.send(command)
                .thenApply(result -> ResponseEntity.noContent().<Void>build())
                .exceptionally(ex -> handleCommandException(ex, "delete alert " + alertId));
    }

    // Centralized exception handling for command futures
    private ResponseEntity<Void> handleCommandException(Throwable ex, String action) {
        log.error("Error during {}: {}", action, ex.getMessage(), ex.getCause()); // Log cause for Axon exceptions
        // Basic mapping: Axon's AggregateNotFoundException could be a 404.
        // IllegalStateException from aggregate could be 400 or 409 (Conflict).
        // For simplicity, most are mapped to 500.
        // Consider using @ControllerAdvice for more global and refined exception handling.
        if (ex.getCause() instanceof org.axonframework.modelling.command.AggregateNotFoundException) {
            return ResponseEntity.notFound().build();
        }
        if (ex.getCause() instanceof IllegalStateException || ex.getCause() instanceof IllegalArgumentException) {
            return ResponseEntity.badRequest().build(); // Or build with error message
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}