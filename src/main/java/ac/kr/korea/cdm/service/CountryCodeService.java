package ac.kr.korea.cdm.service;

import ac.kr.korea.cdm.dto.CountryCodeDto;
import ac.kr.korea.cdm.mapper.CountryCodeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CountryCodeService {

    @Autowired
    private CountryCodeMapper countryCodeMapper;

    private Logger logger = LoggerFactory.getLogger(CountryCodeService.class);

    public List<CountryCodeDto> getAll() {
        return this.countryCodeMapper.getAll();
    }
}
