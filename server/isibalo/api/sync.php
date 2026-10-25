<?php
// Upload to: public_html/isibalo/api/sync.php  ->  https://hbtechinc.co.za/isibalo/api/sync.php
// Edit the 5 constants below. Tables are created automatically.
const DB_HOST = 'localhost';
const DB_NAME = 'CHANGE_ME';
const DB_USER = 'CHANGE_ME';
const DB_PASS = 'CHANGE_ME';
const API_KEY = 'CHANGE_ME';   // must match SYNC_API_KEY in app/build.gradle.kts

header('Content-Type: application/json');
function fail(int $code, string $msg) { http_response_code($code); echo json_encode(['ok' => false, 'error' => $msg]); exit; }

if ($_SERVER['REQUEST_METHOD'] !== 'POST') fail(405, 'POST only');
if (!hash_equals(API_KEY, $_SERVER['HTTP_X_API_KEY'] ?? '')) fail(401, 'bad key');

$in = json_decode(file_get_contents('php://input'), true);
$device = substr((string)($in['device'] ?? ''), 0, 64);
if ($device === '' || !is_array($in)) fail(400, 'bad payload');

try {
    $pdo = new PDO('mysql:host=' . DB_HOST . ';dbname=' . DB_NAME . ';charset=utf8mb4', DB_USER, DB_PASS,
        [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION]);

    $pdo->exec("CREATE TABLE IF NOT EXISTS isibalo_sales (
        device VARCHAR(64) NOT NULL, sale_id BIGINT NOT NULL, total_cents BIGINT NOT NULL,
        method VARCHAR(10) NOT NULL, created_at BIGINT NOT NULL, PRIMARY KEY (device, sale_id))");
    $pdo->exec("CREATE TABLE IF NOT EXISTS isibalo_sale_items (
        device VARCHAR(64) NOT NULL, item_id BIGINT NOT NULL, sale_id BIGINT NOT NULL,
        stock_item_id BIGINT NULL, name VARCHAR(200) NOT NULL, qty INT NOT NULL,
        unit_price_cents BIGINT NOT NULL, PRIMARY KEY (device, item_id))");
    $pdo->exec("CREATE TABLE IF NOT EXISTS isibalo_stock (
        device VARCHAR(64) NOT NULL, item_id BIGINT NOT NULL, name VARCHAR(200) NOT NULL,
        quantity INT NOT NULL, low_threshold INT NOT NULL, cost_cents BIGINT NOT NULL,
        price_cents BIGINT NOT NULL, updated_at BIGINT NOT NULL, PRIMARY KEY (device, item_id))");

    $pdo->beginTransaction();
    $sale = $pdo->prepare("INSERT INTO isibalo_sales (device,sale_id,total_cents,method,created_at)
        VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE total_cents=VALUES(total_cents), method=VALUES(method)");
    $item = $pdo->prepare("INSERT INTO isibalo_sale_items (device,item_id,sale_id,stock_item_id,name,qty,unit_price_cents)
        VALUES (?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE name=VALUES(name), qty=VALUES(qty), unit_price_cents=VALUES(unit_price_cents)");
    foreach (($in['sales'] ?? []) as $s) {
        $sale->execute([$device, $s['id'], $s['total_cents'], $s['method'], $s['created_at']]);
        foreach (($s['items'] ?? []) as $i) {
            $item->execute([$device, $i['id'], $s['id'], $i['stock_item_id'], $i['name'], $i['qty'], $i['unit_price_cents']]);
        }
    }
    $stock = $pdo->prepare("INSERT INTO isibalo_stock (device,item_id,name,quantity,low_threshold,cost_cents,price_cents,updated_at)
        VALUES (?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE name=VALUES(name), quantity=VALUES(quantity),
        low_threshold=VALUES(low_threshold), cost_cents=VALUES(cost_cents), price_cents=VALUES(price_cents), updated_at=VALUES(updated_at)");
    foreach (($in['stock'] ?? []) as $r) {
        $stock->execute([$device, $r['id'], $r['name'], $r['quantity'], $r['low_threshold'], $r['cost_cents'], $r['price_cents'], $r['updated_at']]);
    }
    $pdo->commit();
    echo json_encode(['ok' => true]);
} catch (Throwable $e) {
    if (isset($pdo) && $pdo->inTransaction()) $pdo->rollBack();
    fail(500, 'server error');
}
