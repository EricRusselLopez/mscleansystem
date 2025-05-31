<?php
header("Content-Type: application/json");
define('ACCESS_ALLOWED', true);
require_once "../php/conn.php";

    $token = ";;mslaundryshop2025;;";

    if($_SERVER['REQUEST_METHOD'] !== 'POST' || !isset($_POST['client_fetch_token_request']) || $_POST['client_fetch_token_request'] !== $token) {
        echo json_encode(["response" => false]);
        exit;
    } else {
      if ($_POST['action'] === "get") {
            $since = $_POST['since'] ?? '1970-01-01 00:00:00';

            $stmt = $conn->prepare("
                SELECT transaction_id, time, customer_name, servicetype, total_amount, branch, status
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
        $serviceType = $_POST['servicetype'] ?? '';
        $amount = $_POST['amount'] ?? '';
        $status = $_POST['status'] ?? '';
        $branch = $_POST['branch'] ?? '';

        $transaction_id = 'MCL-' . str_pad(mt_rand(0, 999999), 6, '0', STR_PAD_LEFT);
        $time = date("Y-m-d H:i:s");

        $stmt = $conn->prepare("INSERT INTO reports (transaction_id, time, customer_name, servicetype, total_amount, branch, status) VALUES (?, ?, ?, ?, ?, ?, ?)");
        $stmt->bind_param("sssssss", $transaction_id, $time, $customer, $serviceType, $amount, $branch, $status);
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

        echo json_encode(["response" => true]);
        $conn->close();
        exit;
            } else if ($_POST['action'] === "update") {
            $newstatus = $_POST['newstatus'] ?? '';
                $ti = $_POST['ti'] ?? '';
                $time = date("Y-m-d H:i:s");

                $stmt = $conn->prepare("SELECT status, total_amount, branch, time FROM reports WHERE transaction_id = ?");
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

                    echo json_encode(["response" => true]);
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