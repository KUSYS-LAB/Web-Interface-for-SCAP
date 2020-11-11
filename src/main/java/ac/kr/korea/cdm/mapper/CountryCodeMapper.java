package ac.kr.korea.cdm.mapper;


import ac.kr.korea.cdm.dto.CountryCodeDto;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CountryCodeMapper {
    public List<CountryCodeDto> getAll();
}
