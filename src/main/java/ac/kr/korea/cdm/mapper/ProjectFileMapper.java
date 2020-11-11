package ac.kr.korea.cdm.mapper;

import ac.kr.korea.cdm.dto.ProjectDto;
import ac.kr.korea.cdm.dto.ProjectFileDto;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectFileMapper {
    public ProjectFileDto getOne(ProjectFileDto projectFileDto);

    public ProjectFileDto getOneById(ProjectFileDto projectFileDto);

    public List<ProjectFileDto> getAll(ProjectFileDto projectFileDto);

    public void insertOne(ProjectFileDto projectFileDto);

    public ProjectFileDto selectOne(ProjectFileDto projectFileDto);

    public void updateRequestTime(ProjectFileDto projectFileDto);

    public void updatePathOutput(ProjectFileDto projectFileDto);

    public void delete(ProjectFileDto projectFileDto);
}
