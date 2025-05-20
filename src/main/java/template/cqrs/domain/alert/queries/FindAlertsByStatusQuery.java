package template.cqrs.domain.alert.queries;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import template.cqrs.domain.alert.value_objects.AlertStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FindAlertsByStatusQuery {
    private AlertStatus status;
    private int pageNumber;
    private int pageSize;
}
