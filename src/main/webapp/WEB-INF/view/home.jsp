<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:eval expression="@environment.getProperty('as.domain')" var="as_domain"/>
<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>SB Admin - Login</title>

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
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/crypto-js.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.cookie.js"></script>
    <script>
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

        function leadingZeros(n, digits) {
            var zero = '';
            n = n.toString();

            if (n.length < digits) {
                for (i = 0; i < digits - n.length; i++)
                    zero += '0';
            }
            return zero + n;
        }

        Date.prototype.ymd = function () {
            var yyyy = leadingZeros(this.getFullYear(), 4);
            var MM = leadingZeros(this.getMonth() + 1, 2);
            var dd = leadingZeros(this.getDate(), 2);

            return yyyy + '-' + MM + '-' + dd;
        };
        Date.prototype.hms = function (ss) {
            var HH = leadingZeros(this.getHours(), 2);
            var mm = leadingZeros(this.getMinutes(), 2);
            if (ss) {
                var ss = leadingZeros(this.getSeconds(), 2);
                return HH + ':' + mm + ':' + ss;
            } else return HH + ':' + mm;
        };

        Date.prototype.toString2 = function (ss) {
            return this.ymd() + ' ' + this.hms(ss);
        };

        $(document).ready(function () {
            $("#btn-do-auth").click(function () {
                $("#get-passphrase").modal();
            });

            $("#ok-passphrase").click(function () {
                var cname = $("#id").val();
                var password = $("#password").val();
                var seed = $("#passphrase").val();
                var from = new Date();
                var to = new Date();

                to.setMinutes(from.getMinutes() + 30);

                var time = {from: from.toString2(), to: to.toString2()};

                var body = {
                    cname: cname,
                    password: password,
                    time: time,
                    id_s: "cs",
                    ts: from.toString2(true),
                    cpk: localStorage.getItem(cname + "_pub")
                };

                var pemPrivateKey = localStorage.getItem(cname + "_prv");
                var sig = new KJUR.crypto.Signature({alg: "SHA256withRSA"});
                sig.init(pemPrivateKey, seed);
                sig.updateString(JSON.stringify(body));
                sig = sig.sign();

                var data = {
                    body: body,
                    signature: hextob64(sig)
                };

                $.ajax({
                    url: "${as_domain}/do-auth",
                    contentType: 'application/json',
                    data: JSON.stringify(data),
                    type: 'post',
                    dataType: 'json',
                    success: function (resp) {
                        var body = resp.body;
                        var signatureAs = resp.signature;   // need to be verified.

                        body.cname = cname;
                        body.cpk = localStorage.getItem(cname + "_pub");

                        var sig = new KJUR.crypto.Signature({alg: "SHA256withRSA"});
                        sig.init(pemPrivateKey, seed);
                        sig.updateString(JSON.stringify(body));
                        sig = sig.sign();
                        var token = {
                            body: body,
                            signature: hextob64(sig)
                        }

                        $.cookie('token', JSON.stringify(token), {path: '${pageContext.request.contextPath}/'});
                        location.href = "${pageContext.request.contextPath}/index";
                    },
                    error: function (err) {
                        console.log(err);
                    }
                });

            });
        });
    </script>
</head>

<body class="bg-dark">

<div class="container">
    <div class="card card-login mx-auto mt-5">
        <div class="card-header">Login</div>
        <div class="card-body">
            <!--<form method="post" action="${as_domain}/sign-in-process">-->
            <form>
                <div class="form-group">
                    <div class="form-label-group">
                        <input type="text" id="id" name="id" class="form-control" placeholder="Enter ID"
                               required="required" autofocus="autofocus">
                        <label for="ID">ID</label>
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
                    <div class="checkbox">
                        <label>
                            <input type="checkbox" value="remember-me">
                            Remember Password
                        </label>
                    </div>
                </div>

                <button type="button" class="btn btn-primary btn-block" id="btn-do-auth">Sign-in</button>
            </form>
            <div class="text-center">
                <a class="d-block small mt-3" href="${pageContext.request.contextPath}/sign/up/which">Register an
                    Account</a>
                <a class="d-block small" href="#">Forgot Password?</a>
            </div>
        </div>
    </div>
</div>

<!-- Modal for Successfully import key -->
<div class="modal fade" id="get-passphrase">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h4>Info</h4>
            </div>

            <div class="modal-body">
                <div class="form-group">
                    <label for="passphase">Pass phrase:</label>
                    <input type="password" class="form-control" id="passphrase">
                </div>
            </div>

            <div class="modal-footer">
                <button class="btn btn-primary" type="button" data-dismiss="modal" id="ok-passphrase">OK</button>
            </div>
        </div>
    </div>
</div>

</body>

</html>
