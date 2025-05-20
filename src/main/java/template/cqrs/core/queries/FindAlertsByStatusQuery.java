package template.cqrs.core.queries;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import template.cqrs.core.value_objects.AlertStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FindAlertsByStatusQuery {
    private AlertStatus status;
    private int pageNumber;
    private int pageSize;
}
