package ac.kr.korea.cdm.mapper;

import ac.kr.korea.cdm.dto.CdmMemberDto;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CdmMemberMapper {
    public CdmMemberDto getOne(CdmMemberDto cdmMemberDto);

    public List<CdmMemberDto> getAll();

    public void insertOne(CdmMemberDto cdmMemberDto);
}
