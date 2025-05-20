package template.cqrs.infrastructure.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddNoteRequestDto {
    @NotBlank(message = "Note text cannot be blank")
    private String text;
    @NotBlank(message = "Author cannot be blank")
    private String author;
}