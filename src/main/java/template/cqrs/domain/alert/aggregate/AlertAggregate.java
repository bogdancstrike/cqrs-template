package template.cqrs.domain.alert.aggregate;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.util.Assert; // Spring's Assert for preconditions
import template.cqrs.domain.aggregate.commands.*;
import template.cqrs.domain.aggregate.events.*;
import template.cqrs.domain.alert.aggregate.commands.*;
import template.cqrs.domain.alert.aggregate.events.*;
import template.cqrs.domain.alert.value_objects.AlertDetails;
import template.cqrs.domain.alert.value_objects.AlertNoteDto;
import template.cqrs.domain.alert.value_objects.AlertSeverity;
import template.cqrs.domain.alert.value_objects.AlertStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Aggregate // Marks this class as an Axon Aggregate
@NoArgsConstructor(access = AccessLevel.PROTECTED) // Required by Axon for instantiation; protected for DDD
@Getter // Lombok to generate getters for state fields
@Slf4j // For logging
public class AlertAggregate {

    @AggregateIdentifier // Marks this field as the unique identifier of the aggregate instance
    private UUID alertId;

    private AlertSeverity severity;
    private String description;
    private String source;
    private AlertStatus status;
    private AlertDetails details;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant eventTimestamp; // Timestamp from the original source event

    private String initiatedBy;
    private String updatedBy;

    private Instant acknowledgedAt;
    private String acknowledgedBy;
    private String acknowledgementNotes;

    private Instant resolvedAt;
    private String resolvedBy;
    private String resolutionDetails;

    private Instant closedAt;
    private String closedBy;
    private String closingReason;

    private String assignee;
    private Instant assignedAt;
    private String assignedBy; // User who performed the assignment

    private List<AlertNoteDto> notes;

    private String deletedBy; // For logical deletion
    private Instant deletedAt;
    private String deletionReason;

    //region Command Handlers

    /**
     * Handles the CreateAlertCommand.
     * This constructor is a command handler for creating new AlertAggregate instances.
     *
     * @param command The command to create an alert.
     */
    @CommandHandler
    public AlertAggregate(CreateAlertCommand command) {
        log.debug("Handling CreateAlertCommand: {}", command);

        // Basic Validations (more complex ones can be in a dedicated validator or service)
        Assert.notNull(command.getAlertId(), "Alert ID must not be null for CreateAlertCommand");
        Assert.notNull(command.getSeverity(), "Severity must not be null");
        Assert.hasText(command.getDescription(), "Description must not be empty");
        Assert.hasText(command.getSource(), "Source must not be empty");

        // Apply the AlertCreatedEvent
        AggregateLifecycle.apply(AlertCreatedEvent.builder()
                .alertId(command.getAlertId())
                .severity(command.getSeverity())
                .description(command.getDescription())
                .source(command.getSource())
                .details(command.getDetails() != null ? command.getDetails() : new AlertDetails(null))
                .initialStatus(AlertStatus.ACTIVE) // Default initial status
                .createdAt(Instant.now()) // System timestamp for creation
                .eventTimestamp(command.getEventTimestamp() != null ? command.getEventTimestamp() : Instant.now())
                .initiatedBy(command.getInitiatedBy())
                .build());
        log.info("AlertCreatedEvent applied for alertId: {}", command.getAlertId());
    }

    @CommandHandler
    public void handle(UpdateAlertCommand command) {
        log.debug("Handling UpdateAlertCommand: {}", command);
        Assert.notNull(command.getAlertId(), "Alert ID must not be null for UpdateAlertCommand");
        // Ensure the aggregate is in a state where it can be updated
        if (this.status == AlertStatus.CLOSED || this.status == AlertStatus.DELETED) {
            throw new IllegalStateException("Cannot update an alert that is already " + this.status);
        }

        // Check if there's anything to update
        boolean isSeverityUpdated = command.getSeverity() != null && !Objects.equals(this.severity, command.getSeverity());
        boolean isDescriptionUpdated = command.getDescription() != null && !Objects.equals(this.description, command.getDescription());
        boolean isDetailsUpdated = command.getDetails() != null && !Objects.equals(this.details, command.getDetails());

        if (!isSeverityUpdated && !isDescriptionUpdated && !isDetailsUpdated) {
            log.warn("UpdateAlertCommand received for alertId: {} with no changes.", command.getAlertId());
            // Optionally, do nothing or throw an exception if updates must change something.
            // For this template, we'll allow "no-op" updates but log them.
            return;
        }

        AggregateLifecycle.apply(AlertUpdatedEvent.builder()
                .alertId(this.alertId)
                .severity(isSeverityUpdated ? command.getSeverity() : this.severity)
                .description(isDescriptionUpdated ? command.getDescription() : this.description)
                .details(isDetailsUpdated ? command.getDetails() : this.details)
                .updatedAt(Instant.now())
                .updatedBy(command.getUpdatedBy())
                .build());
        log.info("AlertUpdatedEvent applied for alertId: {}", this.alertId);
    }

    @CommandHandler
    public void handle(AcknowledgeAlertCommand command) {
        log.debug("Handling AcknowledgeAlertCommand: {}", command);
        Assert.notNull(command.getAlertId(), "Alert ID cannot be null");
        Assert.hasText(command.getAcknowledgedBy(), "AcknowledgedBy user cannot be blank");

        if (this.status != AlertStatus.ACTIVE) {
            throw new IllegalStateException("Alert " + this.alertId + " cannot be acknowledged. Current status: " + this.status);
        }

        AggregateLifecycle.apply(AlertAcknowledgedEvent.builder()
                .alertId(this.alertId)
                .acknowledgedBy(command.getAcknowledgedBy())
                .acknowledgedAt(Instant.now())
                .newStatus(AlertStatus.ACKNOWLEDGED)
                .notes(command.getNotes())
                .build());
        log.info("AlertAcknowledgedEvent applied for alertId: {}", this.alertId);
    }

    @CommandHandler
    public void handle(ResolveAlertCommand command) {
        log.debug("Handling ResolveAlertCommand: {}", command);
        Assert.notNull(command.getAlertId(), "Alert ID cannot be null");
        Assert.hasText(command.getResolvedBy(), "ResolvedBy user cannot be blank");
        Assert.hasText(command.getResolutionDetails(), "Resolution details cannot be blank");

        if (this.status != AlertStatus.ACTIVE && this.status != AlertStatus.ACKNOWLEDGED) {
            throw new IllegalStateException("Alert " + this.alertId + " cannot be resolved. Current status: " + this.status);
        }

        AggregateLifecycle.apply(AlertResolvedEvent.builder()
                .alertId(this.alertId)
                .resolvedBy(command.getResolvedBy())
                .resolutionDetails(command.getResolutionDetails())
                .resolvedAt(Instant.now())
                .newStatus(AlertStatus.RESOLVED)
                .build());
        log.info("AlertResolvedEvent applied for alertId: {}", this.alertId);
    }

    @CommandHandler
    public void handle(CloseAlertCommand command) {
        log.debug("Handling CloseAlertCommand: {}", command);
        Assert.notNull(command.getAlertId(), "Alert ID cannot be null");
        Assert.hasText(command.getClosedBy(), "ClosedBy user cannot be blank");

        if (this.status == AlertStatus.CLOSED || this.status == AlertStatus.DELETED) {
            throw new IllegalStateException("Alert " + this.alertId + " is already " + this.status);
        }
        // Typically, an alert should be RESOLVED before being CLOSED, but business rules might allow closing ACTIVE/ACKNOWLEDGED alerts.
        // For this template, we allow closing from ACTIVE, ACKNOWLEDGED, RESOLVED.
        if (this.status != AlertStatus.ACTIVE && this.status != AlertStatus.ACKNOWLEDGED && this.status != AlertStatus.RESOLVED) {
            log.warn("Closing alert {} from status {}. Consider if this is intended.", this.alertId, this.status);
        }


        AggregateLifecycle.apply(AlertClosedEvent.builder()
                .alertId(this.alertId)
                .closedBy(command.getClosedBy())
                .closedAt(Instant.now())
                .newStatus(AlertStatus.CLOSED)
                .reason(command.getReason())
                .build());
        log.info("AlertClosedEvent applied for alertId: {}", this.alertId);
    }

    @CommandHandler
    public void handle(AddNoteToAlertCommand command) {
        log.debug("Handling AddNoteToAlertCommand: {}", command);
        Assert.notNull(command.getAlertId(), "Alert ID cannot be null");
        Assert.hasText(command.getText(), "Note text cannot be blank");
        Assert.hasText(command.getAuthor(), "Note author cannot be blank");

        if (this.status == AlertStatus.CLOSED || this.status == AlertStatus.DELETED) {
            throw new IllegalStateException("Cannot add note to an alert that is " + this.status);
        }

        AlertNoteDto newNote = new AlertNoteDto(UUID.randomUUID(), command.getText(), command.getAuthor(), Instant.now());

        AggregateLifecycle.apply(NoteAddedToAlertEvent.builder()
                .alertId(this.alertId)
                .note(newNote)
                .build());
        log.info("NoteAddedToAlertEvent applied for alertId: {}", this.alertId);
    }

    @CommandHandler
    public void handle(AssignAlertCommand command) {
        log.debug("Handling AssignAlertCommand: {}", command);
        Assert.notNull(command.getAlertId(), "Alert ID cannot be null");
        Assert.hasText(command.getAssignee(), "Assignee cannot be blank");

        if (this.status == AlertStatus.CLOSED || this.status == AlertStatus.DELETED) {
            throw new IllegalStateException("Cannot assign an alert that is " + this.status);
        }
        if (Objects.equals(this.assignee, command.getAssignee())) {
            log.warn("Alert {} is already assigned to {}. No change applied.", this.alertId, command.getAssignee());
            return; // Or throw an exception if re-assigning to the same user is not allowed/meaningful
        }

        AggregateLifecycle.apply(AlertAssignedEvent.builder()
                .alertId(this.alertId)
                .assignee(command.getAssignee())
                .assignedAt(Instant.now())
                .assignedBy(command.getAssignedBy()) // User performing the assignment
                .build());
        log.info("AlertAssignedEvent applied for alertId: {}", this.alertId);
    }


    @CommandHandler
    public void handle(DeleteAlertCommand command) {
        log.debug("Handling DeleteAlertCommand: {}", command);
        Assert.notNull(command.getAlertId(), "Alert ID cannot be null for DeleteAlertCommand");

        if (this.status == AlertStatus.DELETED) {
            throw new IllegalStateException("Alert " + this.alertId + " is already deleted.");
        }
        // Business rule: Allow deletion from any state, or only specific states?
        // For this template, allowing deletion from any non-deleted state.
        // Consider if "CLOSED" alerts can be "DELETED". Usually, deletion is a stronger action.

        AggregateLifecycle.apply(AlertDeletedEvent.builder()
                .alertId(this.alertId)
                .newStatus(AlertStatus.DELETED) // The status after deletion
                .deletedAt(Instant.now())
                .deletedBy(command.getDeletedBy())
                .reason(command.getReason())
                .build());
        log.info("AlertDeletedEvent applied for alertId: {}", this.alertId);
    }

    //endregion


    //region Event Sourcing Handlers

    /**
     * Applies the AlertCreatedEvent to the aggregate's state.
     * This method is called by Axon when an aggregate instance is being reconstituted from its events
     * or when an event is applied after a command handler.
     *
     * @param event The event to apply.
     */
    @EventSourcingHandler
    protected void on(AlertCreatedEvent event) {
        log.debug("Applying AlertCreatedEvent: {}", event);
        this.alertId = event.getAlertId();
        this.severity = event.getSeverity();
        this.description = event.getDescription();
        this.source = event.getSource();
        this.details = event.getDetails();
        this.status = event.getInitialStatus();
        this.createdAt = event.getCreatedAt();
        this.eventTimestamp = event.getEventTimestamp();
        this.updatedAt = event.getCreatedAt(); // Initially, updatedAt is same as createdAt
        this.initiatedBy = event.getInitiatedBy();
        this.notes = new ArrayList<>(); // Initialize notes list
        log.trace("State after AlertCreatedEvent for {}: {}", this.alertId, this);
    }

    @EventSourcingHandler
    protected void on(AlertUpdatedEvent event) {
        log.debug("Applying AlertUpdatedEvent: {}", event);
        this.severity = event.getSeverity(); // Assumes event carries the new state
        this.description = event.getDescription();
        this.details = event.getDetails();
        this.updatedAt = event.getUpdatedAt();
        this.updatedBy = event.getUpdatedBy();
        log.trace("State after AlertUpdatedEvent for {}: {}", this.alertId, this);
    }

    @EventSourcingHandler
    protected void on(AlertAcknowledgedEvent event) {
        log.debug("Applying AlertAcknowledgedEvent: {}", event);
        this.status = event.getNewStatus();
        this.acknowledgedAt = event.getAcknowledgedAt();
        this.acknowledgedBy = event.getAcknowledgedBy();
        this.acknowledgementNotes = event.getNotes();
        this.updatedAt = event.getAcknowledgedAt(); // Acknowledge updates the 'updatedAt' timestamp
        log.trace("State after AlertAcknowledgedEvent for {}: {}", this.alertId, this);
    }

    @EventSourcingHandler
    protected void on(AlertResolvedEvent event) {
        log.debug("Applying AlertResolvedEvent: {}", event);
        this.status = event.getNewStatus();
        this.resolvedAt = event.getResolvedAt();
        this.resolvedBy = event.getResolvedBy();
        this.resolutionDetails = event.getResolutionDetails();
        this.updatedAt = event.getResolvedAt();
        log.trace("State after AlertResolvedEvent for {}: {}", this.alertId, this);
    }

    @EventSourcingHandler
    protected void on(AlertClosedEvent event) {
        log.debug("Applying AlertClosedEvent: {}", event);
        this.status = event.getNewStatus();
        this.closedAt = event.getClosedAt();
        this.closedBy = event.getClosedBy();
        this.closingReason = event.getReason();
        this.updatedAt = event.getClosedAt();
        log.trace("State after AlertClosedEvent for {}: {}", this.alertId, this);
    }

    @EventSourcingHandler
    protected void on(NoteAddedToAlertEvent event) {
        log.debug("Applying NoteAddedToAlertEvent: {}", event);
        if (this.notes == null) {
            this.notes = new ArrayList<>();
        }
        this.notes.add(event.getNote());
        this.updatedAt = event.getNote().getTimestamp(); // Adding a note updates the aggregate
        log.trace("State after NoteAddedToAlertEvent for {}: {}", this.alertId, this);
    }

    @EventSourcingHandler
    protected void on(AlertAssignedEvent event) {
        log.debug("Applying AlertAssignedEvent: {}", event);
        this.assignee = event.getAssignee();
        this.assignedAt = event.getAssignedAt();
        this.assignedBy = event.getAssignedBy();
        this.updatedAt = event.getAssignedAt();
        log.trace("State after AlertAssignedEvent for {}: {}", this.alertId, this);
    }

    @EventSourcingHandler
    protected void on(AlertDeletedEvent event) {
        log.debug("Applying AlertDeletedEvent: {}", event);
        this.status = event.getNewStatus(); // Should be DELETED
        this.deletedAt = event.getDeletedAt();
        this.deletedBy = event.getDeletedBy();
        this.deletionReason = event.getReason();
        this.updatedAt = event.getDeletedAt(); // Deletion updates the 'updatedAt' timestamp
        // AggregateLifecycle.markDeleted(); // If you want Axon to treat this as a hard delete for future command routing.
        // For logical delete, just updating status is often enough.
        log.trace("State after AlertDeletedEvent for {}: {}", this.alertId, this);
    }

    //endregion

    // toString for debugging (Lombok @Getter and manual fields are fine, or @ToString)
    @Override
    public String toString() {
        return "AlertAggregate{" +
                "alertId=" + alertId +
                ", severity=" + severity +
                ", description='" + description + '\'' +
                ", source='" + source + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
