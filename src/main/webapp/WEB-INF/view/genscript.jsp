<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:eval expression="@environment.getProperty('cs.domain')" var="cs_domain"/>
<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>SB Admin - Dashboard</title>

    <!-- Custom fonts for this template-->
    <link href="${pageContext.request.contextPath}/vendor/fontawesome-free/css/all.min.css" rel="stylesheet"
          type="text/css">

    <!-- Page level plugin CSS-->
    <link href="${pageContext.request.contextPath}/vendor/datatables/dataTables.bootstrap4.css" rel="stylesheet">

    <!-- Custom styles for this template-->
    <link href="${pageContext.request.contextPath}/css/sb-admin.css" rel="stylesheet">
    <!-- Bootstrap core JavaScript-->
    <script src="${pageContext.request.contextPath}/vendor/jquery/jquery.min.js"></script>
    <script src="${pageContext.request.contextPath}/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>

    <!-- Core plugin JavaScript-->
    <script src="${pageContext.request.contextPath}/vendor/jquery-easing/jquery.easing.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jszip.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jsrsasign-all-min.js"></script>

    <!-- Page level plugin JavaScript-->
    <script src="${pageContext.request.contextPath}/vendor/datatables/jquery.dataTables.js"></script>

    <!-- Custom scripts for all pages-->
    <script src="${pageContext.request.contextPath}/js/sb-admin.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.cookie.js"></script>
    <script>
        $(document).ready(function () {
            var setCsFlag = false;

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


            $("#get-sign").click(() => {
                $("#get-passphrase").modal();
            });


            // CS 서명받아오기
            $("#ok-passphrase").click(function () {
                var id = "${USER }";
                var seed = $("#passphrase").val();
                var from = new Date();
                var to = new Date();

                to.setMinutes(from.getMinutes() + 30);

                var param_as = JSON.parse($.cookie(id + '_as'));

                var ts = from.toString2(true);

                var body = {
                    ticket: param_as.body.ticket,
                    ac: $("#r-script").val(),
                    id_s: "id_tgs",
                    sk: param_as.body.sk,
                    iv: param_as.body.iv,
                    auth: {cname: '${USER}', timestamp: from.toString2(true)},
                    time: {from: from.toString2(), to: to.toString2()},
                    ts: from.toString2(true),
                    cpk: localStorage.getItem(id + "_pub")
                };


                var pemPublicKey = localStorage.getItem(id + "_pub");
                var pemPrivateKey = localStorage.getItem(id + "_prv");
                var bs64Cert = localStorage.getItem(id + "_cert");
                var sig = new KJUR.crypto.Signature({alg: "SHA256withECDSA"});
                sig.init(pemPrivateKey, seed);
                sig.updateString(JSON.stringify(body));
                sig = sig.sign();

                var data = {
                    body: body,
                    signature: hextob64(sig)
                };

                $.ajax({
                    url: '${cs_domain}/get-sign',
                    contentType: 'application/json',
                    data: JSON.stringify(data),
                    type: 'post',
                    dataType: 'json',
                    success: function (data) {
                        $.cookie(id + '_cs', JSON.stringify(data));
                        setCsFlag = true;
                    },
                    error: function (error) {
                        console.log(error);
                    }
                });
            });


            // 티쥐에스 티켓
            $("#test-tgs").click(function () {
                if (setCsFlag) {
                    var url = $("#addr-tgs").val();
                    if (url === "") return;

                    var id = "${USER }";
                    var seed = $("#passphrase").val();
                    var from = new Date();
                    var to = new Date();

                    to.setMinutes(from.getMinutes() + 30);
                    var param_cs = JSON.parse($.cookie(id + '_cs'));
                    var ts = from.toString2(true);

                    var body = {
                        ticket: param_cs.body.ticket,
                        id_s: "id_ss",
                        sk: param_cs.body.sk,
                        iv: param_cs.body.iv,
                        auth: {cname: '${USER}', ts: from.toString2(true)},
                        time: {from: from.toString2(), to: to.toString2()},
                        ts: from.toString2(true),
                        cpk: localStorage.getItem(id + "_pub")
                    };

                    var pemPublicKey = localStorage.getItem(id + "_pub");
                    var pemPrivateKey = localStorage.getItem(id + "_prv");
                    var bs64Cert = localStorage.getItem(id + "_cert");
                    var sig = new KJUR.crypto.Signature({alg: "SHA256withECDSA"});
                    sig.init(pemPrivateKey, seed);
                    sig.updateString(JSON.stringify(body));
                    sig = sig.sign();

                    var data = {
                        body: body,
                        signature: hextob64(sig)
                    };

                    console.log(data);

                    $.ajax({
                        url: url,
                        type: 'post',
                        data: JSON.stringify(data),
                        dataType: 'json',
                        contentType: 'application/json',
                        success: function (resp) {
                            console.log(resp);
                        },
                        error: function (err) {
                            console.log(err);
                        }
                    });
                } else {
                    alert('click Get-Sign button first.');
                }
            });

            $("#test-ss").click(function () {
                if (setCsFlag) {
                    var url = $("#addr-ss").val();
                    if (url === "") return;

                    console.log(url);

                    var id = "${USER }";
                    var seed = $("#passphrase").val();
                    var from = new Date();
                    var to = new Date();

                    to.setMinutes(from.getMinutes() + 30);
                    var param_cs = JSON.parse($.cookie(id + '_cs'));
                    var ts = from.toString2(true);

                    var body = {
                        ticket: param_cs.body.ticket,
                        ac: $("#r-script").val(),
                        sig_ac: param_cs.body.sig_ac,
                        id_s: "id_ss",
                        sk: param_cs.body.sk,
                        iv: param_cs.body.iv,
                        auth: {cname: '${USER}', ts: from.toString2(true)},
                        time: {from: from.toString2(), to: to.toString2()},
                        ts: from.toString2(true),
                        cpk: localStorage.getItem(id + "_pub")
                    };

                    var pemPublicKey = localStorage.getItem(id + "_pub");
                    var pemPrivateKey = localStorage.getItem(id + "_prv");
                    var bs64Cert = localStorage.getItem(id + "_cert");
                    var sig = new KJUR.crypto.Signature({alg: "SHA256withECDSA"});
                    sig.init(pemPrivateKey, seed);
                    sig.updateString(JSON.stringify(body));
                    sig = sig.sign();

                    var data = {
                        body: body,
                        signature: hextob64(sig)
                    };

                    console.log(data);

                    $.ajax({
                        url: url,
                        type: 'post',
                        data: JSON.stringify(data),
                        dataType: 'json',
                        contentType: 'application/json',
                        success: function (resp) {
                            console.log(resp);
                        },
                        error: function (err) {
                            console.log(err);
                        }
                    });
                } else {
                    alert('click Get-Sign button first');
                }
            });
        });
    </script>

</head>

<body id="page-top">

<nav class="navbar navbar-expand navbar-dark bg-dark static-top">

    <a class="navbar-brand mr-1" href="${pageContext.request.contextPath}/index">Secure CDM Analysis Platform</a>

    <button class="btn btn-link btn-sm text-white order-1 order-sm-0" id="sidebarToggle" href="#">
        <i class="fas fa-bars"></i>
    </button>

    <!-- Navbar Search -->
    <form class="d-none d-md-inline-block form-inline ml-auto mr-0 mr-md-3 my-2 my-md-0">
        <div class="input-group">
            <input type="text" class="form-control" placeholder="Search for..." aria-label="Search"
                   aria-describedby="basic-addon2">
            <div class="input-group-append">
                <button class="btn btn-primary" type="button">
                    <i class="fas fa-search"></i>
                </button>
            </div>
        </div>
    </form>

    <!-- Navbar -->
    <ul class="navbar-nav ml-auto ml-md-0">
        <li class="nav-item dropdown no-arrow mx-1">
            <a class="nav-link dropdown-toggle" href="#" id="alertsDropdown" role="button" data-toggle="dropdown"
               aria-haspopup="true" aria-expanded="false">
                <i class="fas fa-bell fa-fw"></i>
                <span class="badge badge-danger">9+</span>
            </a>
            <div class="dropdown-menu dropdown-menu-right" aria-labelledby="alertsDropdown">
                <a class="dropdown-item" href="#">Action</a>
                <a class="dropdown-item" href="#">Another action</a>
                <div class="dropdown-divider"></div>
                <a class="dropdown-item" href="#">Something else here</a>
            </div>
        </li>
        <li class="nav-item dropdown no-arrow mx-1">
            <a class="nav-link dropdown-toggle" href="#" id="messagesDropdown" role="button" data-toggle="dropdown"
               aria-haspopup="true" aria-expanded="false">
                <i class="fas fa-envelope fa-fw"></i>
                <span class="badge badge-danger">7</span>
            </a>
            <div class="dropdown-menu dropdown-menu-right" aria-labelledby="messagesDropdown">
                <a class="dropdown-item" href="#">Action</a>
                <a class="dropdown-item" href="#">Another action</a>
                <div class="dropdown-divider"></div>
                <a class="dropdown-item" href="#">Something else here</a>
            </div>
        </li>
        <li class="nav-item dropdown no-arrow">
            <a class="nav-link dropdown-toggle" href="#" id="userDropdown" role="button" data-toggle="dropdown"
               aria-haspopup="true" aria-expanded="false">
                <i class="fas fa-user-circle fa-fw"></i>
            </a>
            <div class="dropdown-menu dropdown-menu-right" aria-labelledby="userDropdown">
                <a class="dropdown-item" href="#">Settings</a>
                <a class="dropdown-item" href="#">Activity Log</a>
                <div class="dropdown-divider"></div>
                <a class="dropdown-item" href="#" data-toggle="modal" data-target="#logoutModal">Logout</a>
            </div>
        </li>
    </ul>

</nav>

<div id="wrapper">

    <!-- Sidebar -->
    <ul class="sidebar navbar-nav">
        <li class="nav-item">
            <a class="nav-link" href="${pageContext.request.contextPath}/index">
                <i class="fas fa-fw fa-tachometer-alt"></i>
                <span>Dashboard</span>
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link" href="${pageContext.request.contextPath}/export-key">
                <i class="fas fa-fw fa-folder"></i>
                <span>Export Key</span>
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link" href="${pageContext.request.contextPath}/import-key">
                <i class="fas fa-fw fa-folder"></i>
                <span>Import Key</span>
            </a>
        </li>
        <li class="nav-item active">
            <a class="nav-link" href="${pageContext.request.contextPath}/gen-script">
                <i class="fas fa-fw fa-folder"></i>
                <span>Generate Script</span>
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link" href="${pageContext.request.contextPath}/atlas">
                <i class="fas fa-fw fa-folder"></i>
                <span>ATLAS</span>
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link" href="${pageContext.request.contextPath}/myproject">
                <i class="fas fa-fw fa-folder"></i>
                <span>My Project</span>
            </a>
        </li>
    </ul>

    <div id="content-wrapper">

        <div class="container-fluid">

            <!-- Breadcrumbs-->
            <ol class="breadcrumb">
                <li class="breadcrumb-item">
                    <a href="${pageContext.request.contextPath}/index">Dashboard</a>
                </li>
                <li class="breadcrumb-item active">Import Key</li>
            </ol>

            <h1>Generate Script ${USER}</h1>
            <hr>
            <div>
                <div class="form-group">
                    <label for="script">Script</label>
                    <textarea rows="10" class="form-control" id="r-script">
				        hello, R script?
				    </textarea>
                </div>

                <div class="form-group">
                    <label for="addr-tgs">Address of tgs</label>
                    <input type="text" id="addr-tgs" class="form-control"
                           placeholder="http://[address of tgs]/get-ticket">
                </div>
                <div class="form-group">
                    <label for="addr-ss">Address of ss</label>
                    <input type="text" id="addr-ss" class="form-control"
                           placeholder="http://[address of ss]/do-analysis">
                </div>

                <button class="btn btn-primary" id="get-sign">Get Sign</button>
                <button class="btn btn-primary" id="test-tgs">Test Tgs</button>
                <button class="btn btn-primary" id="test-ss">Test Ss</button>
            </div>
        </div>
        <!-- /.container-fluid -->

        <!-- Sticky Footer -->
        <footer class="sticky-footer">
            <div class="container my-auto">
                <div class="copyright text-center my-auto">
                    <span>Copyright © System And Network Security Lab. in Korea University 2019</span>
                </div>
            </div>
        </footer>

    </div>
    <!-- /.content-wrapper -->

</div>
<!-- /#wrapper -->

<!-- Scroll to Top Button-->
<a class="scroll-to-top rounded" href="#page-top">
    <i class="fas fa-angle-up"></i>
</a>

<!-- Logout Modal-->
<div class="modal fade" id="logoutModal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel"
     aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="exampleModalLabel">Ready to Leave?</h5>
                <button class="close" type="button" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">×</span>
                </button>
            </div>
            <div class="modal-body">Select "Logout" below if you are ready to end your current session.</div>
            <div class="modal-footer">
                <button class="btn btn-secondary" type="button" data-dismiss="modal">Cancel</button>
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/sign-out">Logout</a>
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