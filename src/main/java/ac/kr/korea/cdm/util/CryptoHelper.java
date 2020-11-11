package ac.kr.korea.cdm.util;

import ac.kr.korea.cdm.constants.Constants;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Component
public class CryptoHelper {

    public KeyPair generateEcKeyPair() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchProviderException {
        Security.addProvider(new BouncyCastleProvider());
//    	ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("prime256v1");
//        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256r1");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
        SecureRandom random = new SecureRandom();
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA");
        keyPairGenerator.initialize(ecSpec, random);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        return keyPair;
    }

    public KeyPair generateRsaKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.genKeyPair();
        return keyPair;
    }

    public PrivateKey getPrivate(String filename, String cryptoType) throws Exception {
        // cryptoType {RSA, EC}
        byte[] keyBytes = Files.readAllBytes(new File(filename).toPath());
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance(cryptoType);
        return kf.generatePrivate(spec);
    }

    public PublicKey getPublic(String filename, String cryptoType) throws Exception {
        // cryptoType {RSA, EC}
        byte[] keyBytes = Files.readAllBytes(new File(filename).toPath());
        return this.getPublic(keyBytes, cryptoType);
    }

    public PublicKey getPublic(byte[] bytes, String cryptoType) throws NoSuchAlgorithmException, InvalidKeySpecException {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
        KeyFactory kf = KeyFactory.getInstance(cryptoType);
        return kf.generatePublic(spec);
    }

    public void writeToFile(File output, byte[] toWrite)
            throws IllegalBlockSizeException, BadPaddingException, IOException {
        FileOutputStream fos = new FileOutputStream(output);
        fos.write(toWrite);
        fos.flush();
        fos.close();
    }

    public PublicKey restorePublicKeyFromPem(String pem) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        Reader reader = new StringReader(pem);
        SubjectPublicKeyInfo parser = (SubjectPublicKeyInfo) new PEMParser(reader).readObject();
        return new JcaPEMKeyConverter().getPublicKey(parser);
    }

    public String encryptWithAes(String data, Key sk, IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, sk, iv);

        byte[] dataEnc = cipher.doFinal(data.getBytes());
        return Base64Utils.encodeToString(dataEnc);
    }

    public byte[] decryptWithAes(byte[] data, Key sk, IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, sk, iv);

        return cipher.doFinal(data);
    }

    public boolean verifySignature(byte[] data, byte[] signature, PublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature sign = null;
        if (Constants.TYPE_PKI.toLowerCase().trim().equals("ec")) {
            sign = Signature.getInstance("SHA256withECDSA");
        } else if (Constants.TYPE_PKI.toLowerCase().trim().equals("rsa")) {
            sign = Signature.getInstance("SHA256withRSA");
        }

        sign.initVerify(publicKey);
        sign.update(data);
        return sign.verify(signature);
    }

    public byte[] encryptWithRsa(byte[] data, PublicKey publicKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    public byte[] decryptWithRsa(byte[] data, PrivateKey privateKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }

    public String getSignatureBase64(byte[] data, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = null;
        if (Constants.TYPE_PKI.toLowerCase().trim().equals("ec")) {
            signature = Signature.getInstance("SHA256withECDSA");
        } else if (Constants.TYPE_PKI.toLowerCase().trim().equals("rsa")) {
            signature = Signature.getInstance("SHA256withRSA");
        }

        signature.initSign(privateKey);
        signature.update(data);
        return Base64Utils.encodeToString(signature.sign());
    }

    public String convertPublicKeyToPem(PublicKey publicKey) throws IOException {
        StringWriter stringWriter = new StringWriter();
        PemWriter pemWriter = new PemWriter(stringWriter);
        pemWriter.writeObject(new PemObject("PUBLIC KEY", publicKey.getEncoded()));
        pemWriter.flush();
        pemWriter.close();
        String pemPublicKey = stringWriter.toString();
        stringWriter.close();
        return pemPublicKey;
    }
}
