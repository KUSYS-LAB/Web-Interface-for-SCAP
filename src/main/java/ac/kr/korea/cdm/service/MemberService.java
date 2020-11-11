package ac.kr.korea.cdm.service;

import ac.kr.korea.cdm.dto.MemberDto;
import ac.kr.korea.cdm.mapper.MemberMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MemberService {
    @Autowired
    private MemberMapper memberMapper;

    private Logger logger = LoggerFactory.getLogger(MemberService.class);

    public boolean isMember(MemberDto memberDto) {
        if (this.memberMapper.getOne(memberDto) != null) return true;
        return false;
    }

    public MemberDto getOne(MemberDto memberDto) {
        return this.memberMapper.getOne(memberDto);
    }

    public void insertOne(MemberDto memberDto) {
        if (memberDto.isFilled()) this.memberMapper.insertOne(memberDto);
        else logger.info("memberDto is not filled");
    }
}
