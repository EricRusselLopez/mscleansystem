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
            $since = $_POST['since'];

            $stmt = $conn->prepare("
                SELECT time, item_name, quantity, threshold, branch, status, last_restock
                FROM inventory
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
            $item_name = $_POST['item_name'] ?? '';
            $quantity  = (int) ($_POST['quantity'] ?? 0);
            $threshold = (int) ($_POST['threshold'] ?? 0);
            $branch    = $_POST['branch'] ?? '';
            $time      = date("Y-m-d H:i:s");

            // Check if item_name + branch already exist
            $checkStmt = $conn->prepare("SELECT COUNT(*) FROM inventory WHERE item_name = ? AND branch = ?");
            $checkStmt->bind_param("ss", $item_name, $branch);
            $checkStmt->execute();
            $checkStmt->bind_result($count);
            $checkStmt->fetch();
            $checkStmt->close();

            if ($count > 0) {
                // Duplicate found - send false response
                echo json_encode(["response" => false, "error" => "Item already exists for this branch"]);
            } else {
                // No duplicate - proceed insert
                $status = ($quantity < $threshold) ? 'Low' : 'Normal';

                $stmt = $conn->prepare("INSERT INTO inventory (time, item_name, quantity, threshold, branch, status, last_restock) VALUES (?, ?, ?, ?, ?, ?, ?)");
                $stmt->bind_param("ssiisss", $time, $item_name, $quantity, $threshold, $branch, $status, $time);

                if ($stmt->execute()) {
                    echo json_encode(["response" => true]);
                } else {
                    echo json_encode(["response" => false, "error" => $stmt->error]);
                }

                $stmt->close();
            }

            $conn->close();
            exit;
        }


            $action = $_POST['action'] ?? '';

            if ($action === 'update') {
                $itemName  = trim($_POST['item_name']   ?? '');
                $quantity  = intval($_POST['quantity']  ?? 0);
                $threshold = intval($_POST['threshold'] ?? 0);
                $branch    = trim($_POST['branch']      ?? '');
                $time      = date("Y-m-d H:i:s");

                $status = ($quantity == 0) ? 'Out of Stock' : (($quantity < $threshold) ? 'Low' : 'Normal');

                $stmt = $conn->prepare("
                    UPDATE inventory
                    SET time          = ?,
                        quantity      = ?,
                        threshold     = ?,
                        status        = ?,
                        last_restock  = ?
                    WHERE item_name = ? AND branch = ?
                ");
                $stmt->bind_param(
                    "siissss",
                    $time, $quantity, $threshold, $status, $time,
                    $itemName, $branch
                );
                $ok = $stmt->execute();
                $stmt->close();

                echo json_encode(['response' => $ok]);
                $conn->close();
                exit;
            }

            else if ($action === 'use') {
                $itemName = trim($_POST['item_name'] ?? '');
                $branch   = trim($_POST['branch']    ?? '');
                $useQty   = intval($_POST['quantity'] ?? 0);
                $time     = date("Y-m-d H:i:s");

                $stmt = $conn->prepare("
                    SELECT quantity, threshold
                    FROM inventory
                    WHERE item_name = ? AND branch = ?
                    LIMIT 1
                ");
                $stmt->bind_param("ss", $itemName, $branch);
                $stmt->execute();
                $stmt->bind_result($curQty, $threshold);
                if (! $stmt->fetch()) {
                    echo json_encode(['response' => false, 'message' => 'Item not found']);
                    $stmt->close();
                    $conn->close();
                    exit;
                }
                $stmt->close();

                $newQty = $useQty;

                $status = ($newQty == 0) ? 'Out of Stock' : (($newQty < $threshold) ? 'Low' : 'Normal');



                $stmt = $conn->prepare("
                    UPDATE inventory
                    SET quantity = ?,
                        time     = ?,
                        status   = ?
                    WHERE item_name = ? AND branch = ?
                ");
                $stmt->bind_param("issss", $newQty, $time, $status, $itemName, $branch);
                $ok = $stmt->execute();
                $stmt->close();

                echo json_encode(['response' => $ok]);
                $conn->close();
                exit;
            }
            else if($action === "remove") {
                $itemName = trim($_POST['item_name'] ?? '');
                $branch   = trim($_POST['branch']    ?? '');
                $stmt = $conn->prepare("DELETE FROM inventory WHERE item_name = ? AND branch = ?");
                $stmt->bind_param("ss", $itemName, $branch);
                if($stmt->execute()) {
                    echo json_encode(['response' => true]);
                }
            }

            echo json_encode(['response' => false, 'message' => 'Unknown action']);
            $conn->close();
        }

?>