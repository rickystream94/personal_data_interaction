<?php

$json = json_decode($_POST["data"],true);
$nickname = $json["user"][0]["nickname"];
$gamification = $json["user"][0]["Gamification"];
if ($gamification==1) {
	$versionStatus = "YESGamification";
}
else {
	$versionStatus = "NOGamification";
}
$date = date('Y_m_d');
$filename = "Phrasebook_Dump_".$nickname."_".$versionStatus."_".$date.".json";

//Save JSON to file (in same folder of the PHP file)
$file_handle = fopen($filename, 'w');
fwrite($file_handle, $_POST["data"]);
fclose($file_handle);

$response = "Successfully saved file ".$filename;
echo $response;

?>