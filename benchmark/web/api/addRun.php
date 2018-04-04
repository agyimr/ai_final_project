<?php

require("api.class.php");

Api::contentTypeHeader();

if( !Api::Parameters($_POST, array("hash", "hostname", "level", "error", "duration", "actions", "time")) ) {
	Api::sendError(400, "Missing parameters");
}

$db = Api::openDatabase();

$stmt = $db->prepare("INSERT INTO runs VALUES (null, ?, ?, ?, ?, ?, ?, ?)");

$stmt->bind_param('ssssddd', $_POST["hostname"], $_POST["hash"], $_POST["level"], $_POST["error"], $_POST["duration"], $_POST["actions"], $_POST["time"]);

if( !$stmt->execute()) {
	Api::sendError(500, $db->error);	
}

$db->close();

Api::send("OK");

?>