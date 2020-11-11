package ac.kr.korea.cdm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProjectFileDto {
    private int projectId;
    private int fileId;
    private String fileName;
    private String description;
    private String path;
    private Timestamp requestTime;
    private String pathOutput;

    public ProjectFileDto(int projectId, String fileName, String description, String path) {
        this.projectId = projectId;
        this.fileName = fileName;
        this.description = description;
        this.path = path;
    }
}

