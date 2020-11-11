package ac.kr.korea.cdm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProjectDto {
    private Integer projectId;
    private String memberId;
    private String description;

    public ProjectDto(String memberId, String description) {
        this.memberId = memberId;
        this.description = description;
    }
}
