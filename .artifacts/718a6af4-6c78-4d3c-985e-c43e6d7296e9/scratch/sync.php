<?php
/**
 * Isibalo Express Merchant - Backend Synchronization Endpoint (sync.php)
 * Receives JSON payloads from Android WorkManager and stores them in MySQL.
 */

header('Content-Type: application/json; charset=utf-8');

// Database Configuration
$host = 'localhost';
$db   = 'isibalo_db';
$user = 'isibalo_user';
$pass = 'SECURE_DB_PASSWORD';
$expectedApiKey = 'CHANGE_ME'; // Matches BuildConfig.SYNC_API_KEY

// 1. Read Incoming JSON Request
$rawInput = file_get_contents('php://input');
$data = json_decode($rawInput, true);

if (!$data) {
    http_response_code(400);
    echo json_encode(['status' => 'error', 'message' => 'Invalid JSON payload']);
    exit;
}

// 2. Validate API Key
if (!isset($data['apiKey']) || $data['apiKey'] !== $expectedApiKey) {
    http_response_code(401);
    echo json_encode(['status' => 'error', 'message' => 'Unauthorized: Invalid API Key']);
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
    echo json_encode(['status' => 'error', 'message' => 'Database connection failed']);
    exit;
}

// 4. Process Sales Records
$sales = $data['sales'] ?? [];
$syncedCount = 0;

$stmt = $pdo->prepare("INSERT INTO sales (local_id, total_cents, method, created_at) VALUES (:local_id, :total_cents, :method, FROM_UNIXTIME(:created_at / 1000)) ON DUPLICATE KEY UPDATE total_cents = :total_cents");

foreach ($sales as $sale) {
    try {
        $stmt->execute([
            'local_id' => $sale['id'],
            'total_cents' => $sale['totalCents'],
            'method' => $sale['method'],
            'created_at' => $sale['createdAt']
        ]);
        $syncedCount++;
    } catch (Exception $ex) {
        // Log individual record failure if needed
    }
}

// 5. Return Success Response
echo json_encode([
    'status' => 'success',
    'synced_sales' => $syncedCount,
    'timestamp' => time()
]);
?>
