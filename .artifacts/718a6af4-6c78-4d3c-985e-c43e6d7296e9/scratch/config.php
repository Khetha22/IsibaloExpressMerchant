<?php
/**
 * ============================================================================
 * Isibalo Express - Database & Security Configuration (config.php)
 * Domain: hbtechinc.co.za
 * ============================================================================
 * Upload to cPanel in the same directory as sync.php (e.g., /public_html/config.php)
 */

declare(strict_types=1);

// 1. MySQL Database Credentials (Taken from your Afrihost cPanel screenshots)
define('DB_HOST', 'localhost');
define('DB_NAME', 'hbtechv1x6a2_htech_db');
define('DB_USER', 'hbtechv1x6a2_isibaloexpress');
define('DB_PASS', 'YOUR_CPANEL_USER_PASSWORD'); // Replace with the password you set for hbtechv1x6a2_isibaloexpress in cPanel

// 2. Shared Security API Key (Must match SYNC_API_KEY in Android app's build.gradle.kts)
define('SYNC_API_KEY', 'isibalo_live_sec_998877665544332211');

/**
 * Returns a shared PDO database connection instance.
 */
function get_db_connection(): PDO {
    static $pdo = null;
    if ($pdo === null) {
        try {
            $pdo = new PDO(
                "mysql:host=" . DB_HOST . ";dbname=" . DB_NAME . ";charset=utf8mb4",
                DB_USER,
                DB_PASS,
                [
                    PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
                    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                    PDO::ATTR_TIMEOUT => 15
                ]
            );
        } catch (PDOException $e) {
            http_response_code(500);
            header('Content-Type: application/json; charset=utf-8');
            echo json_encode([
                'status' => 'error',
                'message' => 'Database connection failed: ' . $e->getMessage()
            ]);
            exit;
        }
    }
    return $pdo;
}
?>
