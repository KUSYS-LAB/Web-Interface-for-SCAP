<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:eval expression="@environment.getProperty('cs.domain')" var="cs_domain"/>
<spring:eval expression="@environment.getProperty('tgs.domain')" var="tgs_domain"/>
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
    <script src="${pageContext.request.contextPath}/vendor/jquery-easing/jquery.easing.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jszip.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jsrsasign-all-min.js"></script>

    <!-- Page level plugin JavaScript-->
    <script src="${pageContext.request.contextPath}/vendor/datatables/jquery.dataTables.js"></script>

    <!-- Custom scripts for all pages-->
    <script src="${pageContext.request.contextPath}/js/sb-admin.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.cookie.js"></script>

    <style>
        #zip_cotents {
            width: 100%;
            height: 30vh;
            overflow: auto;
        }
    </style>

    <link rel="stylesheet"
          href="https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.52.2/codemirror.min.css">
    </link>
    <link rel="stylesheet"
          href="https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.52.2/theme/monokai.min.css">

    <script type="text/javascript"
            src="https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.52.2/codemirror.min.js">
    </script>

    <script type="text/javascript"
            src="https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.52.2/mode/r/r.min.js">
    </script>

    <script type="text/javascript"
            src="https://cdnjs.cloudflare.com/ajax/libs/jshint/2.11.0/jshint.js">
    </script>

    <script type="text/javascript" src="${pageContext.request.contextPath}/assets/js/jszip.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/assets/js/jszip-utils.min.js"></script>

    <!--[if IE]
        <script type="text/javascript" src="/jszip/jszip-utils.ie.min.js"></script>
    <![endif]-->

    <script type="text/javascript" src="http://danml.com/js/download.js"></script>


    <!-- 이전에 설명한 글의 유용한(?) 함수를 묶어둔 파일 -->
    <script type="text/javascript" src="${pageContext.request.contextPath}/assets/js/zip_common.js"></script>

    <script type="text/javascript">
        var setCsFlag = false;
        // var setTgsFlag = false;
        var _url = '${pageContext.request.contextPath}/code-editor/zipfile';
        var fileid_int = ${fileid};
        var fileid = String(fileid_int);
        var zipInst = null;
        var read_filename = null;
        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function () {
            if (this.readyState == 4 && this.status == 200) {
                var _data = this.response;
                var _blob = new Blob([_data], {type: 'application/x-zip-compressed'});
                var file = _blob;
                console.log(file);
                if (file) {
                    var reader = new FileReader();
                    reader.onload = function (evt) {
                        // 파일내용을 읽으면 이 곳에 온다.
                        // 파일내용은 evt.target.result에 있게 되고 이를 JSZip에 넘겨 구조를 파악하게 한다.
                        JSZip.loadAsync(evt.target.result).then(
                            // Zip파일내용이 처리되면 화면에 그리는 코드가 수행될 것이다.
                            function (zip) {
                                zipInst = zip;
                                showfilelist(zipInst, 'zip_cotents');
                            },
                        )
                    }
                    reader.onerror = function (evt) {
                        alert('파일을 못 열었어요?..');
                    }
                    reader.readAsArrayBuffer(file);
                } else {
                    alert('파일이 없는 걸로..');
                }
            }
        };

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

        function showfilelist(zip, outId) {
            var text = '<table class="table">';

            text += '<tr>';
            text += '<td>relativePath + name</td>';
            text += '<td>dir/file</td>';
            text += '</tr>';

            // Zip파일내의 모든 파일을 순회한다.(압축을 푼것이 아니고 zip파일내의 파일목록)
            zip.forEach(
                function (relativePath, entry) {
                    text += '<tr>';
                    text += '<td>';
                    text += (entry.dir ? '' + entry.name + '' : '<span onclick="clickSpan(\'' + entry.name + '\' )">' + entry.name + '</span>');
                    text += '</td>';
                    text += '<td>' + (entry.dir ? 'dir' : 'file') + '</td>';
                    text += '</tr>';
                }
            );
            text += '</table>';
            document.getElementById("zip_cotents").innerHTML = text;
        }

        function clickSpan(filename) {
            zipInst;
            read_filename = filename;
            zipInst.files[filename].async('string').then(function (fileData) {
                console.log(fileData); // These are your file contents
                $("#area").empty();
                let textarea = $('<textarea class = "myTextarea" id = "myTextarea" style="height: 100%; width: 100%; display: none"></textarea>')
                $('#area').prepend(textarea);
                $(".myTextarea").append(fileData);
                var editor = CodeMirror.fromTextArea(myTextarea, {
                    tabSize: 2,
                    value: '',
                    mode: 'r',
                    lineNumbers: true,
                });
                editor.setSize("100%", "100%");
                $("#save").click(function () {
                    var value = editor.getValue();
                    console.log(value);
                    zipInst.file(filename, value);


                })
            });
        }

        function getTicket(blob, tgsPublicKey) {
            if (setCsFlag) {
                var url = '${tgs_domain}/get-ticket';
                if (url === "") return;

                var id = "${USER.cname }";
                var seed = $("#passphrase").val();
                var from = new Date();
                var to = new Date();

                to.setMinutes(from.getMinutes() + 30);
                var param_cs = JSON.parse($.cookie(id + '_cs'));
                var ts = from.toString2(true);

                var esk = hextob64(KJUR.crypto.Cipher.encrypt(JSON.stringify({
                        sk: param_cs.body.esk.sk,
                        iv: param_cs.body.esk.iv
                    }), tgsPublicKey, 'RSAOAEP'));
                var auth = hextob64(KJUR.crypto.Cipher.encrypt(
                    JSON.stringify({
                        cname: id,
                        ts: from.toString2(true)
                    }), tgsPublicKey, 'RSAOAEP'));

                var body = {
                    ticket: param_cs.body.ticket,
                    sig_ac: param_cs.body.sig_ac,
                    id_s: "id_ss",
                    esk: esk,  // need to be encrypted by CS witthe pk+ of TGS.
                    auth: auth,
                    time: {from: from.toString2(), to: to.toString2()},
                    ts: from.toString2(true),
                    cpk: localStorage.getItem(id + "_pub")
                };

                // var pemPublicKey = localStorage.getItem(id + "_pub");
                var pemPrivateKey = localStorage.getItem(id + "_prv");
                // var bs64Cert = localStorage.getItem(id + "_cert");
                var sig = new KJUR.crypto.Signature({alg: "SHA256withRSA"});
                sig.init(pemPrivateKey, seed);
                sig.updateString(JSON.stringify(body));
                sig = sig.sign();

                var data = {
                    body: body,
                    signature: hextob64(sig)
                };

                // console.log(data);

                $.ajax({
                    url: url,
                    type: 'post',
                    data: JSON.stringify(data),
                    dataType: 'json',
                    contentType: 'application/json',
                    success: function (resp) {
                        setTgsFlag = true;
                        $.cookie(id + '_tgs', JSON.stringify(resp), {path: '${pageContext.request.contextPath}/'});
                        modifyzip(blob);
                    },
                    error: function (err) {
                        console.log(err);
                    }
                });
            }
        }

        function modifyzip(blob) {
            if (setTgsFlag) {
                var formData = new FormData();
                formData.append("file", blob);
                formData.append("fileid", String(${fileid}));

                $.ajax({
                    type: "POST",
                    url: "${pageContext.request.contextPath}/code-editor/modifyzip",
                    data: formData,
                    contentType: false,
                    processData: false,
                    success: function (resp) {
                        setCsFlag = false;
                        setTgsFlag = false;
                    },
                    error: function (xhr, status, error) {
                        alert(error);
                    }
                });
            }
        }

        function leadingZeros(n, digits) {
            var zero = '';
            n = n.toString();

            if (n.length < digits) {
                for (i = 0; i < digits - n.length; i++)
                    zero += '0';
            }
            return zero + n;
        }

        function getCodeSign(blob, csPublicKey, tgsPublicKey) {
            var id = "${USER.cname }";
            var seed = $("#passphrase").val();
            var from = new Date();
            var to = new Date();
            var param_as = JSON.parse($.cookie('token'));
            var ts = from.toString2(true);

            to.setMinutes(from.getMinutes() + 30);

            var esk = hextob64(KJUR.crypto.Cipher.encrypt(JSON.stringify({
                sk: param_as.body.sk,
                iv: param_as.body.iv
            }), csPublicKey, 'RSAOAEP'));
            var auth = hextob64(KJUR.crypto.Cipher.encrypt(JSON.stringify({
                cname: id,
                timestamp: from.toString2(true)
            }), csPublicKey, 'RSAOAEP'));

            console.log(blob);

            var body = {
                ticket: param_as.body.ticket,
                id_s: "id_tgs",
                esk: esk,
                auth: auth,
                time: {from: from.toString2(), to: to.toString2()},
                ts: from.toString2(true),
                cpk: localStorage.getItem(id + "_pub")
            };

            // var pemPublicKey = localStorage.getItem(id + "_pub");
            var pemPrivateKey = localStorage.getItem(id + "_prv");
            // var bs64Cert = localStorage.getItem(id + "_cert");
            var sig = new KJUR.crypto.Signature({alg: "SHA256withRSA"});
            sig.init(pemPrivateKey, seed);
            sig.updateString(JSON.stringify(body));
            sig = sig.sign();

            var data = {
                body: body,
                signature: hextob64(sig)
            };
            var fd = new FormData();
            fd.append('ac', blob);
            fd.append('data', new Blob([JSON.stringify(data)], {type: 'application/json'}));

            $.ajax({
                url: '${cs_domain}/get-sign',
                data: fd,
                processData: false,
                contentType: false,
                type: 'post',
                dataType: 'json',
                success: function (data) {
                    $.cookie(id + '_cs', JSON.stringify(data), {path: '${pageContext.request.contextPath}/'});
                    setCsFlag = true;
                    getTicket(blob, tgsPublicKey);
                },
                error: function (error) {
                    console.log(error);
                }
            });
        }

        xhr.open('POST', _url);
        xhr.responseType = 'blob';
        xhr.setRequestHeader('Content-type', 'application/json');
        xhr.send(fileid);

        $(document).ready(function () {
            var csPublicKey = null;
            var tgsPublicKey = null;

            $.ajax({
                url: '${cs_domain}/get-cert',
                type: 'get',
                dataType: 'json',
                success: (json) => {
                    // the signature should be verified.
                    var signature = json.signature;
                    var body = json.body;
                    var certificate = body.certificate;
                    csPublicKey = KEYUTIL.getKey(b64nltohex(certificate), null, 'pkcs8pub');
                },
                error: (err) => {
                    console.log(err);
                }
            });

            $.ajax({
                url: '${tgs_domain}/get-cert',
                type: 'get',
                dataType: 'json',
                success: (json) => {
                    // the signature should be verified.
                    var signature = json.signature;
                    var body = json.body;
                    var certificate = body.certificate;
                    tgsPublicKey = KEYUTIL.getKey(b64nltohex(certificate), null, 'pkcs8pub');
                },
                error: (err) => {
                    console.log(err);
                }
            });

            $("#submit").click(function () {
                //삽입
                $("#get-passphrase").modal();
            });

            //cs에서 사인 받아오기
            $("#ok-passphrase").click(function () {
                var zip = zipInst;
                // var formData = new FormData();
                // formData.append("zip",zip);
                zip.generateAsync({type: "blob"}).then(function (blob) {
                    getCodeSign(blob, csPublicKey, tgsPublicKey);
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
        <li class="nav-item ">
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
    </ul>

    <div id="content-wrapper">

        <div class="container-fluid" style="height: 100%; position:relative; padding: 0px;">

            <h1> FileDetail : ${filename}</h1>

            <div class="container-fluid">
                <button id="submit" class="btn btn-success">submit</button>
                <button id="save" class="btn btn-primary">save</button>
            </div>
            <hr>
            <div id="zip_cotents"></div>

            <div style=" position:absolute; width: 100%; height :50%; bottom: 0px; padding: 0px;">
                <div style="height: 100%; width: 100%; bottom: 0px; padding: 0px;" id="area"></div>
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