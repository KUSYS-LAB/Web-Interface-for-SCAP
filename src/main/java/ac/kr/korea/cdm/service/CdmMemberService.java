package ac.kr.korea.cdm.service;

import ac.kr.korea.cdm.dto.CdmMemberDto;
import ac.kr.korea.cdm.mapper.CdmMemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CdmMemberService {

    @Autowired
    private CdmMemberMapper cdmMemberMapper;

    public CdmMemberDto getOne(CdmMemberDto cdmMemberDto) {
        return this.cdmMemberMapper.getOne(cdmMemberDto);
    }

    public List<CdmMemberDto> getAll() {
        return this.cdmMemberMapper.getAll();
    }

    public void insertOne(CdmMemberDto cdmMemberDto) {
        this.cdmMemberMapper.insertOne(cdmMemberDto);
    }
}
