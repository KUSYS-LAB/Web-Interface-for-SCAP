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
public class ServiceServerRequestDto {
    private int projectId;
    private int fileId;
    private Timestamp requestTime;

}