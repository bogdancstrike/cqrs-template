package template.cqrs.web.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import template.cqrs.coreapi.common.AlertStatus;
import template.cqrs.coreapi.dto.AlertDto;
import template.cqrs.coreapi.dto.PagedAlertResponse;
import template.cqrs.coreapi.queries.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Alert Query API", description = "Endpoints for querying alerts.")
public class AlertQueryController {

    private final QueryGateway queryGateway;

    @GetMapping("/{alertId}")
    @Operation(summary = "Get an alert by its ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Alert found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AlertDto.class))),
                    @ApiResponse(responseCode = "404", description = "Alert not found")
            })
    public CompletableFuture<ResponseEntity<AlertDto>> getAlertById(
            @Parameter(description = "ID of the alert to retrieve") @PathVariable UUID alertId) {
        log.debug("Received request to get alert by ID: {}", alertId);
        FindAlertByIdQuery query = new FindAlertByIdQuery(alertId);

        return queryGateway.query(query, ResponseTypes.optionalInstanceOf(AlertDto.class))
                .thenApply(optionalResult -> optionalResult
                        .map(ResponseEntity::ok)
                        .orElseGet(() -> ResponseEntity.notFound().build()));
    }

    @GetMapping
    @Operation(summary = "Get all alerts with pagination",
            description = "Retrieves a paginated list of all alerts, sorted by creation date descending by default.")
    public CompletableFuture<PagedAlertResponse> getAllAlerts(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of alerts per page") @RequestParam(defaultValue = "10") int size) {
        log.debug("Received request to get all alerts: page={}, size={}", page, size);
        FindAllAlertsQuery query = new FindAllAlertsQuery(page, size);
        return queryGateway.query(query, ResponseTypes.instanceOf(PagedAlertResponse.class));
    }

    @GetMapping("/search")
    @Operation(summary = "Find alerts by keyword",
            description = "Searches for alerts where the keyword matches in description or source.")
    public CompletableFuture<PagedAlertResponse> findAlertsByKeyword(
            @Parameter(description = "Keyword to search for") @RequestParam String keyword,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of alerts per page") @RequestParam(defaultValue = "10") int size) {
        log.debug("Received request to find alerts by keyword: {}, page={}, size={}", keyword, page, size);
        FindAlertsByKeywordQuery query = new FindAlertsByKeywordQuery(keyword, page, size);
        return queryGateway.query(query, ResponseTypes.instanceOf(PagedAlertResponse.class));
    }

    @GetMapping("/filter")
    @Operation(summary = "Find alerts by timestamp range",
            description = "Retrieves alerts created within the specified timestamp range (inclusive). Timestamps should be in ISO 8601 format.")
    public CompletableFuture<PagedAlertResponse> findAlertsByTimestampRange(
            @Parameter(description = "Start timestamp (ISO 8601 format, e.g., 2023-01-01T00:00:00Z)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @Parameter(description = "End timestamp (ISO 8601 format, e.g., 2023-01-31T23:59:59Z)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of alerts per page") @RequestParam(defaultValue = "10") int size) {
        log.debug("Received request to find alerts by timestamp range: {} - {}, page={}, size={}", startTime, endTime, page, size);
        FindAlertsByTimestampRangeQuery query = new FindAlertsByTimestampRangeQuery(startTime, endTime, page, size);
        return queryGateway.query(query, ResponseTypes.instanceOf(PagedAlertResponse.class));
    }

    @GetMapping("/status")
    @Operation(summary = "Find alerts by status")
    public CompletableFuture<PagedAlertResponse> findAlertsByStatus(
            @Parameter(description = "Alert status to filter by", required = true) @RequestParam AlertStatus status,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of alerts per page") @RequestParam(defaultValue = "10") int size) {
        log.debug("Received request to find alerts by status: {}, page={}, size={}", status, page, size);
        FindAlertsByStatusQuery query = new FindAlertsByStatusQuery(status, page, size);
        return queryGateway.query(query, ResponseTypes.instanceOf(PagedAlertResponse.class));
    }
}
