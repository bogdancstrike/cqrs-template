package template.cqrs.domain.alert.value_objects;

public enum AlertStatus {
    ACTIVE,
    ACKNOWLEDGED,
    RESOLVED,
    CLOSED,
    DELETED // Logical deletion or archival status
}
