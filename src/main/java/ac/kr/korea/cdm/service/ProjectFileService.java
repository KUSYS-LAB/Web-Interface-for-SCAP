package ac.kr.korea.cdm.service;

import ac.kr.korea.cdm.dto.ProjectDto;
import ac.kr.korea.cdm.dto.ProjectFileDto;
import ac.kr.korea.cdm.mapper.ProjectFileMapper;
import ac.kr.korea.cdm.mapper.ProjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectFileService {

    @Autowired
    private ProjectFileMapper projectFileMapper;

    public ProjectFileDto getOne(ProjectFileDto projectFileDto) {
        return this.projectFileMapper.getOne(projectFileDto);
    }

    public ProjectFileDto getOneById(ProjectFileDto projectFileDto) {
        return this.projectFileMapper.getOneById(projectFileDto);
    }

    public List<ProjectFileDto> getAll(ProjectFileDto projectFileDto) {
        return this.projectFileMapper.getAll(projectFileDto);
    }

    public void insertOne(ProjectFileDto projectFileDto) {
        this.projectFileMapper.insertOne(projectFileDto);
    }

    public void updateRequestTime(ProjectFileDto projectFileDto) {
        this.projectFileMapper.updateRequestTime(projectFileDto);
    }

    public void updatePathOutput(ProjectFileDto projectFileDto) {
        this.projectFileMapper.updatePathOutput(projectFileDto);
    }

    public ProjectFileDto selectOne(ProjectFileDto projectFileDto) {
        return this.projectFileMapper.selectOne(projectFileDto);
    }

    public void delete(ProjectFileDto projectFileDto) {
        this.projectFileMapper.delete(projectFileDto);
    }
}
