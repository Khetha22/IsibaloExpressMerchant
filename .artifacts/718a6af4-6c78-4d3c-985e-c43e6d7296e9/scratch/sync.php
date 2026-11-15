<?php
/**
 * ============================================================================
 * Isibalo Express - Unified Sync Endpoint & Owner Dashboard (sync.php)
 * Domain: hbtechinc.co.za
 * ============================================================================
 * Upload to cPanel: /public_html/sync.php (alongside config.php)
 */

declare(strict_types=1);
session_start();

require_once __DIR__ . '/config.php';

$baseDir = __DIR__ . '/sync_data';
if (!is_dir($baseDir)) { @mkdir($baseDir, 0750, true); }
if (!file_exists($baseDir . '/.htaccess')) { @file_put_contents($baseDir . '/.htaccess', "Require all denied\nDeny from all\n"); }

$dataDir = $baseDir;
$mediaDir = $baseDir . '/media';

$action = $_GET['action'] ?? 'dashboard';
$store  = preg_replace('/[^A-Za-z0-9_\-]/', '', (string)($_GET['store'] ?? 'main'));
if ($store === '') { $store = 'main'; }

function set_store(string $s): void {
    global $baseDir, $dataDir, $mediaDir;
    $dataDir = $baseDir . '/' . $s;
    $mediaDir = $dataDir . '/media';
    if (!is_dir($mediaDir)) { @mkdir($mediaDir, 0750, true); }
}
set_store($store);

function key_ok(): bool {
    $headers = getallheaders();
    $k = $headers['X-Api-Key'] ?? $headers['x-api-key'] ?? $_SERVER['HTTP_X_API_KEY'] ?? $_GET['key'] ?? '';
    return $k !== '' && hash_equals(SYNC_API_KEY, $k);
}
function need_key(): void { if (!key_ok()) { http_response_code(401); echo json_encode(['status' => 'error', 'message' => 'Unauthorized']); exit; } }

// Handle API Actions (Push / Pull from Android App)
if ($action === 'push') {
    need_key();
    $body = file_get_contents('php://input');
    $json = json_decode($body ?: '', true);
    if (!is_array($json)) { http_response_code(400); echo json_encode(['status' => 'error', 'message' => 'Invalid JSON']); exit; }

    if (!is_dir($dataDir)) { @mkdir($dataDir, 0750, true); }
    file_put_contents($dataDir . '/snapshot.json', $body);
    file_put_contents($dataDir . '/snapshot_time.txt', date('Y-m-d H:i:s'));
    file_put_contents($dataDir . '/name.txt', substr(preg_replace('/[\r\n]+/', ' ', (string)($_GET['name'] ?? $store)), 0, 80));
    @copy($dataDir . '/snapshot.json', $dataDir . '/snap_' . date('Ymd_His') . '.json');

    echo json_encode(['status' => 'success', 'message' => 'Synced successfully']);
    exit;
}

if ($action === 'pull') {
    need_key();
    $f = $dataDir . '/snapshot.json';
    if (!file_exists($f)) { http_response_code(404); echo json_encode(['status' => 'error', 'message' => 'No snapshot yet']); exit; }
    header('Content-Type: application/json; charset=utf-8');
    readfile($f);
    exit;
}

/* ============================================================================ */
/* Owner Web Dashboard UI & Login                                              */
/* ============================================================================ */
if (isset($_POST['key'])) {
    if (hash_equals(SYNC_API_KEY, (string)$_POST['key'])) {
        $_SESSION['isibalo_owner_auth'] = true;
    } else {
        $loginError = 'Invalid API Key password.';
    }
    header('Location: sync.php?action=dashboard');
    exit;
}
if (isset($_GET['logout'])) {
    session_destroy();
    header('Location: sync.php?action=dashboard');
    exit;
}

$isAuthenticated = !empty($_SESSION['isibalo_owner_auth']);
$snapshotData = null;
$lastSyncTime = 'Never';
$storeName = 'Main Store';

if ($isAuthenticated && file_exists($dataDir . '/snapshot.json')) {
    $snapshotData = json_decode(file_get_contents($dataDir . '/snapshot.json'), true);
    if (file_exists($dataDir . '/snapshot_time.txt')) {
        $lastSyncTime = trim(file_get_contents($dataDir . '/snapshot_time.txt'));
    }
    if (file_exists($dataDir . '/name.txt')) {
        $storeName = trim(file_get_contents($dataDir . '/name.txt'));
    }
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Isibalo Express - Owner Dashboard</title>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
        :root {
            --primary: #0A4FD8;
            --primary-dark: #0739A0;
            --bg: #F4F7FC;
            --card-bg: #FFFFFF;
            --text: #0A1226;
            --text-muted: #64748B;
            --border: #B4C3E6;
            --error: #C62828;
            --success: #16A34A;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: 'Plus Jakarta Sans', sans-serif; background: var(--bg); color: var(--text); padding: 20px; line-height: 1.5; }
        .container { max-width: 1100px; margin: 0 auto; }
        header { background: var(--primary); color: #FFF; padding: 20px; border-radius: 12px; display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; box-shadow: 0 4px 12px rgba(10, 79, 216, 0.15); }
        header h1 { font-size: 1.5rem; font-weight: 800; }
        .logout-btn { background: rgba(255,255,255,0.2); color: #FFF; padding: 8px 16px; border-radius: 8px; text-decoration: none; font-weight: 600; font-size: 0.9rem; transition: background 0.2s; }
        .logout-btn:hover { background: rgba(255,255,255,0.3); }

        .login-card { background: var(--card-bg); max-width: 420px; margin: 80px auto; padding: 30px; border-radius: 16px; box-shadow: 0 10px 25px rgba(0,0,0,0.05); border: 1px solid var(--border); text-align: center; }
        .login-card h2 { color: var(--primary); margin-bottom: 1rem; font-weight: 800; }
        .login-card input { width: 100%; padding: 12px 16px; border: 1px solid var(--border); border-radius: 8px; font-size: 1rem; margin-bottom: 1rem; }
        .login-card button { width: 100%; padding: 12px; background: var(--primary); color: #FFF; border: none; border-radius: 8px; font-weight: 700; font-size: 1rem; cursor: pointer; }
        .login-card button:hover { background: var(--primary-dark); }
        .error-msg { color: var(--error); font-size: 0.85rem; margin-bottom: 1rem; font-weight: 600; }

        .kpi-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 16px; margin-bottom: 24px; }
        .kpi-card { background: var(--card-bg); border: 1px solid var(--border); border-radius: 12px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.03); }
        .kpi-title { font-size: 0.85rem; color: var(--text-muted); font-weight: 700; text-transform: uppercase; margin-bottom: 6px; }
        .kpi-value { font-size: 1.8rem; font-weight: 800; color: var(--primary); }

        .card { background: var(--card-bg); border: 1px solid var(--border); border-radius: 12px; padding: 24px; margin-bottom: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.03); }
        .card h3 { font-size: 1.2rem; font-weight: 800; margin-bottom: 1rem; color: var(--primary); }

        table { width: 100%; border-collapse: collapse; margin-top: 10px; }
        th, td { border: 1px solid var(--border); padding: 12px 16px; text-align: left; font-size: 0.9rem; }
        th { background: #EEF3FF; color: var(--primary); font-weight: 700; }
        tr:nth-child(even) { background: #F8FAFC; }
    </style>
</head>
<body>
<div class="container">

<?php if (!$isAuthenticated): ?>
    <div class="login-card">
        <h2>Isibalo Express</h2>
        <p style="color:var(--text-muted); font-size:0.9rem; margin-bottom:1.5rem;">Enter your secure Sync API Key to access the merchant owner dashboard.</p>
        <?php if (!empty($loginError)): ?>
            <div class="error-msg"><?php echo htmlspecialchars($loginError); ?></div>
        <?php endif; ?>
        <form method="POST">
            <input type="password" name="key" placeholder="Enter Sync API Key..." required autofocus>
            <button type="submit">Access Dashboard</button>
        </form>
    </div>

<?php else:
    $salesList = $snapshotData['sales'] ?? [];
    $stockList = $snapshotData['stock'] ?? [];

    $totalRevenueCents = 0;
    foreach ($salesList as $s) {
        $totalRevenueCents += ($s['total_cents'] ?? $s['totalCents'] ?? 0);
    }
    $totalRevenueRands = $totalRevenueCents / 100.0;
?>
    <header>
        <div>
            <h1>Isibalo Express Dashboard</h1>
            <p style="font-size: 0.85rem; opacity: 0.9;">Store: <?php echo htmlspecialchars($storeName); ?> | Last Synced: <?php echo htmlspecialchars($lastSyncTime); ?></p>
        </div>
        <a href="?logout=1" class="logout-btn">Log Out</a>
    </header>

    <div class="kpi-grid">
        <div class="kpi-card">
            <div class="kpi-title">Total Revenue</div>
            <div class="kpi-value">R <?php echo number_format($totalRevenueRands, 2); ?></div>
        </div>
        <div class="kpi-card">
            <div class="kpi-title">Total Transactions</div>
            <div class="kpi-value"><?php echo count($salesList); ?></div>
        </div>
        <div class="kpi-card">
            <div class="kpi-title">Active Inventory SKUs</div>
            <div class="kpi-value"><?php echo count($stockList); ?></div>
        </div>
        <div class="kpi-card">
            <div class="kpi-title">Sync Status</div>
            <div class="kpi-value" style="font-size:1.2rem; color:var(--success); margin-top:6px;">● Live Online</div>
        </div>
    </div>

    <div class="card">
        <h3>Recent Synced Sales</h3>
        <?php if (empty($salesList)): ?>
            <p style="color:var(--text-muted);">No sales synced yet. Make a sale in your Isibalo Express app and tap 'SYNC NOW'!</p>
        <?php else: ?>
            <table>
                <thead>
                    <tr>
                        <th>Sale ID</th>
                        <th>Payment Method</th>
                        <th>Total Amount</th>
                        <th>Timestamp</th>
                    </tr>
                </thead>
                <tbody>
                    <?php foreach (array_slice(array_reverse($salesList), 0, 10) as $sale):
                        $amt = ($sale['total_cents'] ?? $sale['totalCents'] ?? 0) / 100.0;
                        $method = $sale['method'] ?? 'CASH';
                        $time = date('Y-m-d H:i:s', ($sale['created_at'] ?? $sale['createdAt'] ?? time()) / 1000);
                    ?>
                    <tr>
                        <td>#<?php echo htmlspecialchars((string)($sale['id'] ?? '0')); ?></td>
                        <td><strong><?php echo htmlspecialchars($method); ?></strong></td>
                        <td>R <?php echo number_format($amt, 2); ?></td>
                        <td><?php echo htmlspecialchars($time); ?></td>
                    </tr>
                    <?php endforeach; ?>
                </tbody>
            </table>
        <?php endif; ?>
    </div>

    <div class="card">
        <h3>Live Inventory Status</h3>
        <?php if (empty($stockList)): ?>
            <p style="color:var(--text-muted);">No inventory items synced yet.</p>
        <?php else: ?>
            <table>
                <thead>
                    <tr>
                        <th>Item Name</th>
                        <th>In Stock Quantity</th>
                        <th>Selling Price</th>
                        <th>Cost Price</th>
                    </tr>
                </thead>
                <tbody>
                    <?php foreach ($stockList as $item):
                        $price = ($item['price_cents'] ?? $item['priceCents'] ?? 0) / 100.0;
                        $cost = ($item['cost_cents'] ?? $item['costCents'] ?? 0) / 100.0;
                    ?>
                    <tr>
                        <td><strong><?php echo htmlspecialchars($item['name'] ?? 'Item'); ?></strong></td>
                        <td><?php echo intval($item['quantity'] ?? 0); ?> units</td>
                        <td>R <?php echo number_format($price, 2); ?></td>
                        <td>R <?php echo number_format($cost, 2); ?></td>
                    </tr>
                    <?php endforeach; ?>
                </tbody>
            </table>
        <?php endif; ?>
    </div>

<?php endif; ?>

</div>
</body>
</html>
