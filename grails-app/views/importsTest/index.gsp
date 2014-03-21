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
<body style="margin:20px">
<h1>Test Imports</h1>
<g:uploadForm class="form-horizontal" action="process" controller="imports"  role="form">
  <div class="form-group">
    <label for="entityName"  class="col-sm-2 control-label">Entity Name:</label>
    <div class="col-sm-4">
	<select class="form-control" name="entityName">
		<option value="testImportItem1">testImportItem1</option>
		<option value="testImportItem2">testImportItem2</option>
		<option value="testImportItem3">testImportItem3</option>
		<option value="testImportItem4">testImportItem4</option>
	</select>
	</div>
  </div>
  <div class="form-group">
    <label for="entityName"  class="col-sm-2 control-label">Data File:</label>
    <div class="col-sm-4">
    <input class="form-control" type="file" name="import" />
    <input type="hidden" name="format" value="html" />
	</div>
  </div>
  <div class="form-group">
    <label for="entityName"  class="col-sm-2 control-label">Email Address:</label>
    <div class="col-sm-4">
    <input class="form-control" type="email" name="email" />
	</div>
  </div>
  <div class="form-group">
    <div class="col-sm-4 col-sm-offset-2">
    <input class="form-control" type="submit" class="btn btn-primary"/>
	</div>
  </div>
</g:uploadForm>
</body>
</html>