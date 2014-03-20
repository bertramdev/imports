<html>
<body>
<h2>processing complete</h2>
<div>success: ${success}</div>
<div>status: ${status}</div>
<div>message: ${msg}</div>
<div>log: <g:link action="showLog" id="${importLogId}" params="${[format:'html']}">Click to view</g:link></div>
<div>errorRows: <g:link action="showErrorRows" id="${importLogId}" params="${[format:'csv']}">Click to download</g:link></div>
<div>cancel: <g:link action="cancel" id="${importLogId}" params="${[format:'html']}">Click to cancel</g:link></div>
<div>trace: ${trace.encodeAsHTML()}</div>
</body>
</html>