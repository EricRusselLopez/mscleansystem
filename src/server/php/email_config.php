<?php
if (!defined('ACCESS_ALLOWED')) {
    die("Restricted Access");
}

/*
|--------------------------------------------------------------------------
| Email Configuration for PHPMailer
|--------------------------------------------------------------------------
| This configuration file contains the SMTP credentials used to send emails.
| 
|
| ⚠️ IMPORTANT:
| - Replace the email and app password below with your own.
| - You MUST enable 2-step verification on your Gmail account and create 
|   an "App Password" for this to work (regular Gmail passwords won't work).
| - Do NOT share or commit this file to public repositories.
|
| Example setup: https://support.google.com/accounts/answer/185833
*/

return [
    'username' => '',    // Your email here
    'password' => ''     // Your App Password here
];
