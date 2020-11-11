<%--
  Created by IntelliJ IDEA.
  User: JEON
  Date: 2020-03-31
  Time: 오후 8:56
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/crypto-js.js"></script>
    <script>
        // this is Base64 representation of the Java counterpart
        // byte[] keyValue = new byte[] { 'A', 'b', 'c', 'd', 'e', 'f', 'g',
        //                'h', 'i', 'j', 'k','l', 'm', 'n', 'o', 'p'};
        // String keyForJS = new BASE64Encoder().encode(keyValue);
        var base64Key = "QWJjZGVmZ2hpamtsbW5vcA==";
        console.log("base64Key = " + base64Key);

        // this is the actual key as a sequence of bytes
        var key = CryptoJS.enc.Base64.parse(base64Key);
        console.log("key = " + key);

        // this is the plain text
        var plaintText = "Hello, World!";
        console.log("plaintText = " + plaintText);

        // this is Base64-encoded encrypted data
        var encryptedData = CryptoJS.AES.encrypt(plaintText, key, {
            mode: CryptoJS.mode.ECB,
            padding: CryptoJS.pad.Pkcs7
        });
        console.log("encryptedData = " + encryptedData);

        // this is the decrypted data as a sequence of bytes
        var decryptedData = CryptoJS.AES.decrypt(encryptedData, key, {
            mode: CryptoJS.mode.ECB,
            padding: CryptoJS.pad.Pkcs7
        });
        console.log("decryptedData = " + decryptedData);

        // this is the decrypted data as a string
        var decryptedText = decryptedData.toString(CryptoJS.enc.Utf8);
        console.log("decryptedText = " + decryptedText);
    </script>
</head>
<body>

</body>
</html>
