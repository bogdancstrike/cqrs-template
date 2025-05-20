package template.cqrs.read_model.document;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import template.cqrs.core.value_objects.AlertDetails;
import template.cqrs.core.value_objects.AlertNoteDto;
import template.cqrs.core.value_objects.AlertSeverity;
import template.cqrs.core.value_objects.AlertStatus;

import java.time.Instant;
import java.util.List;

@Document(indexName = "alerts") // Defines the Elasticsearch index name
@Setting(shards = 1, replicas = 0) // Example settings, adjust for production
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertDocument {

    @Id // Marks this field as the document ID in Elasticsearch
    private String alertId; // Using String for ES ID, can be UUID.toString()

    @Field(type = FieldType.Keyword) // Good for exact matches and aggregations
    private AlertSeverity severity;

    @Field(type = FieldType.Text, analyzer = "standard") // For full-text search
    private String description;

    @Field(type = FieldType.Keyword)
    private String source;

    @Field(type = FieldType.Keyword)
    private AlertStatus status;

    // For complex objects like AlertDetails, Elasticsearch can store them as nested objects.
    // If you need to query specific fields within details, map them explicitly or use 'flattened' type.
    @Field(type = FieldType.Object) // Stores as a nested JSON object
    private AlertDetails details;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time) // ISO8601 format
    private Instant createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
    private Instant updatedAt;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
    private Instant eventTimestamp; // Timestamp from original source event

    @Field(type = FieldType.Keyword)
    private String initiatedBy;

    @Field(type = FieldType.Keyword)
    private String updatedBy;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
    private Instant acknowledgedAt;

    @Field(type = FieldType.Keyword)
    private String acknowledgedBy;

    @Field(type = FieldType.Text) // Notes for acknowledgement
    private String acknowledgementNotes;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
    private Instant resolvedAt;

    @Field(type = FieldType.Keyword)
    private String resolvedBy;

    @Field(type = FieldType.Text)
    private String resolutionDetails;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
    private Instant closedAt;

    @Field(type = FieldType.Keyword)
    private String closedBy;

    @Field(type = FieldType.Text)
    private String closingReason;

    @Field(type = FieldType.Keyword)
    private String assignee;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
    private Instant assignedAt;

    @Field(type = FieldType.Keyword)
    private String assignedBy;

    // For lists of objects, use FieldType.Nested if you need to query individual elements
    // of the list independently. Otherwise, FieldType.Object might suffice.
    @Field(type = FieldType.Nested) // If querying notes fields is needed
    private List<AlertNoteDto> notes;

    // Fields for logical deletion
    @Field(type = FieldType.Keyword)
    private String deletedBy;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
    private Instant deletedAt;

    @Field(type = FieldType.Text)
    private String deletionReason;
}
