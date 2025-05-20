package template.cqrs.read_model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import template.cqrs.core.value_objects.AlertSeverity;
import template.cqrs.core.value_objects.AlertStatus;
import template.cqrs.read_model.document.AlertDocument;

import java.time.Instant;

@Repository
public interface AlertDocumentRepository extends ElasticsearchRepository<AlertDocument, String> { // ID is String

    // Find by status with pagination
    Page<AlertDocument> findByStatus(AlertStatus status, Pageable pageable);

    // Find by keyword in description (uses Elasticsearch query capabilities)
    // This is a simple example; more complex queries might use @Query or Criteria API
    Page<AlertDocument> findByDescriptionContainingIgnoreCase(String keyword, Pageable pageable);

    // Find by timestamp range (createdAt or eventTimestamp)
    Page<AlertDocument> findByCreatedAtBetween(Instant startTime, Instant endTime, Pageable pageable);

    Page<AlertDocument> findByEventTimestampBetween(Instant startTime, Instant endTime, Pageable pageable);


    // Example of a custom query using @Query annotation for more complex searches
    // This query searches for the keyword in description OR source fields.
    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"description\", \"source\"], \"type\": \"best_fields\"}}")
    Page<AlertDocument> findByKeywordInDescriptionOrSource(String keyword, Pageable pageable);

    // Find by severity
    Page<AlertDocument> findBySeverity(AlertSeverity severity, Pageable pageable);

    // Find by assignee
    Page<AlertDocument> findByAssignee(String assignee, Pageable pageable);

    // Find by source
    Page<AlertDocument> findBySource(String source, Pageable pageable);

}

