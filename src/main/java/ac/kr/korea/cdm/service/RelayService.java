package ac.kr.korea.cdm.service;

import ac.kr.korea.cdm.constants.Constants;
import ac.kr.korea.cdm.dto.*;
import ac.kr.korea.cdm.mapper.ServiceServerMapper;
import ac.kr.korea.cdm.util.CryptoHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RelayService {
    private static final Logger logger = LoggerFactory.getLogger(RelayService.class);
//    @Autowired
//    private ProjectRequestService projectRequestService;
    @Autowired
    private ProjectFileService projectFileService;
    @Autowired
    private ServiceServerMapper ssMapper;
    @Autowired
    private CryptoHelper cryptoHelper;


//    public void replay(String path, JSONObject jsonObject, Integer fileId, Integer projectId, Timestamp requestTime) throws IOException {
//        List<ServiceServerDto> ssList = this.ssMapper.getAllServiceServers();
//
//        for (ServiceServerDto ss: ssList) {
//            logger.info(ss.toString());
//            HttpClient httpclient = HttpClientBuilder.create().build();
//            // 파일을 수신할 서버주소
//            HttpPost httppost = new HttpPost("http://" + ss.getIpAddr() + "/ss/analyze");
//            File file = new File(path);
//            FileBody fileBody = new FileBody(file);
//            MultipartEntity entry = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
//            entry.addPart("upload_file", fileBody);
//            entry.addPart("json",new StringBody(String.valueOf(jsonObject), ContentType.APPLICATION_JSON));
//            httppost.setEntity(entry);
//            HttpResponse response = httpclient.execute(httppost);
//            //response.getEntity().getContent(); //이걸로 응답가능?
//            int status = response.getStatusLine().getStatusCode();
//            logger.info("status : "+status);
//            if(status == HttpStatus.SC_OK){
//                logger.info("파일전송 성공");
//                ProjectRequestDto projectRequestDto = new ProjectRequestDto(projectId, fileId, requestTime);
//                this.projectRequestService.insertOne(projectRequestDto);
//            }
//        }
//    }

    public Map<String, Object> checkProgress (AuthenticationDto auth, ProjectFileDto projectFileDto, int serviceServerId) throws JsonProcessingException {
        ServiceServerDto serviceServerDto = this.ssMapper.getOne(serviceServerId);
        logger.info(serviceServerDto.toString());
        String url = "http://" + serviceServerDto.getIpAddr() + "/ss/check-progress";

        HttpHeaders headers = new HttpHeaders();

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(url)
                .queryParam(Constants.CDM_WEB_CNAME, auth.getCname())
                .queryParam(Constants.CDM_PROJECT_ID, projectFileDto.getProjectId())
                .queryParam(Constants.CDM_FILE_ID, projectFileDto.getFileId());

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, new HttpEntity<String>(headers), String.class);
        String response = responseEntity.getBody();
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readValue(response, Map.class);
    }

    public void getAnalysisResult(AuthenticationDto auth, ProjectFileDto projectFileDto, int serverServerId) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        ServiceServerDto serviceServerDto = this.ssMapper.getOne(serverServerId);
        String url = "http://" + serviceServerDto.getIpAddr() + "/ss/get-analysis-result";

        HttpHeaders headers = new HttpHeaders();

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(url)
                .queryParam(Constants.CDM_WEB_CNAME, auth.getCname())
                .queryParam(Constants.CDM_PROJECT_ID, projectFileDto.getProjectId())
                .queryParam(Constants.CDM_FILE_ID, projectFileDto.getFileId());

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, new HttpEntity<String>(headers), String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> json = objectMapper.readValue(responseEntity.getBody(), Map.class);
        String signature = (String) json.get(Constants.CDM_WEB_SIGNATURE);
        Map<String, Object> body = (Map<String, Object>) json.get(Constants.CDM_WEB_BODY);
        PublicKey spk = this.cryptoHelper.getPublic(Base64Utils.decodeFromString((String) body.get(Constants.CDM_SPK)), Constants.TYPE_PKI);

        boolean verify = this.cryptoHelper.verifySignature(objectMapper.writeValueAsBytes(body), Base64Utils.decodeFromString(signature), spk);
        if (verify) {
            String ear = (String) body.get(Constants.CDM_ENCRYPTED_AR);
            String parentPath = new File(projectFileDto.getPath()).getParent();
            File earFile = new File(parentPath + "\\ear");

            FileWriter fileWriter = new FileWriter(earFile);
            fileWriter.write(ear);
            fileWriter.close();

            projectFileDto.setPathOutput(earFile.getPath());
            this.projectFileService.updatePathOutput(projectFileDto);
        }
    }

    public void relay(ProjectFileDto projectFileDto, CdmSecretDto cdmSecretDto, Map<String, Object> data, Timestamp requestTime) throws Exception {
        Thread relayThread = new Thread(
                new RelayRunnable(
                        this.projectFileService,
                        this.ssMapper,
                        this.cryptoHelper,
                        data,
                        projectFileDto,
                        cdmSecretDto,
                        requestTime));
        relayThread.start();
    }

    private class RelayRunnable implements Runnable {
//        private ProjectRequestService projectRequestService;
        private ProjectFileService projectFileService;
        private ServiceServerMapper ssMapper;
        private CryptoHelper cryptoHelper;
        private Map<String, Object> data;
        private ProjectFileDto projectFileDto;
        private CdmSecretDto cdmSecretDto;
        private Timestamp requestTime;

        public RelayRunnable (
//                ProjectRequestService projectRequestService,
                ProjectFileService projectFileService,
                ServiceServerMapper ssMapper,
                CryptoHelper cryptoHelper,
                Map<String, Object> data,
                ProjectFileDto projectFileDto,
                CdmSecretDto cdmSecretDto,
                Timestamp requestTime) {
//            this.projectRequestService = projectRequestService;
            this.projectFileService = projectFileService;
            this.ssMapper = ssMapper;
            this.cryptoHelper = cryptoHelper;
            this.data = data;
            this.projectFileDto = projectFileDto;
            this.cdmSecretDto = cdmSecretDto;
            this.requestTime = requestTime;
        }

        @Override
        public void run() {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
                String now = simpleDateFormat.format(Calendar.getInstance().getTime());
                List<ServiceServerDto> ssList = this.ssMapper.getAllServiceServers();
                Map<String, Object> body = (Map<String, Object>) data.get(Constants.CDM_WEB_BODY);
                PrivateKey privateKey = this.cryptoHelper.getPrivate("CDM-KeyPair/CDM-PrivateKey", Constants.TYPE_PKI);
                PublicKey publicKey = this.cryptoHelper.getPublic("CDM-KeyPair/CDM-PublicKey", Constants.TYPE_PKI);
                String eskStr = new String(this.cryptoHelper.decryptWithRsa(Base64Utils.decodeFromString((String) body.get(Constants.CDM_ESK)), privateKey));
                FileSystemResource ac = new FileSystemResource(new File(projectFileDto.getPath()));

                for (ServiceServerDto serviceServerDto : ssList) {
                    PublicKey ssPublicKey = this.getPubilcKeyFromSs(serviceServerDto.getIpAddr());
                    String esk = Base64Utils.encodeToString(this.cryptoHelper.encryptWithRsa(eskStr.getBytes(), ssPublicKey));
                    String auth = Base64Utils.encodeToString(this.cryptoHelper.encryptWithRsa(objectMapper.writeValueAsBytes(cdmSecretDto.getAuth()), ssPublicKey));

                    Map<String, Object> ssData = new HashMap<>();
                    Map<String, Object> ssBody = new HashMap<>();
                    ssBody.put(Constants.CDM_WEB_TICKET, body.get(Constants.CDM_WEB_TICKET));
                    ssBody.put(Constants.CDM_SIG_AC, body.get(Constants.CDM_SIG_AC));
                    ssBody.put(Constants.CDM_ESK, esk);
                    ssBody.put(Constants.CDM_AUTH, auth);
                    ssBody.put(Constants.CDM_TS, now);
                    ssBody.put(Constants.CDM_PROJECT_ID, projectFileDto.getProjectId());
                    ssBody.put(Constants.CDM_FILE_ID, projectFileDto.getFileId());
                    ssBody.put(Constants.CDM_SPK, Base64Utils.encodeToString(publicKey.getEncoded()));
                    String signature = this.cryptoHelper.getSignatureBase64(objectMapper.writeValueAsBytes(ssBody), privateKey);
                    ssData.put(Constants.CDM_WEB_BODY, ssBody);
                    ssData.put(Constants.CDM_WEB_SIGNATURE, signature);

                    boolean success = this.requestToAnalyze("http://" + serviceServerDto.getIpAddr() + "/ss/analyze", ssData, ac);
                    if (success) {
//                        logger.info(serviceServerDto.toString() + " success");
//                        ProjectRequestDto projectRequestDto = new ProjectRequestDto(
//                                projectFileDto.getProjectId(),
//                                projectFileDto.getFileId(),
//                                this.requestTime);
//                        this.projectRequestService.insertOne(projectRequestDto);
                        projectFileDto.setRequestTime(this.requestTime);
//                        logger.info("relay-projectFileDto-" + projectFileDto.toString());
                        this.projectFileService.updateRequestTime(projectFileDto);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private boolean requestToAnalyze(String url, Map<String, Object> data, FileSystemResource ac) throws JsonProcessingException {
            ObjectMapper objectMapper = new ObjectMapper();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("data", data);
            body.add("ac", ac);
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, requestEntity, String.class);
            String response = responseEntity.getBody();
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
            return (boolean) responseMap.get(Constants.CDM_WEB_BODY);
        }

        private PublicKey getPubilcKeyFromSs(String IpAddr) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
            ObjectMapper objectMapper = new ObjectMapper();
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet httpGet = new HttpGet("http://" + IpAddr + "/ss/get-cert");
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String response = httpClient.execute(httpGet, responseHandler);

            Map<String, Object> data = objectMapper.readValue(response, Map.class);
            Map<String, Object> body = (Map<String, Object>) data.get(Constants.CDM_WEB_BODY);

            return this.cryptoHelper.getPublic(
                    Base64Utils.decodeFromString((String) body.get(Constants.CDM_CERTIFICATE)),
                    Constants.TYPE_PKI);
        }
    }
}
