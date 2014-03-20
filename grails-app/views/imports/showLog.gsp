<html>
<body>
	<%if (log) {%>
${new grails.converters.JSON(log).toString(true).encodeAsHTML()}
	<%} else {%>
	Log not found
	<%}%>
</body>
</html>