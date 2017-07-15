<?php

$nickname = $_POST["nickname"];
$date = date('Y_m_d');

//Reads last value of gamification that has been written, flips the value and overwrites to same file.
$binary_check_file = "binary.txt";
$previous_binary = file_get_contents($binary_check_file);
$current_binary = $previous_binary=='1' ? '0' : '1';
file_put_contents($binary_check_file, $current_binary);

//Log install from current user to a new text file
$install_log_file = "install_log.txt";
$data = array($nickname,$date,$current_binary);
$data_string = implode(",", $data); //Create CSV line
file_put_contents($install_log_file, $data_string."\r\n", FILE_APPEND);

echo $current_binary;
?>