package ac.kr.korea.cdm.controller;

import ac.kr.korea.cdm.constants.Constants;
import ac.kr.korea.cdm.dto.*;
import ac.kr.korea.cdm.mapper.ServiceServerMapper;
import ac.kr.korea.cdm.service.ProjectFileService;
import ac.kr.korea.cdm.service.ProjectService;
import ac.kr.korea.cdm.service.RelayService;
import ac.kr.korea.cdm.service.VerificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
public class CdmProjectController {
    private static final Logger logger = LoggerFactory.getLogger(CdmProjectController.class);

    @Autowired
    private RelayService relayService;
    @Autowired
    private ProjectService projectService;
//    @Autowired
//    private ProjectRequestService projectRequestService;
    @Autowired
    private ProjectFileService projectFileService;
    @Autowired
    private VerificationService verificationService;
    @Autowired
    private ServiceServerMapper ssMapper;

    @RequestMapping(value = "/project", method = RequestMethod.GET)
    public ModelAndView myproject(ModelAndView mv, HttpServletRequest request) {
        HttpSession session = request.getSession();
        String memberId = ((AuthenticationDto)session.getAttribute(Constants.CDM_WEB_USER)).getCname();
        ProjectDto projectDto = new ProjectDto();
        projectDto.setMemberId(memberId);
        List<ProjectDto> projectDtoList = this.projectService.getAll(projectDto);
        mv.addObject("projectDtoList", projectDtoList);
        mv.setViewName("myproject");
        return mv;
    }

    @RequestMapping(value = "/project/mkproject", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView mkproject(ModelAndView mv, HttpServletRequest request) {
        HttpSession session = request.getSession();
        String memberId = ((AuthenticationDto)session.getAttribute(Constants.CDM_WEB_USER)).getCname();
        ProjectDto projectDto = new ProjectDto(memberId, request.getParameter("description"));
        this.projectService.insertOne(projectDto);
        try {
            //폴더생성!
            String path = "C:\\Cdmproject\\project"; //폴더 경로
            String projectId = this.projectService.getOne(projectDto).getProjectId().toString();
            path = path + "\\" + memberId + "\\" + projectId;
            File folder = new File(path);
            // 해당 디렉토리가 없을경우 디렉토리를 생성합니다.
            if (!folder.exists()) {
                try {
                    folder.mkdirs(); //폴더 생성합니다.
//                    System.out.println("프로젝트개인폴더가 생성되었습니다.");
                    logger.info("member's project folder is created");
                } catch (Exception e) {
                    e.getStackTrace();
                }
            } else {
//                System.out.println("이미 프로젝트개인폴더가 생성되어 있습니다.");
                logger.info("member's project folder has already been created");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mv.setViewName("redirect:/project");
        return mv;
    }

    @RequestMapping(value = "/project/projectdetail", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView projectdetail(ModelAndView mv, HttpServletRequest request) throws JSONException {
        ProjectFileDto projectFileDto = new ProjectFileDto();
        String projectIdStr = request.getParameter("project_id");
        Integer projectId = Integer.parseInt(projectIdStr);
//        String jsonResult = null;
        projectFileDto.setProjectId(projectId);
        List<ProjectFileDto> projectFileDtoList = this.projectFileService.getAll(projectFileDto);



//        ProjectRequestDto projectRequestDto = new ProjectRequestDto();
//        Iterator iterator = projectFileDtoList.iterator();
//        ArrayList<ServiceServerRequestDto> serviceserverRequestList= new ArrayList<ServiceServerRequestDto>();

//        while (iterator.hasNext()){
//            projectFileDto = (ProjectFileDto)iterator.next();
//            projectRequestDto.setProjectId(projectFileDto.getProjectId());
//            projectRequestDto.setFileId(projectFileDto.getFileId());
//            projectRequestDto = this.projectRequestService.getMax(projectRequestDto);
//
//            if(projectRequestDto == null){
//                ServiceServerRequestDto serviceServerRequest = new ServiceServerRequestDto(projectFileDto.getProjectId(), projectFileDto.getFileId(), Timestamp.valueOf("1999-11-11 11:11:11.111"));
//                serviceserverRequestList.add(serviceServerRequest);
//            }
//            else {
//                ServiceServerRequestDto serviceServerRequest = new ServiceServerRequestDto(projectFileDto.getProjectId(), projectFileDto.getFileId(),projectRequestDto.getRequestTime());
//                serviceserverRequestList.add(serviceServerRequest);
//                logger.info(projectRequestDto.toString());
//            }
//        }

//        JSONArray jsonArray = new JSONArray();
//        iterator = serviceserverRequestList.iterator();
//        while (iterator.hasNext()){
//            JSONObject data = new JSONObject();
//            ServiceServerRequestDto requestdata = (ServiceServerRequestDto)iterator.next();
//            data.put("project_id",requestdata.getProjectId());
//            data.put("file_id",requestdata.getFileId());
//            data.put("request_time",requestdata.getRequestTime());
//            jsonArray.put(data);
//        }

//        try {
//            HttpClient httpclient = HttpClientBuilder.create().build();
//            // 파일을 수신할 서버주소
//            HttpPost httpPost = new HttpPost("http://163.152.126.90:8080/ss/statusrequest");
//            MultipartEntity entry = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
//            entry.addPart("json",new StringBody(String.valueOf(jsonArray), ContentType.APPLICATION_JSON));
//            httpPost.setEntity(entry);
//            HttpResponse response = httpclient.execute(httpPost);
////            logger.info("getcontent 내용: "+response.getStatusLine().); //이걸로 응답가능?
//            BufferedReader reader = new BufferedReader(new InputStreamReader(
//                    response.getEntity().getContent()));
//            String inputLine;
//            StringBuffer response_content = new StringBuffer();
//
//            while ((inputLine = reader.readLine()) != null) {
//                response_content.append(inputLine);
//            }
//
//            reader.close();
//            logger.info(response_content.toString());
//            jsonResult = response_content.toString();
//            logger.info("json result : "+jsonResult);
//            int status = response.getStatusLine().getStatusCode();
//            logger.info("status : "+status);
//            if(status == HttpStatus.SC_OK){
//                logger.info("statusrequest complete!");
//            }
//        } catch (Throwable t) {
//            t.printStackTrace();
//        }

//        ArrayList<String> statuslist = new ArrayList<String>();
//        JSONArray jsonArray_result = new JSONArray(jsonResult);
//        for(int i = 0; i < jsonArray_result.length();i++){
//            JSONObject jsonObject = jsonArray_result.getJSONObject(i);
//            logger.info("status : " + jsonObject.getString("status"));
//            if(jsonObject.getString("status").equals("run")||jsonObject.getString("status").equals("wait")){
//                statuslist.add("disabled");
//            }
//            else {
//                statuslist.add(null);
//            }
//            logger.info("btn 상태 : "+statuslist.get(i));
//        }

//        mv.addObject("statuslist",statuslist);
        mv.addObject("projectFileDtoList", projectFileDtoList);
        mv.addObject("project_id", projectIdStr);
        mv.setViewName("projectdetail");
        return mv;
    }

    @RequestMapping(value="/project/analysis-status", method=RequestMethod.GET)
    public ModelAndView analysisStatus (HttpServletRequest request, ModelAndView mv) {
        int projectId = Integer.parseInt(request.getParameter(Constants.CDM_PROJECT_ID));
        int fileId = Integer.parseInt(request.getParameter(Constants.CDM_FILE_ID));

        ProjectFileDto projectFileDto = new ProjectFileDto();
        projectFileDto.setProjectId(projectId);
        projectFileDto.setFileId(fileId);

        List<ServiceServerDto> ssList = this.ssMapper.getAllServiceServers();

        mv.addObject("projectFileDto", projectFileDto);
        mv.addObject("ssList", ssList);
        mv.setViewName("analysisstatus");
        return mv;
    }

    @RequestMapping(value="/project/check-progress", method=RequestMethod.GET)
    public Map<String, Object> checkProgress(HttpServletRequest request) {
        int projectId = Integer.parseInt(request.getParameter(Constants.CDM_PROJECT_ID));
        int file_id = Integer.parseInt(request.getParameter(Constants.CDM_FILE_ID));
        int serviceServerId = Integer.parseInt(request.getParameter("ss_id"));
        AuthenticationDto auth = (AuthenticationDto) request.getSession().getAttribute(Constants.CDM_WEB_USER);

        ProjectFileDto projectFileDto = new ProjectFileDto();
        projectFileDto.setProjectId(projectId);
        projectFileDto.setFileId(file_id);
        projectFileDto = this.projectFileService.getOneById(projectFileDto);
        String pathOutput = projectFileDto.getPathOutput();

        if (pathOutput == null || pathOutput.trim().equals("")) {
            try {
                return this.relayService.checkProgress(auth, projectFileDto, serviceServerId);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                Map<String, Object> json = new HashMap<>();
                json.put("status", "error");
                return json;
            }
        } else {

            // check if ss approved to read


            Map<String, Object> json = new HashMap<>();
            json.put(Constants.CDM_STATUS, "pending");
            json.put("analysisProjectId", projectFileDto.getProjectId());
            json.put("analysisFileId", projectFileDto.getFileId());
            return json;
        }
    }

    @RequestMapping(value="/project/request-approval", method=RequestMethod.GET)
    public boolean getAnalysisResult(HttpServletRequest request) {
        int projectId = Integer.parseInt(request.getParameter(Constants.CDM_PROJECT_ID));
        int fileId = Integer.parseInt(request.getParameter(Constants.CDM_FILE_ID));
        int serviceServerId = Integer.parseInt(request.getParameter(Constants.CDM_SS_ID));
        AuthenticationDto auth = (AuthenticationDto) request.getSession().getAttribute(Constants.CDM_WEB_USER);

        ProjectFileDto projectFileDto = new ProjectFileDto();
        projectFileDto.setProjectId(projectId);
        projectFileDto.setFileId(fileId);
        projectFileDto = this.projectFileService.getOneById(projectFileDto);

        String pathOutput = projectFileDto.getPathOutput();
        if (pathOutput == null || pathOutput.trim().equals("")) {
            try {
                this.relayService.getAnalysisResult(auth, projectFileDto, serviceServerId);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    @RequestMapping(value="/project/get-ss-address", method = RequestMethod.GET)
    public Map<String, Object> getServiceServerAddress(HttpServletRequest request) {
        int serviceServerId = Integer.parseInt(request.getParameter(Constants.CDM_SS_ID));

        ServiceServerDto serviceServerDto = this.ssMapper.getOne(serviceServerId);

        Map<String, Object> json = new HashMap<>();
        json.put("body", serviceServerDto);
        return json;
    }

    @RequestMapping(value="/project/get-ear", method=RequestMethod.GET)
    public Map<String, Object> getEncryptedAR(HttpServletRequest request) throws IOException {
        int projectId = Integer.parseInt(request.getParameter(Constants.CDM_PROJECT_ID));
        int fileId = Integer.parseInt(request.getParameter(Constants.CDM_FILE_ID));
        Map<String ,Object> json = new HashMap<>();

        ProjectFileDto projectFileDto = new ProjectFileDto();
        projectFileDto.setProjectId(projectId);
        projectFileDto.setFileId(fileId);

        projectFileDto = this.projectFileService.getOneById(projectFileDto);
        if (projectFileDto != null) {
            String earPath = projectFileDto.getPathOutput();
            BufferedReader reader = new BufferedReader(new FileReader(earPath));
            String ear = "";
            String line = null;
            while ((line = reader.readLine()) != null) {
                ear += line;
            }
            reader.close();
            json.put("status", true);
            json.put("ear", ear);
        } else {
            json.put("status", false);
        }

        return json;
    }

    @RequestMapping(value = "/analyze/process", method = {RequestMethod.POST})
    public AjaxResponeDto<Boolean> analyze_process(@RequestBody Map<String, Object> data, HttpServletRequest httpServletRequest) throws JSONException {
        Map<String, Object> auth = null;
        CdmSecretDto cdmSecretDto = null;
        Cookie csToken = null;
        Cookie tgsToken = null;
        AuthenticationDto sessionAuth = (AuthenticationDto) httpServletRequest.getSession()
                .getAttribute(Constants.CDM_WEB_USER);

        if (!this.verificationService.verifyUserSignature(data)) {
            throw new CdmErrorDto("Invalid signature");
        }

        try {
            auth = this.verificationService.getAuth(data);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CdmErrorDto("Invalid Auth");
        }

        try {
            cdmSecretDto = this.verificationService.getEsk(data);
            cdmSecretDto.setAuth(auth);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CdmErrorDto("Invalid Esk");
        }

        if (!auth.get(Constants.CDM_WEB_CNAME).equals(sessionAuth.getCname())) {
            throw new CdmErrorDto("Cname unmatched");
        }

        Map<String, Object> body = (Map<String, Object>) data.get(Constants.CDM_WEB_BODY);
        ProjectFileDto projectFileDto = new ProjectFileDto();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
        projectFileDto.setFileId(Integer.parseInt((String) body.get(Constants.CDM_FILE_ID)));
        projectFileDto = this.projectFileService.selectOne(projectFileDto);
        String now = simpleDateFormat.format(Calendar.getInstance().getTime());

        for (Cookie cookie : httpServletRequest.getCookies()) {
            if (cookie.getName().trim().equals(cdmSecretDto.getAuth().get(Constants.CDM_WEB_CNAME) + "_cs")) {
                csToken = cookie;
            } else if (cookie.getName().trim().equals(cdmSecretDto.getAuth().get(Constants.CDM_WEB_CNAME) + "_tgs")) {
                tgsToken = cookie;
            }

            if (csToken != null && tgsToken != null) break;
        }

        try {
            this.relayService.relay(projectFileDto, cdmSecretDto, data, Timestamp.valueOf(now));
        } catch (Exception e) {
            e.printStackTrace();
            throw new CdmErrorDto("Failed to relay the AC");
        }


//        ProjectFileDto projectFileDto = new ProjectFileDto();
//        projectFileDto.setFileId(Integer.parseInt((String) body.get(Constants.CDM_FILE_ID)));
//        projectFileDto = this.projectFileService.selectOne(projectFileDto);
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
//        String now = simpleDateFormat.format(Calendar.getInstance().getTime());
//        Timestamp requestTime = Timestamp.valueOf(now);
//
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put(Constants.CDM_FILE_ID, Integer.parseInt((String) body.get(Constants.CDM_FILE_ID)));
//        jsonObject.put(Constants.CDM_PROJECT_ID, projectFileDto.getProjectId());
//        jsonObject.put(Constants.CDM_USER_ID, sessionAuth.getCname());
//        jsonObject.put(Constants.CDM_REQUEST_TIME, now);
//
//        try {
//            this.relayService.replay(
//                    projectFileDto.getPath(),
//                    jsonObject,
//                    projectFileDto.getFileId(),
//                    projectFileDto.getProjectId(),
//                    requestTime);
//        } catch (IOException e) {
//            e.printStackTrace();
//            throw new CdmErrorDto("Failed to request analysis");
//        }

        return new AjaxResponeDto<>(true);

//        HttpSession session = request.getSession();
//        Integer fileId = Integer.parseInt(fileid);
//        ProjectFileDto projectFileDto = new ProjectFileDto();
//        projectFileDto.setFileId(fileId);
//        projectFileDto = this.projectFileService.selectOne(projectFileDto);
//        String path =  projectFileDto.getPath();
//        Integer projectId = projectFileDto.getProjectId();
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
//        Calendar calendar = Calendar.getInstance();
//        String today = simpleDateFormat.format(calendar.getTime());
//        Timestamp requestTime = Timestamp.valueOf(today);
//
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("file_id",fileId);
//        jsonObject.put("project_id", projectId);
//        jsonObject.put("user_id", ((AuthenticationDto)session.getAttribute(Constants.CDM_WEB_USER)).getCname());
//        jsonObject.put("request_time", today);
//        logger.info("보내는 json"+jsonObject.toString());
//
//        try {
//            this.relayService.replay(path, jsonObject, fileId, projectId, requestTime);
//        } catch (IOException e) {
//            e.printStackTrace();
//
//            AjaxResponeDto<Boolean> response = new AjaxResponeDto<>();
//            response.setData(false);
//
//            return response;
//        }
//
//        AjaxResponeDto<Boolean> response = new AjaxResponeDto<>();
//        response.setData(true);
//
//        return response;

    }

    @RequestMapping(value="/test/analysis", method = RequestMethod.POST)
    public AjaxResponeDto<String> testAnalysisRequest(HttpServletRequest request, @RequestBody Map<String, Object> data) {

        if (!this.verificationService.verifyUserSignature(data)) {
            throw new CdmErrorDto("invalid signature");
        }


        return new AjaxResponeDto<>("hello");
    }
}
