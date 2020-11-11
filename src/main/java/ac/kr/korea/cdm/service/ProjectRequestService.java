package ac.kr.korea.cdm.service;

import ac.kr.korea.cdm.dto.ProjectRequestDto;
import ac.kr.korea.cdm.mapper.ProjectRequestMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectRequestService {

    @Autowired private ProjectRequestMapper projectRequestMapper;
    private static final Logger logger = LoggerFactory.getLogger(ProjectRequestService.class);

    public void insertOne(ProjectRequestDto projectRequestDto){
        logger.info("ProjectRequestMapper-insertOne-" + projectRequestDto.toString());
        this.projectRequestMapper.insertOne(projectRequestDto);
    }

    public List<ProjectRequestDto> getAll(ProjectRequestDto projectRequestDto){
        return this.projectRequestMapper.getAll(projectRequestDto);
    }

    public ProjectRequestDto getMax(ProjectRequestDto projectRequestDto){
        return this.projectRequestMapper.getMax(projectRequestDto);
    }

    public void delete(ProjectRequestDto projectRequestDto){
        this.projectRequestMapper.delete(projectRequestDto);
    }
}
