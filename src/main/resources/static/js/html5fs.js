var fn = null;
var data = null;

function errorHandler(error) {
    var message = '';
    console.log('Error');
    /*
    switch (error.message) {
      case FileError.SECURITY_ERR:
        message = 'Security Error';
        break;
      case FileError.NOT_FOUND_ERR:
        message = 'Not Found Error';
        break;
      case FileError.QUOTA_EXCEEDED_ERR:
        message = 'Quota Exceeded Error';
        break;
      case FileError.INVALID_MODIFICATION_ERR:
        message = 'Invalid Modification Error';
        break;
      case FileError.INVALID_STATE_ERR:
        message = 'Invalid State Error';
        break;
      default:
        message = 'Unknown Error';
        break;
    }
    console.log(message);
    */
}

function onInitFs(fs) {
    console.log("Opened file system: " + fs.name);
    console.log(fs);
}

function createLocalFile(fs) {
    fs.root.getFile(fn, {create: true, exclusive: true},
        function (fe) {
            console.log(fe.fullPath);
        }, errorHandler
    );
}

function writeLocalFile(fs) {
    fs.root.getFile(fn, {create: true, exclusive: false},
        function (fe) {
            console.log(fe.fullPath);
            fe.createWriter(function (fileWriter) {
                console.log('succecc creating fw');
                fileWriter.onwriteend = function (e) {
                    console.log("success writing file");
                };
                fileWriter.onerror = function (e) {
                    console.log(e.toString());
                };
                var blob = new Blob(['a', 'b'], {type: 'text/plain'}, 'utf-8');
                fileWriter.write(blob);
            }, errorHandler);
        }, errorHandler);
}

function readLocalFile(fs) {
    fs.root.getFile(fn, {}, function (fe) {
        console.log('success openening file');
        fe.file(function (f) {
            var reader = new FileReader();
            reader.onloadend = function (e) {
                console.log(this.result);
            };
            reader.readAsText(f, 'utf-8');
        }, errorHandler);
    }, errorHandler);
}

function removeLocalFile(fs) {
    fs.root.getFile(fn, {create: false, exclusive: false},
        function (fe) {
            console.log(fe.fullPath);
            fe.remove(function () {
                console.log('success removing file');
            }, errorHandler);
        }, errorHandler
    );
}

function handleLocalFile(handler, fname) {
    fn = fname;
    navigator.webkitPersistentStorage.requestQuota(
        1024 * 1024 * 10, function (grantedBytes) {
            window.webkitRequestFileSystem(PERSISTENT, grantedBytes, handler, errorHandler);

        }, function (e) {
            console.log('Error', e);
        }
    );
}