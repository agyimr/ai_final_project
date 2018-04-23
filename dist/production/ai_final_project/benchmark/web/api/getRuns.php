<?php

require("api.class.php");

Api::contentTypeHeader();

$db = Api::openDatabase();

$result = $db->query("SELECT * FROM runs");

$results = [];

while ($row = $result->fetch_assoc()) {
	$results[] = array(
		"hash" => $row["hash"],
		"hostname" => $row["hostname"],
		"level" => $row["level"],
		"error" => $row["error"],
		"duration" => intval($row["duration"]),
		"actions" => intval($row["actions"]),
		"time" => intval($row["time"])
	);
}

$db->close();

Api::send($results);

?>