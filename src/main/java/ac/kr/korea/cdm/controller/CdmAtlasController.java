package ac.kr.korea.cdm.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@CrossOrigin("*")
@RestController
public class CdmAtlasController {

    private static final Logger logger = LoggerFactory.getLogger(CdmAtlasController.class);

    @RequestMapping(value = "/atlas", method = RequestMethod.GET)
    public ModelAndView atlas(ModelAndView mv) {
        mv.setViewName("atlas");
        return mv;
    }
}
