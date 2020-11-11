package ac.kr.korea.cdm.controller;

import ac.kr.korea.cdm.constants.Constants;
import ac.kr.korea.cdm.dto.AjaxResponeDto;
import ac.kr.korea.cdm.dto.AuthenticationDto;
import ac.kr.korea.cdm.dto.MemberDto;
import ac.kr.korea.cdm.util.CryptoHelper;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Base64Encoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.security.PublicKey;
import java.util.Map;

@CrossOrigin("*")
@RestController
public class CdmAppController {
    private static final Logger logger = LoggerFactory.getLogger(CdmAppController.class);
    @Autowired private CryptoHelper cryptoHelper;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView home(ModelAndView mv, HttpServletRequest request) {
        logger.info("home-sign-in");
        HttpSession session = request.getSession();
        AuthenticationDto auth = (AuthenticationDto) session.getAttribute(Constants.CDM_WEB_USER);

        if (auth != null && !auth.getCname().trim().equals("")) {
            mv.setViewName(Constants.CDM_WEB_REDIRECT_INDEX);
        } else {
            MemberDto memberDto = new MemberDto();
            mv.setViewName("home");
            mv.getModel().put("memberDto", memberDto);
        }

        return mv;
    }

    @RequestMapping(value="/pki/get-cert", method=RequestMethod.GET)
    public AjaxResponeDto<String> getCertificate(HttpServletRequest request) throws Exception {
        // must return X.509 certificiate
        // but right now, just return the public key...
        PublicKey publicKey = this.cryptoHelper.getPublic("CDM-KeyPair/CDM-PublicKey", Constants.TYPE_PKI);
        return new AjaxResponeDto<>(Base64Utils.encodeToString(publicKey.getEncoded()));
    }

    @RequestMapping(value="/error", method=RequestMethod.GET)
    public ModelAndView error(ModelAndView mv) {
        mv.setViewName("error");
        return mv;
    }

    @RequestMapping(value = "/index", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView index(ModelAndView mv, HttpServletRequest request) {
        // need to be fixed! attacker could make fake the ticket
//        logger.info(request.getParameter("param"));
//        logger.info(request.getParameter("cname"));
//        HttpSession httpSession = request.getSession();
//        httpSession.setAttribute(Constants.CDM_WEB_USER, request.getParameter("cname"));
        return mv;
    }

    // remove follows
    @RequestMapping(value = "/blank", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView blankPage(ModelAndView mv) {
        mv.setViewName("blank");
        return mv;
    }

    @RequestMapping(value = "/gen-script", method = RequestMethod.GET)
    public ModelAndView genScript(ModelAndView mv) {
        mv.setViewName("genscript");
        return mv;
    }

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public ModelAndView test(ModelAndView mv) {
        mv.setViewName("test");
        return mv;
    }

    @RequestMapping(value = "/get-ticket", method = RequestMethod.POST)
    public String testTgs(@RequestBody Map<String, Object> json) {
        for (String key : json.keySet()) {
            logger.info(key + " : " + json.get(key));
        }

        return "hello";
    }

    @RequestMapping(value = "/do-analysis", method = RequestMethod.POST)
    public String testSs(@RequestBody Map<String, Object> json) {
        for (String key : json.keySet()) {
            logger.info(key + " : " + json.get(key));
        }

        return "hello";
    }
}
