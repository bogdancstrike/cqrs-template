package template.cqrs.application.query.alert;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FindAlertsByTimestampRangeQuery {
    private Instant startTime; // Inclusive
    private Instant endTime;   // Inclusive
    private int pageNumber;
    private int pageSize;
}
