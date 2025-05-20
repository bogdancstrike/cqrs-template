package template.cqrs.coreapi.queries;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FindAlertsByKeywordQuery {
    private String keyword; // Keyword to search in description or other relevant fields
    private int pageNumber;
    private int pageSize;
}