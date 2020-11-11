<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
<%--    <script type="text/javascript" src="${pageContext.request.contextPath}/js/crypto-js.js"></script>--%>
    <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/crypto-js/3.1.9-1/crypto-js.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/FileSaver.js"></script>

    <!-- Page level plugin JavaScript-->
    <script src="${pageContext.request.contextPath}/vendor/datatables/jquery.dataTables.js"></script>

    <!-- Custom scripts for all pages-->
    <script src="${pageContext.request.contextPath}/js/sb-admin.min.js"></script>
    <script>

        var globalProjectId = null;
        var globalFileId = null;
        var globalSsId = null;

        Date.prototype.toString2 = function (ss) {
            return this.ymd() + ' ' + this.hms(ss);
        };

        function getRequestApproval(projectId, fileId, ssId) {
            $.ajax({
                url: '${pageContext.request.contextPath}/project/request-approval',
                type: 'GET',
                data: {
                    project_id: projectId,
                    file_id: fileId,
                    ss_id: ssId
                }
            });
        }

        async function getServiceServerAddress(ssId) {
            return $.ajax({
                url: '${pageContext.request.contextPath}/project/get-ss-address',
                data: {
                    ss_id: ssId
                },
                type: 'GET'
            });
        }

        async function checkApprovedToRead (projectId, fileId, ssId) {
            var address = await getServiceServerAddress(ssId);

            return $.ajax({
                url: 'http://' + address.body.ipAddr + "/ss/check-approved-to-read",
                type: 'GET',
                data: {
                    project_id: projectId,
                    file_id: fileId,
                    cname: '${USER.cname}'
                }
            });
        }

        async function getAnalysisResult(pi, fi, si) {
            globalProjectId = pi;
            globalFileId = fi;
            globalSsId = si;

            $("#get-passphrase").modal();
        }

        function verifySignature(data) {
            var body = data.body;
            var signature = data.signature;
            var spk = KEYUTIL.getKey(b64tohex(body.spk), null, "pkcs8pub");

            var sig = new KJUR.crypto.Signature({alg: "SHA256withRSA"});
            sig.init(spk);
            sig.updateString(JSON.stringify(body));
            return sig.verify(b64tohex(signature));
        }

        async function getEncryptedAR(projectId, fileId) {
            var ear = null;
            await $.ajax({
                url: '${pageContext.request.contextPath}/project/get-ear',
                type: 'GET',
                data: {
                    project_id: projectId,
                    file_id: fileId,
                },
                success: (data) => {
                    ear = data;
                }
            });
            return ear;
        }

        function getPlainAR(sk, iv, ear) {
            // should be patched for zip file...
            sk = CryptoJS.enc.Base64.parse(sk);
            iv = CryptoJS.enc.Base64.parse(iv);
            var rawData = CryptoJS.enc.Base64.parse(ear);
            var decrypted = CryptoJS.AES.decrypt(
                {ciphertext: rawData},
                sk,
                {
                    mode: CryptoJS.mode.CBC,
                    iv: iv,
                    padding: CryptoJS.pad.Pkcs7
                }
            );
            var file = new Blob([decrypted.toString(CryptoJS.enc.Utf8)], {type: 'text/plain'});
            saveAs(file, "test.txt");

        }

        $(document).ready(() => {

            $("#ok-passphrase").click(async () => {
                var passphrase = $("#passphrase").val();
                var address = await getServiceServerAddress(globalSsId);
                var id = '${USER.cname}';
                var body = {
                    project_id: globalProjectId,
                    file_id: globalFileId,
                    cname: id,
                    cpk: localStorage.getItem(id + "_pub")
                };

                var pemPrivateKey = localStorage.getItem(id + "_prv");
                var sig = new KJUR.crypto.Signature({alg: "SHA256withRSA"});
                sig.init(pemPrivateKey, passphrase);
                sig.updateString(JSON.stringify(body));
                sig = sig.sign();

                var data = {
                    body: body,
                    signature: hextob64(sig)
                };

                await $.ajax({
                    url: 'http://' + address.body.ipAddr + "/ss/get-secret",
                    type: 'POST',
                    data: JSON.stringify(data),
                    contentType: 'application/json',
                    success: async (data) => {
                        if (verifySignature(data)) {
                            var sk = data.body.sk;
                            var iv = data.body.iv;
                            var ear = await getEncryptedAR(globalProjectId, globalFileId);
                            if (ear.status) {
                                getPlainAR(sk, iv, ear.ear);
                            }
                        } else console.log("invalid signature");
                    },
                    error: (err) => {
                        console.log(err);
                    }
                });

                globalProjectId = null;
                globalFileId = null;
                globalSsId = null;
            });


            $(".inst-status").each((index, item) => {

                $.ajax({
                    url: "${pageContext.request.contextPath}/project/check-progress",
                    type: "GET",
                    data: {
                        project_id: ${projectFileDto.projectId},
                        file_id: ${projectFileDto.fileId},
                        ss_id: parseInt($(item).attr("id").split("-")[1])
                    },
                    success: async (data) => {
                        var status = data.status;
                        var projectId = data.analysisProjectId;
                        var fileId = data.analysisFileId;
                        var ssId = parseInt($(item).attr("id").split("-")[1]);

                        var btn = null;
                        if (status == 'complete') {
                            btn = $('<button class="btn btn-primary" onclick="getRequestApproval(' + projectId + ', ' + fileId + ', ' + ssId + ');">Request for approval</button>');
                        } else if (status == 'pending') {
                            var apprv = await checkApprovedToRead(projectId, fileId, ssId);
                            if (apprv) {
                                btn = $('<button class="btn btn-info" onclick="getAnalysisResult(' + projectId + ', ' + fileId +', ' + ssId + ');">Download</button>');
                            } else btn = $('<button class="btn btn-info" disabled>Pending</button>');
                        } else {
                            btn = $('<button class="btn btn-danger" disabled>' + status + '</button>');
                        }
                        $(item).append(btn);
                    },
                    error: (err) => {
                        console.log(err);
                    }
                });
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
        <li class="nav-item">
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
        <li class="nav-item active">
            <a class="nav-link" href="${pageContext.request.contextPath}/project">
                <i class="fas fa-fw fa-folder"></i>
                <span>My Project</span>
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link" href="${pageContext.request.contextPath}/code-editor">
                <i class="fas fa-fw fa-folder"></i>
                <span>Code Editor</span>
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
                <li class="breadcrumb-item active">
                    <a href="${pageContext.request.contextPath}/project/projectdetail">My Project</a>
                </li>
            </ol>

            <h1>Analysis Status</h1>
            <hr>

            <table class="table table-bordered" id="status-table">
                <thead>
                    <tr>
                        <td>INSTITUTE</td>
                        <td>STATUS</td>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${ssList}" var="serviceServerDto" varStatus="varStatus">
                        <tr>
                            <td>${serviceServerDto.institute}</td>
                            <td>
                                <span id="${serviceServerDto.institute}-${serviceServerDto.id}" class="inst-status"></span>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
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
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/sign/out">Logout</a>
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