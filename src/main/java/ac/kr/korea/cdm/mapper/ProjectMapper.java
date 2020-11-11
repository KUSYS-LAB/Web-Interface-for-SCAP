package ac.kr.korea.cdm.mapper;

import ac.kr.korea.cdm.dto.ProjectDto;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectMapper {
    public ProjectDto getOne(ProjectDto projectDto);

    public List<ProjectDto> getAll(ProjectDto projectDto);

    public void insertOne(ProjectDto projectDto);

    public ProjectDto selectOne(ProjectDto projectDto);

    public void delete(ProjectDto projectDto);

}
