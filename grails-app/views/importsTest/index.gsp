<html>
<body>
Upload Form: <br />
<g:uploadForm action="process" controller="imports">
	<select name="entityName">
		<option value="testImportItem1">testImportItem1</option>
		<option value="testImportItem2">testImportItem2</option>
		<option value="testImportItem3">testImportItem3</option>
		<option value="testImportItem4">testImportItem4</option>
	</select>
	<br/>
    <input type="file" name="import" />
    <input type="hidden" name="format" value="html" />
	<br/>
    <input type="submit" />
</g:uploadForm>
</body>
</html>