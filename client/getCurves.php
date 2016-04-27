<?php
	$dir = 'timecurves/'. $_GET['dir'] . "/";
	$result = '[';
	$files = scandir($dir);
	for($i=0 ; $i<sizeof($files); $i++){
		if(strpos($files[$i], '.curve') !== false){
			$result = $result . '"'. $files[$i] . '",';
		}
	}
	$result = rtrim($result,',');

	$result = $result . ']';

	echo $result;
?>