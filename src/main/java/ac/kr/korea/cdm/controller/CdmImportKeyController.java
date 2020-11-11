package ac.kr.korea.cdm.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@CrossOrigin("*")
@RestController
public class CdmImportKeyController {

    private static final Logger logger = LoggerFactory.getLogger(CdmImportKeyController.class);

    @RequestMapping(value = "/import-key", method = RequestMethod.GET)
    public ModelAndView importKey(ModelAndView mv, HttpServletRequest request) {
        mv.setViewName("importkey");
        return mv;
    }
}
