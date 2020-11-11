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
public class CdmExportKeyController {
    private static final Logger logger = LoggerFactory.getLogger(CdmExportKeyController.class);

    @RequestMapping(value = "/export-key", method = RequestMethod.GET)
    public ModelAndView exportKey(ModelAndView mv, HttpServletRequest request) {
        mv.setViewName("exportkey");
        return mv;
    }
}
