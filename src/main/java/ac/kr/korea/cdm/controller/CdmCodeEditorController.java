package ac.kr.korea.cdm.controller;

import ac.kr.korea.cdm.constants.Constants;
import ac.kr.korea.cdm.dto.AjaxResponeDto;
import ac.kr.korea.cdm.dto.AuthenticationDto;
import ac.kr.korea.cdm.dto.ProjectFileDto;
import ac.kr.korea.cdm.service.ProjectFileService;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@CrossOrigin("*")
@RestController
public class CdmCodeEditorController {
    private static final Logger logger = LoggerFactory.getLogger(CdmCodeEditorController.class);
    @Autowired
    private ProjectFileService projectFileService;

    @RequestMapping(value = "/code-editor/filedetail", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView filedetail(ModelAndView mv, HttpServletRequest request) {
        Integer file_id = Integer.parseInt(request.getParameter("file_id"));
        ProjectFileDto projectFileDto = new ProjectFileDto();
        projectFileDto.setFileId(file_id);
        projectFileDto = this.projectFileService.selectOne(projectFileDto);
        mv.addObject("fileid", file_id);
        mv.addObject("filename", projectFileDto.getFileName());
        mv.setViewName("filedetail");
        return mv;
    }

    @RequestMapping(value = "/code-editor/zipfile", method = {RequestMethod.POST, RequestMethod.GET})
    public Resource zipfile(@RequestBody String fileid) throws IOException {
        Integer file_id = Integer.parseInt(fileid);
        ProjectFileDto projectFileDto = new ProjectFileDto();
        projectFileDto.setFileId(file_id);
        projectFileDto = this.projectFileService.selectOne(projectFileDto);
        logger.info(projectFileDto.toString());
        String filepath = projectFileDto.getPath();
        File zipfile = new File(filepath);
        logger.info(zipfile.toString());
        InputStream is = FileUtils.openInputStream(zipfile);
        return new InputStreamResource(is);
    }

    @RequestMapping(value = "/code-editor/modifyzip", method = {RequestMethod.POST, RequestMethod.GET})
    public AjaxResponeDto<Boolean> modifyzip(HttpServletRequest req) throws Exception {

        MultipartHttpServletRequest request = (MultipartHttpServletRequest) req;
        Integer file_id = Integer.parseInt(request.getParameter("fileid"));
        logger.info(request.getParameter("fileid"));
        ProjectFileDto projectFileDto = new ProjectFileDto();
        projectFileDto.setFileId(file_id);
        projectFileDto = this.projectFileService.selectOne(projectFileDto);
        String filepath = projectFileDto.getPath();
        String pathName = filepath.substring(0, filepath.lastIndexOf("/"));
        String saveName = projectFileDto.getFileName();
        MultipartFile file = request.getFile("file");
        File saveFile = new File(pathName, saveName);

        try {
            file.transferTo(saveFile);
        } catch (IOException e) {
            e.printStackTrace();
            return new AjaxResponeDto<>(false);
        }
        return new AjaxResponeDto<>(true);

    }


    @RequestMapping(value = "/code-editor/fileupload", method = {RequestMethod.POST, RequestMethod.GET})
    public ModelAndView fileupload(ModelAndView mv, HttpServletRequest request, @RequestPart("filename") MultipartFile file) throws IOException {
        System.out.println("파일업로드시작!");
        HttpSession session = request.getSession();
        String fileName = file.getOriginalFilename();
        String username = ((AuthenticationDto)session.getAttribute(Constants.CDM_WEB_USER)).getCname();
        String project_id = request.getParameter("project_id");
        String path = "C:/Cdmproject/project/" + username + "/" + project_id;
        File saveFile = new File(path, fileName);
        path = path + "/" + fileName;
        try {
            file.transferTo(saveFile);
            ProjectFileDto projectFileDto = new ProjectFileDto(
                    Integer.parseInt(project_id),
                    fileName,
                    request.getParameter("description"),
                    path);
            logger.info(projectFileDto.toString());
            this.projectFileService.insertOne(projectFileDto);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mv.setViewName("redirect:/project/projectdetail?project_id=" + project_id);
        return mv;
    }

    @RequestMapping(value = "/code-editor", method = RequestMethod.GET)
    public ModelAndView codeeditor(ModelAndView mv) {
        mv.setViewName("codeeditor");
        return mv;
    }
}
