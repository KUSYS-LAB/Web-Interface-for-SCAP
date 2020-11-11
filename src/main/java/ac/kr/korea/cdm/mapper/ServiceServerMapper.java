package ac.kr.korea.cdm.mapper;

import ac.kr.korea.cdm.dto.ServiceServerDto;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceServerMapper {
    public List<ServiceServerDto> getAllServiceServers();
    public ServiceServerDto getOne(int id);
}
