package template.cqrs.query.projection;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.axonframework.eventhandling.Timestamp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import template.cqrs.coreapi.common.AlertNoteDto;
import template.cqrs.coreapi.events.*;
import template.cqrs.query.document.AlertDocument;
import template.cqrs.query.repository.AlertDocumentRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@ProcessingGroup("alert-projection-group")
@Slf4j
public class AlertProjection {

    private final AlertDocumentRepository repository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Value("${app.projection.batch.size:100}")
    private int batchSize;

    @Value("${app.projection.batch.timeout-ms:120000}")
    private long batchTimeoutMs;

    // bulkUpdateQueue should store UpdateQuery if that's what you're adding.
    // Or more generally, org.springframework.data.elasticsearch.core.query.Query
    // For simplicity and type safety with bulkUpdate, let's make it List<UpdateQuery>
    private final List<UpdateQuery> bulkUpdateQueue = new ArrayList<>();
    private ScheduledExecutorService scheduler;
    private volatile long lastFlushTimeMs;

    private static final String ALERTS_INDEX_NAME = "alerts";

    @PostConstruct
    public void init() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        lastFlushTimeMs = System.currentTimeMillis();
        // Check for timeout more frequently than the timeout itself to avoid drift
        long checkInterval = Math.min(batchTimeoutMs / 2, 5000); // e.g., every 5s or half timeout
        scheduler.scheduleWithFixedDelay(this::timedFlush, checkInterval, checkInterval, TimeUnit.MILLISECONDS);
        log.info("AlertProjection initialized with batchSize={}, batchTimeoutMs={}", batchSize, batchTimeoutMs);
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down AlertProjection. Flushing pending updates...");
        flushUpdates(); // Ensure any remaining updates are flushed
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) { // Increased timeout
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        log.info("AlertProjection shutdown complete.");
    }

    private void addToBulkQueue(UpdateQuery updateQuery) {
        synchronized (bulkUpdateQueue) {
            bulkUpdateQueue.add(updateQuery); // Add UpdateQuery directly
            if (bulkUpdateQueue.size() >= batchSize) {
                log.debug("Batch size reached ({}). Flushing updates.", bulkUpdateQueue.size());
                flushUpdates();
            }
        }
    }

    private void timedFlush() {
        synchronized (bulkUpdateQueue) {
            if (!bulkUpdateQueue.isEmpty() && (System.currentTimeMillis() - lastFlushTimeMs >= batchTimeoutMs)) {
                log.info("Batch timeout reached ({}ms). Flushing {} updates.", batchTimeoutMs, bulkUpdateQueue.size());
                flushUpdates();
            }
        }
    }

    private void flushUpdates() {
        List<UpdateQuery> toProcess;
        synchronized (bulkUpdateQueue) {
            if (bulkUpdateQueue.isEmpty()) {
                return;
            }
            toProcess = new ArrayList<>(bulkUpdateQueue);
            bulkUpdateQueue.clear();
            lastFlushTimeMs = System.currentTimeMillis(); // Reset timer after clearing queue
        }

        if (!toProcess.isEmpty()) {
            try {
                elasticsearchOperations.bulkUpdate(toProcess, IndexCoordinates.of(ALERTS_INDEX_NAME));
                log.info("Successfully flushed {} updates to Elasticsearch.", toProcess.size());
            } catch (Exception e) {
                log.error("Error flushing batch updates to Elasticsearch. Updates count: {}. Error: {}", toProcess.size(), e.getMessage(), e);
                // Implement more robust error handling: e.g., re-queue, DLQ, specific exception handling
                // For example, re-queueing (be careful with potential infinite loops for non-transient errors):
                // synchronized (bulkUpdateQueue) {
                //     bulkUpdateQueue.addAll(0, toProcess); // Add back to the front for next attempt
                // }
            }
        }
    }

    @EventHandler
    public void on(AlertCreatedEvent event, @Timestamp Instant eventTimestampIngestion) {
        log.debug("Projecting AlertCreatedEvent: {}", event.getAlertId());
        // AlertDocument.builder() requires @Builder on AlertDocument class
        AlertDocument document = AlertDocument.builder()
                .alertId(event.getAlertId().toString())
                .severity(event.getSeverity())
                .description(event.getDescription())
                .source(event.getSource())
                .details(event.getDetails())
                .status(event.getInitialStatus())
                .createdAt(event.getCreatedAt())
                .eventTimestamp(event.getEventTimestamp())
                .updatedAt(event.getCreatedAt())
                .initiatedBy(event.getInitiatedBy())
                .notes(new ArrayList<>())
                .build();
        try {
            repository.save(document);
            log.info("Alert document {} created in Elasticsearch.", event.getAlertId());
        } catch (Exception e) {
            log.error("Error saving new AlertDocument {} to Elasticsearch: {}", event.getAlertId(), e.getMessage(), e);
        }
    }

    private UpdateQuery createUpdateQuery(String alertId, Map<String, Object> updates) {
        Document updateDocument = Document.from(updates);
        return UpdateQuery.builder(alertId)
                .withDocument(updateDocument)
                .withDocAsUpsert(false)
                .withRetryOnConflict(3)
                .build();
    }

    private List<AlertNoteDto> getNotesFromDocument(AlertDocument doc) {
        return doc.getNotes() == null ? new ArrayList<>() : new ArrayList<>(doc.getNotes());
    }

    @EventHandler
    public void on(AlertUpdatedEvent event, @Timestamp Instant eventTimestampIngestion) {
        log.debug("Projecting AlertUpdatedEvent: {}", event.getAlertId());
        Map<String, Object> updates = Map.of(
                "severity", event.getSeverity(),
                "description", event.getDescription(),
                "details", event.getDetails(),
                "updatedAt", event.getUpdatedAt(),
                "updatedBy", event.getUpdatedBy()
        );
        addToBulkQueue(createUpdateQuery(event.getAlertId().toString(), updates));
    }

    @EventHandler
    public void on(AlertAcknowledgedEvent event) {
        log.debug("Projecting AlertAcknowledgedEvent: {}", event.getAlertId());
        Map<String, Object> updates = Map.of(
                "status", event.getNewStatus(),
                "acknowledgedAt", event.getAcknowledgedAt(),
                "acknowledgedBy", event.getAcknowledgedBy(),
                "acknowledgementNotes", event.getNotes() != null ? event.getNotes() : "",
                "updatedAt", event.getAcknowledgedAt()
        );
        addToBulkQueue(createUpdateQuery(event.getAlertId().toString(), updates));
    }

    @EventHandler
    public void on(AlertResolvedEvent event) {
        log.debug("Projecting AlertResolvedEvent: {}", event.getAlertId());
        Map<String, Object> updates = Map.of(
                "status", event.getNewStatus(),
                "resolvedAt", event.getResolvedAt(),
                "resolvedBy", event.getResolvedBy(),
                "resolutionDetails", event.getResolutionDetails(),
                "updatedAt", event.getResolvedAt()
        );
        addToBulkQueue(createUpdateQuery(event.getAlertId().toString(), updates));
    }

    @EventHandler
    public void on(AlertClosedEvent event) {
        log.debug("Projecting AlertClosedEvent: {}", event.getAlertId());
        Map<String, Object> updates = Map.of(
                "status", event.getNewStatus(),
                "closedAt", event.getClosedAt(),
                "closedBy", event.getClosedBy(),
                "closingReason", event.getReason() != null ? event.getReason() : "",
                "updatedAt", event.getClosedAt()
        );
        addToBulkQueue(createUpdateQuery(event.getAlertId().toString(), updates));
    }

    @EventHandler
    public void on(NoteAddedToAlertEvent event) {
        log.debug("Projecting NoteAddedToAlertEvent for alert: {}", event.getAlertId());
        Optional<AlertDocument> existingDocOpt = repository.findById(event.getAlertId().toString());
        if (existingDocOpt.isPresent()) {
            AlertDocument doc = existingDocOpt.get();
            List<AlertNoteDto> notes = getNotesFromDocument(doc); // Ensures notes list is mutable
            notes.add(event.getNote());

            Map<String, Object> updates = Map.of(
                    "notes", notes, // Use the updated list
                    "updatedAt", event.getNote().getTimestamp()
            );
            addToBulkQueue(createUpdateQuery(event.getAlertId().toString(), updates));
        } else {
            log.warn("AlertDocument not found for NoteAddedToAlertEvent, alertId: {}. Note will not be added to projection.", event.getAlertId());
        }
    }

    @EventHandler
    public void on(AlertAssignedEvent event) {
        log.debug("Projecting AlertAssignedEvent: {}", event.getAlertId());
        Map<String, Object> updates = Map.of(
                "assignee", event.getAssignee(),
                "assignedAt", event.getAssignedAt(),
                "assignedBy", event.getAssignedBy(),
                "updatedAt", event.getAssignedAt()
        );
        addToBulkQueue(createUpdateQuery(event.getAlertId().toString(), updates));
    }

    @EventHandler
    public void on(AlertDeletedEvent event) {
        log.debug("Projecting AlertDeletedEvent: {}", event.getAlertId());
        Map<String, Object> updates = Map.of(
                "status", event.getNewStatus(),
                "deletedAt", event.getDeletedAt(),
                "deletedBy", event.getDeletedBy(),
                "deletionReason", event.getReason() != null ? event.getReason() : "",
                "updatedAt", event.getDeletedAt()
        );
        addToBulkQueue(createUpdateQuery(event.getAlertId().toString(), updates));
    }

    @ResetHandler
    public void onReset() {
        log.info("Resetting AlertProjection. Clearing Elasticsearch index: {}", ALERTS_INDEX_NAME);
        IndexCoordinates indexCoordinates = IndexCoordinates.of(ALERTS_INDEX_NAME);
        if (elasticsearchOperations.indexOps(indexCoordinates).exists()) {
            log.warn("Deleting existing Elasticsearch index: {}", ALERTS_INDEX_NAME);
            elasticsearchOperations.indexOps(indexCoordinates).delete();
        }
        log.info("Creating Elasticsearch index: {} with mappings for AlertDocument.", ALERTS_INDEX_NAME);
        elasticsearchOperations.indexOps(indexCoordinates).create();
        // Ensure mapping is applied. This might also be handled by Spring Data Elasticsearch automatically
        // based on @Document annotation if index doesn't exist or if auto-create/update is enabled.
        elasticsearchOperations.indexOps(indexCoordinates).putMapping(AlertDocument.class);
        log.info("Elasticsearch index {} reset and mapping applied.", ALERTS_INDEX_NAME);
    }
}
