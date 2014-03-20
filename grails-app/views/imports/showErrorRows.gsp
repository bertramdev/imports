<html>
<body>
${new grails.converters.JSON(errorRows).toString(true).encodeAsHTML()}
</body>
</html>