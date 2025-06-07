<?php
header("Content-Type: application/json");
define('ACCESS_ALLOWED', true);
require_once "../php/conn.php";
$mail_config = require_once "../php/email_config.php";

require 'PHPMailer-master/src/PHPMailer.php';
require 'PHPMailer-master/src/SMTP.php';
require 'PHPMailer-master/src/Exception.php';

use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;

    $token = ";;mslaundryshop2025;;";

    if($_SERVER['REQUEST_METHOD'] !== 'POST' || !isset($_POST['client_fetch_token_request']) || $_POST['client_fetch_token_request'] !== $token) {
        echo json_encode(["response" => false]);
        exit;
    } else {
      if ($_POST['action'] === "get") {
            $since = $_POST['since'] ?? '1970-01-01 00:00:00';

            $stmt = $conn->prepare("
                SELECT transaction_id, time, customer_name, contact, servicetype, total_amount, branch, status
                FROM reports
                WHERE time > ?
                ORDER BY time ASC
            ");
            $stmt->bind_param("s", $since);
            $stmt->execute();
            $result = $stmt->get_result();

            $data = [];
            while ($row = $result->fetch_assoc()) {
                $data[] = $row;
            }

            echo json_encode($data);

            $stmt->close();
            $conn->close();
        } else if ($_POST['action'] === "add") {
            $customer = $_POST['customer'] ?? '';
            $contact = $_POST['contact'] ?? '';
            $serviceType = $_POST['servicetype'] ?? '';
            $amount = $_POST['amount'] ?? '';
            $status = $_POST['status'] ?? '';
            $branch = $_POST['branch'] ?? '';

            $transaction_id = 'MCL-' . str_pad(mt_rand(0, 999999), 6, '0', STR_PAD_LEFT);
            $time = date("Y-m-d H:i:s");

            $stmt = $conn->prepare("INSERT INTO reports (transaction_id, time, contact, customer_name, servicetype, total_amount, branch, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            $stmt->bind_param("ssssssss", $transaction_id, $time, $customer, $contact, $serviceType, $amount, $branch, $status);
            $stmt->execute();
            $stmt->close();

            $message = "Report: $customer (₱$amount, $status)";
            $stmt = $conn->prepare("INSERT INTO notifications (time, message) VALUES (?, ?)");
            $stmt->bind_param("ss", $time, $message);
            $stmt->execute();
            $stmt->close();

            if (strtolower($status) === "paid") {
                $month_name = date('F', strtotime($time));
                $month_number = date('n', strtotime($time));
                $year = date('Y', strtotime($time));
                $salesAmount = floatval($amount);

                $sql = "INSERT INTO statistics (time, month_name, month_number, year, branch, total_sales) 
                        VALUES (?, ?, ?, ?, ?, ?) 
                        ON DUPLICATE KEY UPDATE total_sales = total_sales + VALUES(total_sales)";
                $stmt = $conn->prepare($sql);
                $stmt->bind_param("ssiisd", $time, $month_name, $month_number, $year, $branch, $salesAmount);
                $stmt->execute();
                $stmt->close();
            }

            if(strtolower($status) === "pickup" && strtolower(trim($contact)) !== 'n/a' && filter_var($contact, FILTER_VALIDATE_EMAIL)) {
                $mail = new PHPMailer(true);
                    $mail->isSMTP();
                    $mail->Host       = 'smtp.gmail.com';
                    $mail->SMTPAuth   = true;
                    $mail->Username   = $mail_config['username'];
                    $mail->Password   = $mail_config['password'];
                    $mail->SMTPSecure = 'tls';
                    $mail->Port       = 587;

                    $mail->setFrom($mail_config['username'], 'Mrs. Clean Laundry Ease');
                    $mail->addAddress($contact, "$customer");
                    $mail->isHTML(true);
                    $mail->Subject = 'Your Laundry Is Ready for Pickup - Mrs. Clean Laundry Ease';
                    $mail->Body    = '
                    <div style="font-family: Arial, sans-serif; padding:20px; background:#f9f9f9; border-radius:8px;">
                    <div style="text-align:center;">
                        <img src="https://i.imgur.com/xmdt55d.png" alt="Logo"
                            style="max-width:200px; margin-bottom:20px;">
                        <h2 style="color:#28a745;">Ready for Pickup!</h2>
                    </div>
                    <p>Dear <b>' . htmlspecialchars("$customer") . '</b>,</p>
                    <p>Great news—your laundry order is now clean and ready for pickup at our <b>' 
                        . htmlspecialchars($branch) . '</b> branch.</p>
                    <p><strong>Order Details:</strong><br>
                        • Transaction ID: ' . htmlspecialchars($transaction_id) . '<br>
                        • Service Type: ' . htmlspecialchars($serviceType) . '<br>
                        • Total Amount: ₱' . $amount . '</p>
                    <p>Your laundry is ready for pickup now. Please come by at your earliest convenience and remember to bring your transaction ID or this email.</p>
                    <p style="margin-top:20px;">
                        If you have any questions, just reply to this email or call us at <b>(02) 1234-5678</b>.
                    </p>
                    <hr>
                    <p style="font-size:12px; color:#777;">
                        &copy; ' . date("Y") . ' Mrs. Clean Laundry Ease. All rights reserved.
                    </p>
                    </div>';

                    $mail->send();

            }

            echo json_encode(["response" => true, "ti" => $transaction_id]);
            $conn->close();
            exit;
        } else if ($_POST['action'] === "update") {
                $newstatus = $_POST['newstatus'] ?? '';
                $ti = $_POST['ti'] ?? '';
                $customer = $_POST['customer'] ?? '';
                $contact = $_POST['contact'] ?? '';
                $amount = $_POST['amount'] ?? '';
                $branch = $_POST['branch'] ?? '';

                $time = date("Y-m-d H:i:s");

                $stmt = $conn->prepare("SELECT status, total_amount, servicetype, branch, time FROM reports WHERE transaction_id = ?");
                $stmt->bind_param("s", $ti);
                $stmt->execute();
                $result = $stmt->get_result();
                $oldData = $result->fetch_assoc();
                $stmt->close();

                if (!$oldData) {
                    echo json_encode([
                        "response" => false,
                        "error" => "No matching record found."
                    ]);
                    $conn->close();
                    exit;
                }

                $oldstatus = $oldData['status'];
                $oldamount = floatval($oldData['total_amount']);
                $serviceType  = $oldData['servicetype'];
                $branch = $oldData['branch'];
                $oldTime = $oldData['time'];

                $stmt = $conn->prepare("UPDATE reports SET status = ?, time = ? WHERE transaction_id = ?");
                $stmt->bind_param("sss", $newstatus, $time, $ti);

                if ($stmt->execute() && $stmt->affected_rows > 0) {
                    $stmt->close();

                    $oldPaid = strtolower($oldstatus) === "paid";
                    $newPaid = strtolower($newstatus) === "paid";

                    if ($oldPaid !== $newPaid) {
                        $month_name = date('F', strtotime($oldTime));
                        $month_number = date('n', strtotime($oldTime));
                        $year = date('Y', strtotime($oldTime));
                        $salesAmount = $newPaid ? $oldamount : -$oldamount;

                        $sql = "INSERT INTO statistics (time, month_name, month_number, year, branch, total_sales) 
                                VALUES (?, ?, ?, ?, ?, ?) 
                                ON DUPLICATE KEY UPDATE total_sales = total_sales + VALUES(total_sales)";
                        $stmt2 = $conn->prepare($sql);
                        $stmt2->bind_param("ssiisd", $time, $month_name, $month_number, $year, $branch, $salesAmount);

                        $stmt2->execute();
                        $stmt2->close();
                    }
                    if (strtolower($newstatus) === "pickup" && $oldstatus !== "Pickup" && strtolower(trim($contact)) !== 'n/a' && filter_var($contact, FILTER_VALIDATE_EMAIL)) {
                        $mail = new PHPMailer(true);
                        try {
                            $mail->isSMTP();
                            $mail->Host       = 'smtp.gmail.com';
                            $mail->SMTPAuth   = true;
                            $mail->Username   = $mail_config['username'];
                            $mail->Password   = $mail_config['password'];
                            $mail->SMTPSecure = 'tls';
                            $mail->Port       = 587;

                            $mail->setFrom($mail_config['username'], 'Ms. Clean Laundry Ease');
                            $mail->addAddress($contact, $customer);
                            $mail->isHTML(true);
                            $mail->Subject = 'Your Laundry Is Ready for Pickup - Mrs. Clean Laundry Ease';
                            $mail->Body    = '
                            <div style="font-family: Arial, sans-serif; padding:20px; background:#f9f9f9; border-radius:8px;">
                            <div style="text-align:center;">
                                <img src="https://i.imgur.com/xmdt55d.png" alt="Logo"
                                    style="max-width:200px; margin-bottom:20px;">
                                <h2 style="color:#28a745;">Ready for Pickup!</h2>
                            </div>
                            <p>Dear <b>' . htmlspecialchars($customer) . '</b>,</p>
                            <p>Great news—your laundry order is now clean and ready for pickup at our <b>' 
                                . htmlspecialchars($branch) . '</b> branch.</p>
                            <p><strong>Order Details:</strong><br>
                                • Transaction ID: ' . htmlspecialchars($ti) . '<br>
                                • Service Type: '  . htmlspecialchars($serviceType) . '<br>
                                • Total Amount: ₱' . number_format($oldamount, 2) . '</p>
                            <p>Your laundry is ready for pickup now. Please come by at your earliest convenience and remember to bring your transaction ID or this email.</p>
                            <p style="margin-top:20px;">
                                If you have any questions, just reply to this email or call us at <b>(02) 1234-5678</b>.
                            </p>
                            <hr>
                            <p style="font-size:12px; color:#777;">
                                &copy; ' . date("Y") . ' Mrs. Clean Laundry Ease. All rights reserved.
                            </p>
                            </div>';

                            $mail->send();
                        } catch (Exception $e) {
                            echo $mail->ErrorInfo;
                        }
                    }

                    echo json_encode(["response" => true, "ti" => $ti]);
                } else {
                    echo json_encode([
                        "response" => false,
                        "error" => $stmt->error ?: "No matching record found or no change."
                    ]);
                    $stmt->close();
                }
            }
    }

?>