package template.cqrs.coreapi.common;

public enum AlertStatus {
    ACTIVE,
    ACKNOWLEDGED,
    RESOLVED,
    CLOSED,
    DELETED // Logical deletion or archival status
}
