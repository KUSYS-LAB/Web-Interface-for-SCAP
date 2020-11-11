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

    <script src="https://code.jquery.com/jquery-3.3.1.min.js"></script>

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
    <script type="text/javascript" src="${pageContext.request.contextPath}/vendor/jquery-easing/jquery.easing.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.cookie.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jszip.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jsrsasign-all-min.js"></script>

    <!-- Page level plugin JavaScript-->
    <script src="${pageContext.request.contextPath}/vendor/datatables/jquery.dataTables.js"></script>

    <!-- Custom scripts for all pages-->
    <script src="${pageContext.request.contextPath}/js/sb-admin.min.js"></script>

    <script>
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
            var wiPublicKey = null;
            var fileid = null;

            $.ajax({
                url: '${pageContext.request.contextPath}/pki/get-cert',
                type: 'get',
                success: (resp) => {
                    wiPublicKey = KEYUTIL.getKey(b64nltohex(resp.data), null, 'pkcs8pub');
                },
                error: (err) => {
                    console.log(err);
                }
            });

            $('#ok-passphrase').click(() => {
                var id = '${USER.cname}';
                var seed = $("#passphrase").val();
                var csTicket = JSON.parse($.cookie(id + '_cs'));
                var tgsTicket = JSON.parse($.cookie(id + '_tgs'));
                var now = new Date().toString2(true);
                var esk = hextob64(KJUR.crypto.Cipher.encrypt(
                    JSON.stringify(tgsTicket.body.esk), wiPublicKey, 'RSAOAEP'));
                var auth = hextob64(KJUR.crypto.Cipher.encrypt(JSON.stringify({
                    cname: id,
                    timestamp: now
                }), wiPublicKey, 'RSAOAEP'));
                console.log(csTicket);
                console.log(tgsTicket);

                var body = {
                    ticket: tgsTicket.body.ticket,
                    file_id: fileid,
                    sig_ac: csTicket.body.sig_ac,
                    esk: esk,
                    auth: auth,
                    ts: now,
                    cpk: localStorage.getItem(id + '_pub')
                };
                var pemPrivateKey = localStorage.getItem(id + '_prv');
                var sig = new KJUR.crypto.Signature({alg: 'SHA256withRSA'});
                sig.init(pemPrivateKey, seed);
                sig.updateString(JSON.stringify(body));
                sig = sig.sign();

                var data = {
                    body: body,
                    signature: hextob64(sig)
                }

                $.ajax({
                    url : "${pageContext.request.contextPath}/analyze/process",
                    <%--url: '${pageContext.request.contextPath}/test/analysis',--%>
                    contentType : 'application/json',
                    type : 'post',
                    data : JSON.stringify(data),
                    success : function (resp) {
                        console.log(resp);
                        // if (resp.data) {
                        //     btn.attr("disabled", true);
                        // }
                    },
                    error : function (error) {
                        console.log(error);
                    }
                });

            });

            $(".analyze").click(function () {
                fileid = $(this).attr('value');
                $("#get-passphrase").modal();
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

            <h1>project id : ${project_id}</h1>
            <hr>
            <table class="table table-bordered" id="dataTable" width="100%" cellspacing="0">
                <thead>
                <tr>
                    <th>FILENAME</th>
                    <th>DESCRIPTION</th>
                    <th>ANALYZE</th>
                    <th>STATUS</th>
                </tr>
                </thead>
                <tfoot>

                </tfoot>
                <tbody>
                <c:forEach items="${projectFileDtoList}" var="projectFileDto" varStatus="status">
                    <tr>
                        <td><a href="${pageContext.request.contextPath}/code-editor/filedetail?file_id=${projectFileDto.fileId}">${projectFileDto.fileName}</a>
                        </td>
                        <td>${projectFileDto.description}</td>
                        <td>
                        <c:choose>
                            <c:when test="${projectFileDto.requestTime eq null}">
                                <button type="button" class="btn btn-primary analyze" value="${projectFileDto.fileId}">Analyze</button>
                            </c:when>
                            <c:when test="${projectFileDto.requestTime ne null}">
                                <button type="button" class="btn btn-primary" disabled>Analyze</button>
                            </c:when>
                        </c:choose>
                        </td>
                        <td>
                        <c:if test="${projectFileDto.requestTime ne null}">
                            <a href="${pageContext.request.contextPath}/project/analysis-status?project_id=${projectFileDto.projectId}&file_id=${projectFileDto.fileId}">status</a>
                        </c:if>
                        </td>
<%--                    <a href="./analysis/process"></a>--%>
                    </tr>
                </c:forEach>
                </tbody>
            </table>

            <!-- Breadcrumbs-->
            <ol class="breadcrumb">
                <li class="breadcrumb-item">
                    <form action="${pageContext.request.contextPath}/code-editor/fileupload?project_id=${project_id}" id="fileupload" name="fileupload" method="post"
                          enctype="multipart/form-data" style="width: 100%">
                        <input type="text" id="description" name="description" class="form-control"
                               placeholder="Enter File Description" style="width: 100%">
                        <hr>
                        <input id="filename" name="filename" placeholder="파일 선택" type="file">
                        <input type="submit" value="업로드">
                    </form>
                </li>
            </ol>


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
<div class="modal fade" id="success-import-key">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h4>Info</h4>
            </div>

            <div class="modal-body">Successfully imported PKI</div>

            <div class="modal-footer">
                <button class="btn btn-primary" type="button" data-dismiss="modal">OK</button>
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