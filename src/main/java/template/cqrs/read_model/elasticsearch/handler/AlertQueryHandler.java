package template.cqrs.read_model.elasticsearch.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import template.cqrs.application.query.alert.*;
import template.cqrs.domain.model.alert.dto.AlertDto;
import template.cqrs.domain.model.alert.dto.PagedAlertResponse;
import template.cqrs.read_model.elasticsearch.document.AlertDocument;
import template.cqrs.read_model.elasticsearch.repository.AlertDocumentRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertQueryHandler {

    private final AlertDocumentRepository repository;

    @QueryHandler
    public Optional<AlertDto> handle(FindAlertByIdQuery query) {
        log.debug("Handling FindAlertByIdQuery for id: {}", query.getAlertId());
        return repository.findById(query.getAlertId().toString())
                .map(this::convertToDto);
    }

    @QueryHandler
    public PagedAlertResponse handle(FindAllAlertsQuery query) {
        log.debug("Handling FindAllAlertsQuery: page={}, size={}", query.getPageNumber(), query.getPageSize());
        Pageable pageable = PageRequest.of(query.getPageNumber(), query.getPageSize(), Sort.by("createdAt").descending());
        Page<AlertDocument> page = repository.findAll(pageable);
        return convertToPagedResponse(page);
    }

    @QueryHandler
    public PagedAlertResponse handle(FindAlertsByKeywordQuery query) {
        log.debug("Handling FindAlertsByKeywordQuery: keyword={}, page={}, size={}",
                query.getKeyword(), query.getPageNumber(), query.getPageSize());
        Pageable pageable = PageRequest.of(query.getPageNumber(), query.getPageSize(), Sort.by("createdAt").descending());
        // Using the custom query for broader search
        Page<AlertDocument> page = repository.findByKeywordInDescriptionOrSource(query.getKeyword(), pageable);
        return convertToPagedResponse(page);
    }

    @QueryHandler
    public PagedAlertResponse handle(FindAlertsByTimestampRangeQuery query) {
        log.debug("Handling FindAlertsByTimestampRangeQuery: start={}, end={}, page={}, size={}",
                query.getStartTime(), query.getEndTime(), query.getPageNumber(), query.getPageSize());
        Pageable pageable = PageRequest.of(query.getPageNumber(), query.getPageSize(), Sort.by("createdAt").descending());
        // Assuming query is for 'createdAt'. Change to 'eventTimestamp' if needed.
        Page<AlertDocument> page = repository.findByCreatedAtBetween(query.getStartTime(), query.getEndTime(), pageable);
        return convertToPagedResponse(page);
    }

    @QueryHandler
    public PagedAlertResponse handle(FindAlertsByStatusQuery query) {
        log.debug("Handling FindAlertsByStatusQuery: status={}, page={}, size={}",
                query.getStatus(), query.getPageNumber(), query.getPageSize());
        Pageable pageable = PageRequest.of(query.getPageNumber(), query.getPageSize(), Sort.by("createdAt").descending());
        Page<AlertDocument> page = repository.findByStatus(query.getStatus(), pageable);
        return convertToPagedResponse(page);
    }

    // --- Helper Methods ---

    private AlertDto convertToDto(AlertDocument doc) {
        if (doc == null) {
            return null;
        }
        return AlertDto.builder()
                .alertId(UUID.fromString(doc.getAlertId()))
                .severity(doc.getSeverity())
                .description(doc.getDescription())
                .source(doc.getSource())
                .status(doc.getStatus())
                .details(doc.getDetails())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .eventTimestamp(doc.getEventTimestamp())
                .initiatedBy(doc.getInitiatedBy())
                .updatedBy(doc.getUpdatedBy())
                .acknowledgedAt(doc.getAcknowledgedAt())
                .acknowledgedBy(doc.getAcknowledgedBy())
                .acknowledgementNotes(doc.getAcknowledgementNotes())
                .resolvedAt(doc.getResolvedAt())
                .resolvedBy(doc.getResolvedBy())
                .resolutionDetails(doc.getResolutionDetails())
                .closedAt(doc.getClosedAt())
                .closedBy(doc.getClosedBy())
                .closingReason(doc.getClosingReason())
                .assignee(doc.getAssignee())
                .assignedAt(doc.getAssignedAt())
                .assignedBy(doc.getAssignedBy())
                .notes(doc.getNotes() != null ? doc.getNotes() : Collections.emptyList())
                .build();
    }

    private PagedAlertResponse convertToPagedResponse(Page<AlertDocument> page) {
        List<AlertDto> dtoList = page.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return PagedAlertResponse.builder()
                .alerts(dtoList)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
