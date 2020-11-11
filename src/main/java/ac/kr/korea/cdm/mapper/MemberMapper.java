package ac.kr.korea.cdm.mapper;


import ac.kr.korea.cdm.dto.MemberDto;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberMapper {
    public MemberDto getOne(MemberDto memberDto);

    public void insertOne(MemberDto memberDto);
}
