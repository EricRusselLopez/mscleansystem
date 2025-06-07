<?php 
header("Content-Type: application/json");
define('ACCESS_ALLOWED', true);
require_once "../php/conn.php";
$token = ";;mslaundryshop2025;;";
if ($_SERVER['REQUEST_METHOD'] !== 'POST'
    || !isset($_POST['client_fetch_token_request'])
    || $_POST['client_fetch_token_request'] !== $token
) {
    exit;
}

// owner credentials
$ownerFirstname = "Owner";
$ownerLastname  = "Owner";
$ownerEmail     = "russelmcpe0320@gmail.com";
$ownerPassword  = "123";
$ownerRole      = "owner";
$ownerBranch    = "all";

$hashedPassword = password_hash($ownerPassword, PASSWORD_DEFAULT);

$check = $conn->prepare(
    "SELECT 1 
       FROM user_credentials 
      WHERE email = ? 
      LIMIT 1"
);
$check->bind_param("s", $ownerEmail);
$check->execute();
$check->store_result();

if ($check->num_rows === 0) {
    // 2) insert if missing
    $insert = $conn->prepare(
        "INSERT INTO user_credentials
            (firstname, lastname, email, password, role, branch)
         VALUES (?, ?, ?, ?, ?, ?)"
    );
    $insert->bind_param(
        "ssssss",
        $ownerFirstname,
        $ownerLastname,
        $ownerEmail,
        $hashedPassword,
        $ownerRole,
        $ownerBranch
    );

    if ($insert->execute()) {
        echo json_encode(["response" => true, "message" => "Owner account created."]);
    } else {
        echo json_encode(["response" => false, "error" => $insert->error]);
    }

    $insert->close();
}

$check->close();
$conn->close();
