package template.cqrs.core.value_objects;

public enum AlertStatus {
    ACTIVE,
    ACKNOWLEDGED,
    RESOLVED,
    CLOSED,
    DELETED // Logical deletion or archival status
}
