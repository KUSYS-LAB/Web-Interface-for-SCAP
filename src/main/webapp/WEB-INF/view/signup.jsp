<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:eval expression="@environment.getProperty('as.domain')" var="as_domain"/>
<spring:eval expression="@environment.getProperty('ca.domain')" var="ca_domain"/>
<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>SB Admin - Register</title>

    <!-- Custom fonts for this template-->
    <link href="${pageContext.request.contextPath}/vendor/fontawesome-free/css/all.min.css" rel="stylesheet"
          type="text/css">

    <!-- Custom styles for this template-->
    <link href="${pageContext.request.contextPath}/css/sb-admin.css" rel="stylesheet">
    <!-- Bootstrap core JavaScript-->
    <script src="${pageContext.request.contextPath}/vendor/jquery/jquery.min.js"></script>
    <script src="${pageContext.request.contextPath}/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>

    <!-- Core plugin JavaScript-->
    <script src="${pageContext.request.contextPath}/vendor/jquery-easing/jquery.easing.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jsrsasign-all-min.js"></script>
    <script type="text/javascript">
        $.fn.serializeObject = function () {

            var result = {};
            var extend = function (i, element) {
                var node = result[element.name];
                if ("undefined" !== typeof node && node !== null) {
                    if ($.isArray(node)) {
                        node.push(element.value)
                    } else {
                        result[element.name] = [node, element.value]
                    }
                } else {
                    result[element.name] = element.value
                }
            };

            $.each(this.serializeArray(), extend);
            return result
        };

        $(document).ready(function () {
            var id = null;
            var privateKey = null;
            var publicKey = null;
            var pemPublicKey = null;
            var pemPrivateKey = null;
            var certificate = null;

            $("#seed").on('hidden.bs.modal', function () {
                var seed = $("#seed-value").val();
                var id = $("#id").val();

                pemPrivateKey = KEYUTIL.getPEM(privateKey, "PKCS8PRV", seed, "AES-256-CBC");

                // var sig = new KJUR.crypto.Signature({alg: "SHA256withECDSA"});
                var sig = new KJUR.crypto.Signature({alg: "SHA256withRSA"});
                sig.init(pemPrivateKey, seed);
                sig.updateString(atob(certificate));
                var sigValueHex = sig.sign();

                localStorage.setItem(id + "_pub", pemPublicKey);
                localStorage.setItem(id + "_prv", pemPrivateKey);
                localStorage.setItem(id + "_cert", certificate);
                localStorage.setItem(id + "_sig", sigValueHex);
                //$("form").get(0).submit();

                var data = $("#form-sign-up").serializeObject();

                $.ajax({
                    url: "${as_domain}/sign-up/process",
                    data: JSON.stringify(data),
                    contentType: "application/json",
                    dataType: "json",
                    type: "post",
                    success: function (resp) {
                        // WI의 Proejct폴더 하위에 방금 가입한 회원의 전용 폴더를 생성하는 request
                        var cdm_member = {member_id: $("#id").val()};
                        $.ajax({
                            url: "${pageContext.request.contextPath}/sign/up/process",
                            data: JSON.stringify(cdm_member),
                            type: "POST",
                            contentType: 'application/json',
                            success: function (string) {
                                console.log(string);
                            },
                            error: function (xhr, status, error) {
                                console.log(error);
                            }
                        });
                        $(location).attr('href', '${pageContext.request.contextPath}/');
                    },
                    error: function (err) {
                        console.log(err);
                    }
                });
            });

            $("#signup").click(function () {
                <c:if test="${which == 'institutional'}">
                // ???????????????
                </c:if>
                <c:if test="${which == 'personal'}">
                var firstNameEn = $("#firstNameEn").val();
                var lastNameEn = $("#lastNameEn").val();
                var countryCode = $("#countryCode").val();
                // var keyPair = KEYUTIL.generateKeypair("EC", "secp256r1");
                var keyPair = KEYUTIL.generateKeypair("RSA", 2048);

                privateKey = keyPair.prvKeyObj;
                publicKey = keyPair.pubKeyObj;
                pemPublicKey = KEYUTIL.getPEM(publicKey);

                var data = {
                    'pub': pemPublicKey,
                    'firstName': firstNameEn,
                    'lastName': lastNameEn,
                    'countryCode': countryCode
                };
                console.log(data);

                $.ajax({
                    url: "${ca_domain}/get-cert",
                    data: JSON.stringify(data),
                    type: 'post',
                    dataType: 'json',
                    contentType: 'application/json',
                    success: function (resp) {
                        certificate = resp.data;
                        $("#seed").modal();
                    },
                    error: function (error) {
                        console.log(error)
                    }
                });
                </c:if>
            });
        });
    </script>
</head>

<body class="bg-dark">

<div class="container">
    <div class="card card-register mx-auto mt-5">
        <div class="card-header">Register an Account</div>
        <div class="card-body">
            <!--<form method="post" action="${as_domain}/sign-up/process">-->
            <form id="form-sign-up">
                <c:if test="${which == 'institutional' }">

                    <div class="form-group">
                        <div class="form-label-group">
                            <input type="text" id="id" name="id" class="form-control" placeholder="Enter ID"
                                   required="required" autofocus="autofocus">
                            <label for="id">ID</label>
                        </div>
                    </div>

                    <div class="form-group">
                        <div class="form-label-group">
                            <input type="password" id="password" name="password" class="form-control"
                                   placeholder="Enter Password" required="required">
                            <label for="password">Password</label>
                        </div>
                    </div>

                    <div class="form-group">
                        <div class="form-label-group">
                            <input type="text" class="form-control" name="firstNameEn" id="firstNameEn"
                                   placeholder="Enter Institution in English" name="firstNameEn" required="required">
                            <label for="fristNameEn">Institution(en): </label>
                        </div>
                    </div>

                    <div class="form-group">
                        <div class="form-label-group">
                            <label for="fristNameKo">Institution(ko): </label>
                            <input type="text" class="form-control" name="firstNameKo" id="firstNameKo"
                                   placeholder="Enter Institution in Korean" name="firstNameKo" required="required">
                        </div>
                    </div>

                    <div class="form-group">
                        <select class="form-control" id="countryCode" name="countryCode">
                            <c:forEach var="cc" items="${countryCodes }">
                                <option value="${cc.code3 }">${cc.country}</option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="form-group">
                        <input type="hidden" class="form-control" id="typeCode" name="typeCode" value="2" required>
                    </div>

                </c:if>
                <c:if test="${which == 'personal' }">

                    <div class="form-group">
                        <div class="form-label-group">
                            <input type="text" id="id" name="id" class="form-control" placeholder="Enter ID"
                                   required="required" autofocus="autofocus">
                            <label for="id">ID</label>
                        </div>
                    </div>

                    <div class="form-group">
                        <div class="form-label-group">
                            <input type="password" id="password" name="password" class="form-control"
                                   placeholder="Enter Password" required="required">
                            <label for="password">Password</label>
                        </div>
                    </div>

                    <div class="form-group">
                        <div class="form-row">
                            <div class="col-md-6">
                                <div class="form-label-group">
                                    <input type="text" id="firstNameEn" name="firstNameEn" class="form-control"
                                           placeholder="Enter First name in English" required="required">
                                    <label for="firstNameEn">First name(en)</label>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="form-label-group">
                                    <input type="text" id="lastNameEn" name="lastNameEn" class="form-control"
                                           placeholder="Enter Last name in English" required="required">
                                    <label for="lastNameEn">Last name(en)</label>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="form-group">
                        <div class="form-row">
                            <div class="col-md-6">
                                <div class="form-label-group">
                                    <input type="text" id="firstNameKo" name="firstNameKo" class="form-control"
                                           placeholder="Enter First name in Korean" required="required">
                                    <label for="firstNameKo">First name(ko)</label>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="form-label-group">
                                    <input type="text" id="lastNameKo" name="lastNameKo" class="form-control"
                                           placeholder="Enter Last name in Korean" required="required">
                                    <label for="lastNameKo">Last name(ko)</label>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="form-group">
                        <select class="form-control" id="countryCode" name="countryCode">
                            <c:forEach var="cc" items="${countryCodes }">
                                <option value="${cc.code3 }">${cc.country}</option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="form-group">
                        <div class="form-label-group">
                            <input type="text" id="institute" name="institute" class="form-control"
                                   placeholder="Enter Institute" required="required">
                            <label for="institute">Institute</label>
                        </div>
                    </div>

                    <div class="form-group">
                        <input type="hidden" class="form-control" id="typeCode" name="typeCode" value="1" required>
                    </div>
                </c:if>
                <button type="button" class="btn btn-primary btn-block" id="signup">Sign-up</button>
            </form>

            <div class="modal" id="seed">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h4>put the password for protecting private key</h4>
                        </div>

                        <div class="modal-body">
                            <div class="form-group">
                                <label for="seed">password: </label>
                                <input type="password" class="form-control" id="seed-value" name="seed-value" required>
                            </div>
                        </div>

                        <div class="modal-footer">
                            <button type="button" class="btn btn-danger" data-dismiss="modal">Ok</button>
                        </div>
                    </div>
                </div>
            </div>

            <div class="text-center">
                <a class="d-block small mt-3" href="${pageContext.request.contextPath}/">Login Page</a>
                <a class="d-block small" href="#">Forgot Password?</a>
            </div>
        </div>
    </div>
</div>

</body>

</html>
