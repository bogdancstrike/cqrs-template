package template.cqrs.coreapi.queries;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import template.cqrs.coreapi.common.AlertStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FindAlertsByStatusQuery {
    private AlertStatus status;
    private int pageNumber;
    private int pageSize;
}
