<?php

require("mysql.config.php");

class Api {

	static function contentTypeHeader() {
		header("Content-type: text/plain");
	}

	static function Parameters($where, $parametersName) {
		
		foreach($parametersName as $parameterName) {
			if( !array_key_exists($parameterName, $where) ) {
				return false;
			}
		}
		return true;
		
	}
	
	static function send($data) {
		echo json_encode($data, JSON_PRETTY_PRINT);
	}
	
	static function sendError($code, $description) {
		
		Api::send(
			array(
				'errorCode' => $code,
				'errorDescription' => $description
			)
		);
		exit;
	}
	
	static function openDatabase() {
		
		$db = new mysqli(MYSQL_HOST, MYSQL_USER, MYSQL_PASS, MYSQL_BASE);
		
		if ($db->connect_errno) {
			
			Api::sendError(10, "Database connection failed : " . $db->connect_error);
		}
		
		return $db;
	}	
	
	
} 

?>