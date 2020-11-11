package ac.kr.korea.cdm.service;

import ac.kr.korea.cdm.constants.Constants;
import ac.kr.korea.cdm.dto.CdmSecretDto;
import ac.kr.korea.cdm.util.CryptoHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

@Service
public class VerificationService {

    private static final Logger logger = LoggerFactory.getLogger(VerificationService.class);
    @Autowired private CryptoHelper cryptoHelper;

    public boolean verifyAcTicket(Map<String, Object> data) {
        try {
            Map<String, Object> body = (Map<String, Object>) data.get(Constants.CDM_WEB_BODY);
            String cname = (String) body.get(Constants.CDM_WEB_CNAME);
            String ticket = (String) body.get(Constants.CDM_WEB_TICKET);
            PublicKey publicKey = this.cryptoHelper.getPublic("CDM-KeyPair/CDM-PublicKey", Constants.TYPE_PKI);
            PrivateKey privateKey = this.cryptoHelper.getPrivate("CDM-KeyPair/CDM-PrivateKey", Constants.TYPE_PKI);
            String pemPublicKey = this.cryptoHelper.convertPublicKeyToPem(publicKey);

            Map<String, Object> bodyForVerify = new HashMap<>();
            bodyForVerify.put(Constants.CDM_WEB_CNAME, cname);
            bodyForVerify.put(Constants.CDM_WEB_TICKET, ticket);
            bodyForVerify.put(Constants.CDM_WEB_CPK, pemPublicKey);
            ObjectMapper objectMapper = new ObjectMapper();

            String signature = this.cryptoHelper.getSignatureBase64(objectMapper.writeValueAsString(bodyForVerify).getBytes(), privateKey);

            Map<String, Object> requestForAs = new HashMap<>();
            requestForAs.put(Constants.CDM_WEB_BODY, bodyForVerify);
            requestForAs.put(Constants.CDM_WEB_SIGNATURE, signature);

            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(Constants.AS_DOMAIN + Constants.URL_VERIFY);
            post.addHeader("content-type", "application/json");
            post.setEntity(new StringEntity(objectMapper.writeValueAsString(requestForAs)));
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String response = httpClient.execute(post, responseHandler);
//            logger.info(response);

            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
            Map<String, Object> bodyAs = (Map<String, Object>) responseMap.get(Constants.CDM_WEB_BODY);
            String publicKeyAs = (String) bodyAs.get(Constants.CDM_WEB_CPK);
            boolean verify = (boolean) bodyAs.get("verify");
            String signatureAs = (String) responseMap.get(Constants.CDM_WEB_SIGNATURE);

            return verify
                    && this.cryptoHelper.verifySignature(
                            objectMapper.writeValueAsBytes(bodyAs),
                    Base64Utils.decodeFromString(signatureAs),
                    this.cryptoHelper.restorePublicKeyFromPem(publicKeyAs));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean verifyUserSignature(Map<String, Object> data) {
        try {

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> body = (Map<String, Object>) data.get(Constants.CDM_WEB_BODY);
            String signature = (String) data.get(Constants.CDM_WEB_SIGNATURE);
            PublicKey publicKey = this.cryptoHelper.restorePublicKeyFromPem(
                    (String) body.get(Constants.CDM_WEB_CPK)
            );

            return this.cryptoHelper.verifySignature(objectMapper.writeValueAsBytes(body), Base64Utils.decodeFromString(signature), publicKey);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Map<String, Object> convertJsonToMap(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        return mapper.readValue(json, new TypeReference<Map<String, Object>>() {});
    }

    public Map<String, Object> getAuth(Map<String, Object> data) throws Exception {
        Map<String, Object> body = (Map<String, Object>) data.get(Constants.CDM_WEB_BODY);
        PrivateKey privateKey = this.cryptoHelper.getPrivate("CDM-KeyPair/CDM-PrivateKey", Constants.TYPE_PKI);
        String authBase64 = (String) body.get(Constants.CDM_AUTH);
        ObjectMapper objectMapper = new ObjectMapper();

        String authStr = new String(this.cryptoHelper.decryptWithRsa(Base64Utils.decodeFromString(authBase64), privateKey));
        return objectMapper.readValue(authStr, Map.class);
    }

    public CdmSecretDto getEsk(Map<String, Object> data) throws Exception {
        Map<String, Object> body = (Map<String, Object>) data.get(Constants.CDM_WEB_BODY);
        PrivateKey privateKey = this.cryptoHelper.getPrivate("CDM-KeyPair/CDM-PrivateKey", Constants.TYPE_PKI);
        String eskBase64 = (String) body.get(Constants.CDM_ESK);
        ObjectMapper objectMapper = new ObjectMapper();

        String eskStr = new String(this.cryptoHelper.decryptWithRsa(Base64Utils.decodeFromString(eskBase64), privateKey));
        Map<String, Object> eskMap = objectMapper.readValue(eskStr, Map.class);
        SecretKeySpec sk = new SecretKeySpec(Base64Utils.decodeFromString((String) eskMap.get(Constants.CDM_SK)), Constants.ALGORITHM_BLOCK_CIPHER);
        IvParameterSpec iv = new IvParameterSpec(Base64Utils.decodeFromString((String) eskMap.get(Constants.CDM_IV)));
        CdmSecretDto cdmSecretDto = new CdmSecretDto();
        cdmSecretDto.setSk(sk);
        cdmSecretDto.setIv(iv);
        return cdmSecretDto;
    }

}
