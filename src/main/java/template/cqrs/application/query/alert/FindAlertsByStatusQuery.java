package template.cqrs.application.query.alert;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import template.cqrs.domain.model.alert.value_objects.AlertStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FindAlertsByStatusQuery {
    private AlertStatus status;
    private int pageNumber;
    private int pageSize;
}
