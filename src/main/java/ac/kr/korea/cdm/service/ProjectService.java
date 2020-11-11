package ac.kr.korea.cdm.service;

import ac.kr.korea.cdm.dto.ProjectDto;
import ac.kr.korea.cdm.mapper.ProjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {
    @Autowired
    private ProjectMapper projectMapper;

    public ProjectDto getOne(ProjectDto projectDto) {
        return this.projectMapper.getOne(projectDto);
    }

    public List<ProjectDto> getAll(ProjectDto projectDto) {
        return this.projectMapper.getAll(projectDto);
    }

    public void insertOne(ProjectDto projectDto) {
        this.projectMapper.insertOne(projectDto);
    }

    public ProjectDto selectOne(ProjectDto projectDto) {
        return this.projectMapper.selectOne(projectDto);
    }

    public void delete(ProjectDto projectDto) {
        this.projectMapper.delete(projectDto);
    }
}
