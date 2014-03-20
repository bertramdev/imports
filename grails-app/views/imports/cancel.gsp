<html>
<body>
<h2>processing cancellation requested</h2>
<div>success: ${success}</div>
<div>log: <g:link action="showLog" id="${importLogId}" params="${[format:'html']}">Click to view</g:link></div>
<div>errorRows: <g:link action="showErrorRows" id="${importLogId}" params="${[format:'csv']}">Click to download</g:link></div>
</body>
</html>