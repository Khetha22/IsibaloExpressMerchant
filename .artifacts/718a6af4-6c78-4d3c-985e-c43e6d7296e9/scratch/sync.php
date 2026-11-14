<?php
/**
 * ============================================================================
 * Isibalo Express Merchant - Backend Synchronization Endpoint (sync.php)
 * Domain: hbtechinc.co.za
 * Path on Server: /public_html/isibalo/api/sync.php
 * ============================================================================
 * Receives encrypted JSON payloads from Android WorkManager and stores them in MySQL.
 */

// Enable CORS and JSON header
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, GET, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, X-Api-Key');
header('Content-Type: application/json; charset=utf-8');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit;
}

// 1. Database Configuration (Update with your cPanel MySQL details)
$host = 'localhost';
$db   = 'hbtech_isibalo_db'; // Replace with your cPanel database name
$user = 'hbtech_isibalo_user'; // Replace with your cPanel database username
$pass = 'YOUR_SECURE_DATABASE_PASSWORD'; // Replace with your password

// 2. Security API Key (Must match BuildConfig.SYNC_API_KEY in Android app)
$expectedApiKey = 'CHANGE_ME';

// Get API Key from header or JSON body
$headers = getallheaders();
$apiKeyHeader = $headers['X-Api-Key'] ?? $headers['x-api-key'] ?? '';

$rawInput = file_get_contents('php://input');
$data = json_decode($rawInput, true);

$apiKeyBody = $data['apiKey'] ?? '';

if ($apiKeyHeader !== $expectedApiKey && $apiKeyBody !== $expectedApiKey) {
    http_response_code(401);
    echo json_encode(['status' => 'error', 'message' => 'Unauthorized: Invalid or missing API Key']);
    exit;
}

// 3. Connect to MySQL via PDO
try {
    $pdo = new PDO("mysql:host=$host;dbname=$db;charset=utf8mb4", $user, $pass, [
        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
    ]);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(['status' => 'error', 'message' => 'Database connection failed: ' . $e->getMessage()]);
    exit;
}

// 4. Process Incoming Sales and Stock Data
$deviceId = $data['device'] ?? 'unknown_device';
$sales = $data['sales'] ?? [];
$stock = $data['stock'] ?? [];

$syncedSalesCount = 0;
$syncedStockCount = 0;

try {
    $pdo->beginTransaction();

    // Insert or Update Sales (Idempotent Upsert)
    $stmtSale = $pdo->prepare("
        INSERT INTO sales (local_id, device_id, total_cents, method, created_at)
        VALUES (:id, :device, :total_cents, :method, FROM_UNIXTIME(:created_at / 1000))
        ON DUPLICATE KEY UPDATE total_cents = :total_cents, method = :method
    ");

    $stmtItem = $pdo->prepare("
        INSERT INTO sale_items (sale_local_id, stock_item_id, name, qty, unit_price_cents)
        VALUES (:sale_id, :stock_item_id, :name, :qty, :unit_price_cents)
    ");

    foreach ($sales as $sale) {
        $stmtSale->execute([
            'id' => $sale['id'] ?? 0,
            'device' => $deviceId,
            'total_cents' => $sale['total_cents'] ?? $sale['totalCents'] ?? 0,
            'method' => $sale['method'] ?? 'CASH',
            'created_at' => $sale['created_at'] ?? $sale['createdAt'] ?? time() * 1000
        ]);
        $syncedSalesCount++;

        // Process Sale Items if present
        if (isset($sale['items']) && is_array($sale['items'])) {
            foreach ($sale['items'] as $item) {
                $stmtItem->execute([
                    'sale_id' => $sale['id'] ?? 0,
                    'stock_item_id' => $item['stock_item_id'] ?? null,
                    'name' => $item['name'] ?? 'Item',
                    'qty' => $item['qty'] ?? 1,
                    'unit_price_cents' => $item['unit_price_cents'] ?? 0
                ]);
            }
        }
    }

    // Insert or Update Stock Items
    $stmtStock = $pdo->prepare("
        INSERT INTO stock_items (local_id, device_id, name, quantity, low_threshold, cost_cents, price_cents, updated_at)
        VALUES (:id, :device, :name, :quantity, :low_threshold, :cost_cents, :price_cents, FROM_UNIXTIME(:updated_at / 1000))
        ON DUPLICATE KEY UPDATE quantity = :quantity, price_cents = :price_cents, cost_cents = :cost_cents
    ");

    foreach ($stock as $item) {
        $stmtStock->execute([
            'id' => $item['id'] ?? 0,
            'device' => $deviceId,
            'name' => $item['name'] ?? 'Stock Item',
            'quantity' => $item['quantity'] ?? 0,
            'low_threshold' => $item['low_threshold'] ?? 5,
            'cost_cents' => $item['cost_cents'] ?? 0,
            'price_cents' => $item['price_cents'] ?? 0,
            'updated_at' => $item['updated_at'] ?? time() * 1000
        ]);
        $syncedStockCount++;
    }

    $pdo->commit();

    echo json_encode([
        'status' => 'success',
        'message' => 'Sync completed successfully',
        'synced_sales' => $syncedSalesCount,
        'synced_stock' => $syncedStockCount,
        'timestamp' => time()
    ]);

} catch (Exception $ex) {
    if ($pdo->inTransaction()) {
        $pdo->rollBack();
    }
    http_response_code(500);
    echo json_encode([
        'status' => 'error',
        'message' => 'Transaction sync failed: ' . $ex.getMessage()
    ]);
}
?>
