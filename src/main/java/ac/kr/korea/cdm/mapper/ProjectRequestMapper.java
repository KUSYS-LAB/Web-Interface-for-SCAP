package ac.kr.korea.cdm.mapper;

import ac.kr.korea.cdm.dto.ProjectRequestDto;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRequestMapper {
    public void insertOne(ProjectRequestDto projectRequestDto);

    public List<ProjectRequestDto> getAll(ProjectRequestDto projectRequestDto);

    public ProjectRequestDto getMax(ProjectRequestDto projectRequestDto);

    public void delete(ProjectRequestDto projectRequestDto);

}
