package template.cqrs.application.query.alert;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FindAllAlertsQuery {
    private int pageNumber; // 0-indexed
    private int pageSize;
    // Optional: Add sortField and sortDirection (e.g., String sortField, String sortDirection "ASC"/"DESC")
    // For simplicity, sorting might be handled by default or specific query methods.
}
