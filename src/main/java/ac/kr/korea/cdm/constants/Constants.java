package ac.kr.korea.cdm.constants;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Constants {
    public static final String CDM_WEB_USER = "USER";
    public static final String CDM_WEB_SIGN_UP_WHICH = "which";

    public static final String CDM_WEB_ID = "id";
    public static final String CDM_WEB_PW = "pw";
    public static final String CDM_WEB_FIRST_NAME = "firstName";
    public static final String CDM_WEB_LAST_NAME = "lastName";
    public static final String CDM_WEB_COUNTRY_CODE = "countryCode";
    public static final String CDM_WEB_PUB = "pub";
    public static final String CDM_WEB_SIGNATURE = "signature";
    public static final String CDM_WEB_BODY = "body";
    public static final String CDM_WEB_CPK = "cpk";
    public static final String CDM_WEB_CNAME = "cname";
    public static final String CDM_WEB_TICKET = "ticket";
    public static final String CDM_FILE_ID = "file_id";
    public static final String CDM_PROJECT_ID = "project_id";
    public static final String CDM_USER_ID = "user_id";
    public static final String CDM_AUTH = "auth";
    public static final String CDM_ESK = "esk";
    public static final String CDM_SPK = "spk";
    public static final String CDM_SK = "sk";
    public static final String CDM_IV = "iv";
    public static final String CDM_SIG_AC = "sig_ac";
    public static final String CDM_CERTIFICATE = "certificate";
    public static final String CDM_TS = "ts";
    public static final String CDM_REQUEST_TIME = "request_time";
    public static final String CDM_ENCRYPTED_AR = "ear";
    public static final String CDM_STATUS = "status";
    public static final String CDM_SS_ID = "ss_id";
    public static final String CDM_WEB_REDIRECT_ROOT = "redirect:/";
    public static final String CDM_WEB_REDIRECT_INDEX = "redirect:/index";
    public static final String CDM_WEB_REDIRECT_ADMIN = "redirect:/admin";
//    public static String ALGORITHM_SIGNATURE = "SHA256withECDSA";
    public static final String ALGORITHM_BLOCK_CIPHER = "AES";
    public static final String EXPIRED_DATE_FORMAT = "yyyy-MM-dd HH:mm";
    public static final String URL_VERIFY = "/verify";
    public static String AS_DOMAIN;
    public static String CA_DOMAIN;
    public static String CS_DOMAIN;
    public static String TYPE_PKI;

    @Value("${as.domain}")
    public void setAsDomain(String url) {AS_DOMAIN = url;}

    @Value("${ca.domain}")
    public void setCaDomain(String url) {
        CA_DOMAIN = url;
    }

    @Value("${cs.domain}")
    public void setCsDomain(String url) {
        CS_DOMAIN = url;
    }

    @Value("${publickey.type}")
    public void setTypePki(String type) {TYPE_PKI = type;}
}
