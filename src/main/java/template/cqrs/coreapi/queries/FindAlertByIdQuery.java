package template.cqrs.coreapi.queries;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data // Lombok's @Data for getters, setters, equals, hashCode, toString
@NoArgsConstructor
@AllArgsConstructor
public class FindAlertByIdQuery {
    private UUID alertId;
}
