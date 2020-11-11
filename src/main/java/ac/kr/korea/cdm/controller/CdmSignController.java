package ac.kr.korea.cdm.controller;

import ac.kr.korea.cdm.constants.Constants;
import ac.kr.korea.cdm.dto.CdmMemberDto;
import ac.kr.korea.cdm.dto.CountryCodeDto;
import ac.kr.korea.cdm.dto.MemberDto;
import ac.kr.korea.cdm.service.CdmMemberService;
import ac.kr.korea.cdm.service.CountryCodeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
public class CdmSignController {

    private static final Logger logger = LoggerFactory.getLogger(CdmSignController.class);
    @Autowired
    private CountryCodeService countryCodeService;
    @Autowired
    private CdmMemberService cdmMemberService;

    @RequestMapping(value = "/sign/up/which", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView signUpWhich(ModelAndView mv) {
        logger.info("sign-up-which");
        mv.setViewName("signupwhich");
        return mv;
    }


    @RequestMapping(value = "/sign/up", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView signUp(ModelAndView mv, HttpServletRequest request) {
        logger.info("sign-up-" + request.getParameter(Constants.CDM_WEB_SIGN_UP_WHICH));

        List<CountryCodeDto> countryCodes = this.countryCodeService.getAll();

        mv.setViewName("signup");
        mv.getModel().put("countryCodes", countryCodes);
        mv.getModel().put(Constants.CDM_WEB_SIGN_UP_WHICH, request.getParameter(Constants.CDM_WEB_SIGN_UP_WHICH));
//		mv.addObject("getUrl", Constants.CDM_DOMAIN + Constants.CA_GET_CERT);
        return mv;
    }

    @RequestMapping(value = "/sign/up/process", method = {RequestMethod.POST})
    public String signUpProcess(HttpServletRequest request, @RequestBody Map<String, Object> json) throws JsonProcessingException {
        String memberId = (String) json.get("member_id");
        CdmMemberDto cdmMemberDto = new CdmMemberDto(memberId);

        try {
            this.cdmMemberService.insertOne(cdmMemberDto);
            //폴더생성!
            String path = "C:\\Cdmproject\\project"; //폴더 경로
            path = path + "\\" + cdmMemberDto.getMemberId();
            File folder = new File(path);

            // 해당 디렉토리가 없을경우 디렉토리를 생성합니다.
            if (!folder.exists()) {
                try {
                    folder.mkdirs(); //폴더 생성합니다.
                    logger.info("프로젝트개인폴더가 생성되었습니다.");
                } catch (Exception e) {
                    e.getStackTrace();
                }
            } else {
                logger.info("이미 프로젝트개인폴더가 생성되어 있습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cdmMemberDto.toString();
    }


    @RequestMapping(value = "/sign/out", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView signOut(ModelAndView mv, HttpServletRequest request) {
        logger.info("sign-out");
        if (request.isRequestedSessionIdValid()) {
            HttpSession httpSession = request.getSession();
            httpSession.removeAttribute(Constants.CDM_WEB_USER);
        }
        mv.setViewName(Constants.CDM_WEB_REDIRECT_ROOT);
        return mv;
    }
}
