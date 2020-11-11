package ac.kr.korea.cdm;

import ac.kr.korea.cdm.util.CryptoHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.TimeZone;

@Configuration
@SpringBootApplication
public class CdmApplication extends SpringBootServletInitializer {
    private static final Logger logger = LoggerFactory.getLogger(CdmApplication.class);

    @Autowired
    private CryptoHelper cryptoHelper;

    @Value("${publickey.type}")
    private String publicKeyType;

    public static void main(String[] args) {
        SpringApplication.run(CdmApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(CdmApplication.class);
    }

    @PostConstruct
    public void init() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, BadPaddingException, IllegalBlockSizeException, IOException {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));

        if (!new File("CDM-KeyPair").exists()) new File("CDM-KeyPair").mkdir();
        if (!new File("CDM-KeyPair/CDM-PublicKey").exists() || !new File("CDM-KeyPair/CDM-PrivateKey").exists()) {
//            logger.info("create key pair");
//            KeyPair keyPair = cryptoHelper.generateEcKeyPair();
            KeyPair keyPair = null;
            if (publicKeyType.toLowerCase().trim().equals("ec")) keyPair = cryptoHelper.generateEcKeyPair();
            else if (publicKeyType.toLowerCase().trim().equals("rsa")) keyPair = cryptoHelper.generateRsaKeyPair();
            cryptoHelper.writeToFile(new File("CDM-KeyPair/CDM-PublicKey"), keyPair.getPublic().getEncoded());
            cryptoHelper.writeToFile(new File("CDM-KeyPair/CDM-PrivateKey"), keyPair.getPrivate().getEncoded());
        }

        String path = "C:/Cdmproject/project"; //폴더 경로
        File folder = new File(path);
        // 해당 디렉토리가 없을경우 디렉토리를 생성합니다.
        if (!folder.exists()) {
            try {
                folder.mkdirs(); //폴더 생성합니다.
                logger.info("project folder was successfully created");
            } catch (Exception e) {
                e.getStackTrace();
            }
        } else {
            logger.info("project folder has been already created");
        }
    }
}
