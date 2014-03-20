<html>
<head>
<!-- Latest compiled and minified CSS -->
<link rel="stylesheet" href="http://netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.min.css">

<!-- Optional theme -->
<link rel="stylesheet" href="http://netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap-theme.min.css">
<script src="http://code.jquery.com/jquery-1.11.0.min.js"></script>
<script src="http://code.jquery.com/jquery-migrate-1.2.1.min.js"></script>
<!-- Latest compiled and minified JavaScript -->
<script src="http://netdna.bootstrapcdn.com/bootstrap/3.1.1/js/bootstrap.min.js"></script>
</head>
<body>
	<div class="container">
<h1>Processing Cancellation Requested</h1>
	<div class="row">
<div class="col-sm-2 control-label">Success:</div><div class="col-sm-4">${success}</div>
	</div>

	<div class="row">
<div class="col-sm-2 control-label">Log:</div><div class="col-sm-4"><g:link action="showLog" id="${importLogId}" params="${[format:'html']}">Click to view</g:link></div>
	</div>

	<div class="row">
<div class="col-sm-2 control-label">Error Rows:</div><div class="col-sm-4"><g:link action="showErrorRows" id="${importLogId}" params="${[format:'csv']}">Click to download</g:link></div>
	</div>
	</div>
</body>
</html>