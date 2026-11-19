package za.co.hbtechinc.isibalo.desktop.data

import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement

class DesktopDatabase {
    private val dbPath: String by lazy {
        val userHome = System.getProperty("user.home")
        val dir = File(userHome, ".isibalo_express")
        if (!dir.exists()) dir.mkdirs()
        File(dir, "isibalo_express_desktop.db").absolutePath
    }

    private fun getConnection(): Connection {
        Class.forName("org.sqlite.JDBC")
        return DriverManager.getConnection("jdbc:sqlite:$dbPath")
    }

    init {
        createTables()
        seedIfEmpty()
    }

    private fun createTables() {
        getConnection().use { conn ->
            conn.createStatement().use { stmt ->
                stmt.execute(
                    """
                    CREATE TABLE IF NOT EXISTS stock_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        quantity INTEGER NOT NULL,
                        lowStockThreshold INTEGER NOT NULL DEFAULT 5,
                        costCents INTEGER NOT NULL DEFAULT 0,
                        priceCents INTEGER NOT NULL,
                        expiryDate INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL,
                        synced INTEGER NOT NULL DEFAULT 0
                    );
                    """.trimIndent()
                )

                stmt.execute(
                    """
                    CREATE TABLE IF NOT EXISTS sales (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        totalCents INTEGER NOT NULL,
                        method TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        synced INTEGER NOT NULL DEFAULT 0
                    );
                    """.trimIndent()
                )

                stmt.execute(
                    """
                    CREATE TABLE IF NOT EXISTS sale_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        saleId INTEGER NOT NULL,
                        stockItemId INTEGER,
                        name TEXT NOT NULL,
                        qty INTEGER NOT NULL,
                        unitPriceCents INTEGER NOT NULL,
                        FOREIGN KEY(saleId) REFERENCES sales(id) ON DELETE CASCADE
                    );
                    """.trimIndent()
                )

                stmt.execute(
                    """
                    CREATE TABLE IF NOT EXISTS cash_up_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        date INTEGER NOT NULL,
                        floatCents INTEGER NOT NULL,
                        expectedCashCents INTEGER NOT NULL,
                        actualCashCents INTEGER NOT NULL,
                        varianceCents INTEGER NOT NULL,
                        cardTotalCents INTEGER NOT NULL DEFAULT 0,
                        mobileTotalCents INTEGER NOT NULL DEFAULT 0,
                        notes TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL,
                        synced INTEGER NOT NULL DEFAULT 0
                    );
                    """.trimIndent()
                )

                stmt.execute(
                    """
                    CREATE TABLE IF NOT EXISTS knowledge_docs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        title TEXT NOT NULL,
                        content TEXT NOT NULL,
                        category TEXT NOT NULL DEFAULT 'General',
                        updatedAt INTEGER NOT NULL
                    );
                    """.trimIndent()
                )

                stmt.execute(
                    """
                    CREATE TABLE IF NOT EXISTS customer_debts (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        customerName TEXT NOT NULL,
                        phone TEXT NOT NULL,
                        amountCents INTEGER NOT NULL,
                        note TEXT NOT NULL DEFAULT '',
                        settled INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        synced INTEGER NOT NULL DEFAULT 0
                    );
                    """.trimIndent()
                )

                stmt.execute(
                    """
                    CREATE TABLE IF NOT EXISTS expenses (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        title TEXT NOT NULL,
                        amountCents INTEGER NOT NULL,
                        category TEXT NOT NULL DEFAULT 'General',
                        createdAt INTEGER NOT NULL,
                        synced INTEGER NOT NULL DEFAULT 0
                    );
                    """.trimIndent()
                )

                stmt.execute(
                    """
                    CREATE TABLE IF NOT EXISTS petty_cash (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        recipient TEXT NOT NULL,
                        amountCents INTEGER NOT NULL,
                        note TEXT NOT NULL DEFAULT '',
                        issuedAt INTEGER NOT NULL,
                        settled INTEGER NOT NULL DEFAULT 0,
                        synced INTEGER NOT NULL DEFAULT 0
                    );
                    """.trimIndent()
                )

                stmt.execute(
                    """
                    CREATE TABLE IF NOT EXISTS merchant_loans (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        principalCents INTEGER NOT NULL,
                        feeCents INTEGER NOT NULL,
                        totalRepayCents INTEGER NOT NULL,
                        remainingCents INTEGER NOT NULL,
                        status TEXT NOT NULL DEFAULT 'Active',
                        createdAt INTEGER NOT NULL
                    );
                    """.trimIndent()
                )

                stmt.execute(
                    """
                    CREATE TABLE IF NOT EXISTS staff_members (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        role TEXT NOT NULL DEFAULT 'Cashier',
                        dailyWageCents INTEGER NOT NULL,
                        phone TEXT NOT NULL DEFAULT '',
                        active INTEGER NOT NULL DEFAULT 1,
                        synced INTEGER NOT NULL DEFAULT 0
                    );
                    """.trimIndent()
                )

                stmt.execute(
                    """
                    CREATE TABLE IF NOT EXISTS whatsapp_share_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        title TEXT NOT NULL,
                        summary TEXT NOT NULL,
                        sharedAt INTEGER NOT NULL,
                        synced INTEGER NOT NULL DEFAULT 0
                    );
                    """.trimIndent()
                )
            }
        }
    }

    fun seedIfEmpty() {
        getConnection().use { conn ->
            val countStock = conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery("SELECT COUNT(*) FROM stock_items")
                if (rs.next()) rs.getInt(1) else 0
            }
            if (countStock == 0) {
                val stockList = listOf(
                    StockItem(name = "Bread 700g", quantity = 25, lowStockThreshold = 5, costCents = 1500, priceCents = 1800),
                    StockItem(name = "Cooking Oil 750ml", quantity = 14, lowStockThreshold = 4, costCents = 3500, priceCents = 4200),
                    StockItem(name = "Airtime R10", quantity = 50, lowStockThreshold = 10, costCents = 950, priceCents = 1000),
                    StockItem(name = "Fresh Milk 2L", quantity = 10, lowStockThreshold = 4, costCents = 2800, priceCents = 3300),
                    StockItem(name = "Till Rolls 57mm", quantity = 8, lowStockThreshold = 5, costCents = 900, priceCents = 1400),
                    StockItem(name = "Speedpoint Paper", quantity = 4, lowStockThreshold = 5, costCents = 1200, priceCents = 1800)
                )
                stockList.forEach { upsertStock(it) }
            }

            val countKnowledge = conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery("SELECT COUNT(*) FROM knowledge_docs")
                if (rs.next()) rs.getInt(1) else 0
            }
            if (countKnowledge == 0) {
                val docs = listOf(
                    KnowledgeDoc(
                        title = "Store Return & Exchange Policy",
                        category = "Policy",
                        content = "Refunds and exchanges are allowed within 7 days with a valid receipt. Perishable items (milk, bread) cannot be returned once opened."
                    ),
                    KnowledgeDoc(
                        title = "Wholesale Supplier Contacts",
                        category = "Suppliers",
                        content = "Bread: Albany Driver Sipho 082 555 0199. Drinks: Coca-Cola Depot Sandton 011 400 2000. Airtime: Flash Merchant Support 083 900 0000."
                    ),
                    KnowledgeDoc(
                        title = "End of Day Cashup Procedure",
                        category = "Operations",
                        content = "Count physical cash in till drawer. Deduct float of R500. Match cash total against Isibalo Express Expected Cash total. Report any variance above R50."
                    )
                )
                docs.forEach { upsertKnowledgeDoc(it) }
            }

            val countStaff = conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery("SELECT COUNT(*) FROM staff_members")
                if (rs.next()) rs.getInt(1) else 0
            }
            if (countStaff == 0) {
                val staffList = listOf(
                    Staff(name = "Sibusiso Dlamini", role = "Store Manager", dailyWageCents = 45000, phone = "071 234 5678"),
                    Staff(name = "Thandiwe Khumalo", role = "Cashier", dailyWageCents = 28000, phone = "082 345 6789")
                )
                staffList.forEach { upsertStaff(it) }
            }
        }
    }

    // --- Stock Items ---
    fun getAllStock(): List<StockItem> {
        val list = mutableListOf<StockItem>()
        getConnection().use { conn ->
            val rs = conn.createStatement().executeQuery("SELECT * FROM stock_items ORDER BY name ASC")
            while (rs.next()) {
                list.add(
                    StockItem(
                        id = rs.getLong("id"),
                        name = rs.getString("name"),
                        quantity = rs.getInt("quantity"),
                        lowStockThreshold = rs.getInt("lowStockThreshold"),
                        costCents = rs.getLong("costCents"),
                        priceCents = rs.getLong("priceCents"),
                        expiryDate = rs.getLong("expiryDate"),
                        updatedAt = rs.getLong("updatedAt"),
                        synced = rs.getInt("synced") == 1
                    )
                )
            }
        }
        return list
    }

    fun upsertStock(item: StockItem) {
        getConnection().use { conn ->
            if (item.id == 0L) {
                val sql = "INSERT INTO stock_items (name, quantity, lowStockThreshold, costCents, priceCents, expiryDate, updatedAt, synced) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
                conn.prepareStatement(sql).use { pstmt ->
                    pstmt.setString(1, item.name)
                    pstmt.setInt(2, item.quantity)
                    pstmt.setInt(3, item.lowStockThreshold)
                    pstmt.setLong(4, item.costCents)
                    pstmt.setLong(5, item.priceCents)
                    pstmt.setLong(6, item.expiryDate)
                    pstmt.setLong(7, System.currentTimeMillis())
                    pstmt.setInt(8, if (item.synced) 1 else 0)
                    pstmt.executeUpdate()
                }
            } else {
                val sql = "UPDATE stock_items SET name = ?, quantity = ?, lowStockThreshold = ?, costCents = ?, priceCents = ?, expiryDate = ?, updatedAt = ?, synced = ? WHERE id = ?"
                conn.prepareStatement(sql).use { pstmt ->
                    pstmt.setString(1, item.name)
                    pstmt.setInt(2, item.quantity)
                    pstmt.setInt(3, item.lowStockThreshold)
                    pstmt.setLong(4, item.costCents)
                    pstmt.setLong(5, item.priceCents)
                    pstmt.setLong(6, item.expiryDate)
                    pstmt.setLong(7, System.currentTimeMillis())
                    pstmt.setInt(8, if (item.synced) 1 else 0)
                    pstmt.setLong(9, item.id)
                    pstmt.executeUpdate()
                }
            }
        }
    }

    fun deleteStock(id: Long) {
        getConnection().use { conn ->
            val pstmt = conn.prepareStatement("DELETE FROM stock_items WHERE id = ?")
            pstmt.setLong(1, id)
            pstmt.executeUpdate()
        }
    }

    // --- Sales & Checkout ---
    fun checkout(lines: List<CartLine>, method: PaymentMethod): Long {
        getConnection().use { conn ->
            conn.autoCommit = false
            try {
                val totalCents = lines.sumOf { it.unitPriceCents * it.qty }
                val now = System.currentTimeMillis()

                val insertSaleSql = "INSERT INTO sales (totalCents, method, createdAt, synced) VALUES (?, ?, ?, 0)"
                val saleId: Long
                conn.prepareStatement(insertSaleSql, Statement.RETURN_GENERATED_KEYS).use { pstmt ->
                    pstmt.setLong(1, totalCents)
                    pstmt.setString(2, method.name)
                    pstmt.setLong(3, now)
                    pstmt.executeUpdate()
                    val rs = pstmt.generatedKeys
                    saleId = if (rs.next()) rs.getLong(1) else 0L
                }

                val insertItemSql = "INSERT INTO sale_items (saleId, stockItemId, name, qty, unitPriceCents) VALUES (?, ?, ?, ?, ?)"
                conn.prepareStatement(insertItemSql).use { pstmt ->
                    lines.forEach { line ->
                        pstmt.setLong(1, saleId)
                        if (line.stockItemId != null) pstmt.setLong(2, line.stockItemId) else pstmt.setNull(2, java.sql.Types.INTEGER)
                        pstmt.setString(3, line.name)
                        pstmt.setInt(4, line.qty)
                        pstmt.setLong(5, line.unitPriceCents)
                        pstmt.executeUpdate()
                    }
                }

                val decStockSql = "UPDATE stock_items SET quantity = MAX(quantity - ?, 0), updatedAt = ? WHERE id = ?"
                conn.prepareStatement(decStockSql).use { pstmt ->
                    lines.forEach { line ->
                        if (line.stockItemId != null) {
                            pstmt.setInt(1, line.qty)
                            pstmt.setLong(2, now)
                            pstmt.setLong(3, line.stockItemId)
                            pstmt.executeUpdate()
                        }
                    }
                }

                conn.commit()
                return saleId
            } catch (e: Exception) {
                conn.rollback()
                throw e
            } finally {
                conn.autoCommit = true
            }
        }
    }

    fun getSalesSince(from: Long): List<SaleWithItems> {
        val salesMap = mutableMapOf<Long, Sale>()
        val itemsMap = mutableMapOf<Long, MutableList<SaleItem>>()

        getConnection().use { conn ->
            val salesSql = "SELECT * FROM sales WHERE createdAt >= ? ORDER BY createdAt DESC"
            conn.prepareStatement(salesSql).use { pstmt ->
                pstmt.setLong(1, from)
                val rs = pstmt.executeQuery()
                while (rs.next()) {
                    val sId = rs.getLong("id")
                    salesMap[sId] = Sale(
                        id = sId,
                        totalCents = rs.getLong("totalCents"),
                        method = try { PaymentMethod.valueOf(rs.getString("method")) } catch (_: Exception) { PaymentMethod.CASH },
                        createdAt = rs.getLong("createdAt"),
                        synced = rs.getInt("synced") == 1
                    )
                    itemsMap[sId] = mutableListOf()
                }
            }

            if (salesMap.isNotEmpty()) {
                val itemsSql = "SELECT * FROM sale_items ORDER BY id ASC"
                val rs = conn.createStatement().executeQuery(itemsSql)
                while (rs.next()) {
                    val sId = rs.getLong("saleId")
                    if (salesMap.containsKey(sId)) {
                        itemsMap[sId]?.add(
                            SaleItem(
                                id = rs.getLong("id"),
                                saleId = sId,
                                stockItemId = rs.getObject("stockItemId") as? Long,
                                name = rs.getString("name"),
                                qty = rs.getInt("qty"),
                                unitPriceCents = rs.getLong("unitPriceCents")
                            )
                        )
                    }
                }
            }
        }

        return salesMap.values.map { sale ->
            SaleWithItems(sale, itemsMap[sale.id] ?: emptyList())
        }
    }

    // --- Cash-Up ---
    fun getAllCashUp(): List<CashUpRecord> {
        val list = mutableListOf<CashUpRecord>()
        getConnection().use { conn ->
            val rs = conn.createStatement().executeQuery("SELECT * FROM cash_up_records ORDER BY createdAt DESC")
            while (rs.next()) {
                list.add(
                    CashUpRecord(
                        id = rs.getLong("id"),
                        date = rs.getLong("date"),
                        floatCents = rs.getLong("floatCents"),
                        expectedCashCents = rs.getLong("expectedCashCents"),
                        actualCashCents = rs.getLong("actualCashCents"),
                        varianceCents = rs.getLong("varianceCents"),
                        cardTotalCents = rs.getLong("cardTotalCents"),
                        mobileTotalCents = rs.getLong("mobileTotalCents"),
                        notes = rs.getString("notes"),
                        createdAt = rs.getLong("createdAt"),
                        synced = rs.getInt("synced") == 1
                    )
                )
            }
        }
        return list
    }

    fun insertCashUp(record: CashUpRecord): Long {
        getConnection().use { conn ->
            val sql = "INSERT INTO cash_up_records (date, floatCents, expectedCashCents, actualCashCents, varianceCents, cardTotalCents, mobileTotalCents, notes, createdAt, synced) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { pstmt ->
                pstmt.setLong(1, record.date)
                pstmt.setLong(2, record.floatCents)
                pstmt.setLong(3, record.expectedCashCents)
                pstmt.setLong(4, record.actualCashCents)
                pstmt.setLong(5, record.varianceCents)
                pstmt.setLong(6, record.cardTotalCents)
                pstmt.setLong(7, record.mobileTotalCents)
                pstmt.setString(8, record.notes)
                pstmt.setLong(9, System.currentTimeMillis())
                pstmt.setInt(10, if (record.synced) 1 else 0)
                pstmt.executeUpdate()
                val rs = pstmt.generatedKeys
                return if (rs.next()) rs.getLong(1) else 0L
            }
        }
    }

    // --- Customer Debts ---
    fun getAllCustomerDebts(): List<CustomerDebt> {
        val list = mutableListOf<CustomerDebt>()
        getConnection().use { conn ->
            val rs = conn.createStatement().executeQuery("SELECT * FROM customer_debts ORDER BY settled ASC, createdAt DESC")
            while (rs.next()) {
                list.add(
                    CustomerDebt(
                        id = rs.getLong("id"),
                        customerName = rs.getString("customerName"),
                        phone = rs.getString("phone"),
                        amountCents = rs.getLong("amountCents"),
                        note = rs.getString("note"),
                        settled = rs.getInt("settled") == 1,
                        createdAt = rs.getLong("createdAt"),
                        synced = rs.getInt("synced") == 1
                    )
                )
            }
        }
        return list
    }

    fun insertCustomerDebt(debt: CustomerDebt): Long {
        getConnection().use { conn ->
            val sql = "INSERT INTO customer_debts (customerName, phone, amountCents, note, settled, createdAt, synced) VALUES (?, ?, ?, ?, ?, ?, ?)"
            conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { pstmt ->
                pstmt.setString(1, debt.customerName)
                pstmt.setString(2, debt.phone)
                pstmt.setLong(3, debt.amountCents)
                pstmt.setString(4, debt.note)
                pstmt.setInt(5, if (debt.settled) 1 else 0)
                pstmt.setLong(6, System.currentTimeMillis())
                pstmt.setInt(7, if (debt.synced) 1 else 0)
                pstmt.executeUpdate()
                val rs = pstmt.generatedKeys
                return if (rs.next()) rs.getLong(1) else 0L
            }
        }
    }

    fun settleDebt(id: Long) {
        getConnection().use { conn ->
            val pstmt = conn.prepareStatement("UPDATE customer_debts SET settled = 1 WHERE id = ?")
            pstmt.setLong(1, id)
            pstmt.executeUpdate()
        }
    }

    fun deleteDebt(id: Long) {
        getConnection().use { conn ->
            val pstmt = conn.prepareStatement("DELETE FROM customer_debts WHERE id = ?")
            pstmt.setLong(1, id)
            pstmt.executeUpdate()
        }
    }

    // --- Expenses ---
    fun getAllExpenses(): List<Expense> {
        val list = mutableListOf<Expense>()
        getConnection().use { conn ->
            val rs = conn.createStatement().executeQuery("SELECT * FROM expenses ORDER BY createdAt DESC")
            while (rs.next()) {
                list.add(
                    Expense(
                        id = rs.getLong("id"),
                        title = rs.getString("title"),
                        amountCents = rs.getLong("amountCents"),
                        category = rs.getString("category"),
                        createdAt = rs.getLong("createdAt"),
                        synced = rs.getInt("synced") == 1
                    )
                )
            }
        }
        return list
    }

    fun insertExpense(expense: Expense): Long {
        getConnection().use { conn ->
            val sql = "INSERT INTO expenses (title, amountCents, category, createdAt, synced) VALUES (?, ?, ?, ?, ?)"
            conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { pstmt ->
                pstmt.setString(1, expense.title)
                pstmt.setLong(2, expense.amountCents)
                pstmt.setString(3, expense.category)
                pstmt.setLong(4, System.currentTimeMillis())
                pstmt.setInt(5, if (expense.synced) 1 else 0)
                pstmt.executeUpdate()
                val rs = pstmt.generatedKeys
                return if (rs.next()) rs.getLong(1) else 0L
            }
        }
    }

    fun deleteExpense(id: Long) {
        getConnection().use { conn ->
            val pstmt = conn.prepareStatement("DELETE FROM expenses WHERE id = ?")
            pstmt.setLong(1, id)
            pstmt.executeUpdate()
        }
    }

    // --- Petty Cash ---
    fun getAllPettyCash(): List<PettyCash> {
        val list = mutableListOf<PettyCash>()
        getConnection().use { conn ->
            val rs = conn.createStatement().executeQuery("SELECT * FROM petty_cash ORDER BY settled ASC, issuedAt DESC")
            while (rs.next()) {
                list.add(
                    PettyCash(
                        id = rs.getLong("id"),
                        recipient = rs.getString("recipient"),
                        amountCents = rs.getLong("amountCents"),
                        note = rs.getString("note"),
                        issuedAt = rs.getLong("issuedAt"),
                        settled = rs.getInt("settled") == 1,
                        synced = rs.getInt("synced") == 1
                    )
                )
            }
        }
        return list
    }

    fun insertPettyCash(record: PettyCash): Long {
        getConnection().use { conn ->
            val sql = "INSERT INTO petty_cash (recipient, amountCents, note, issuedAt, settled, synced) VALUES (?, ?, ?, ?, ?, ?)"
            conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { pstmt ->
                pstmt.setString(1, record.recipient)
                pstmt.setLong(2, record.amountCents)
                pstmt.setString(3, record.note)
                pstmt.setLong(4, System.currentTimeMillis())
                pstmt.setInt(5, if (record.settled) 1 else 0)
                pstmt.setInt(6, if (record.synced) 1 else 0)
                pstmt.executeUpdate()
                val rs = pstmt.generatedKeys
                return if (rs.next()) rs.getLong(1) else 0L
            }
        }
    }

    fun settlePettyCash(id: Long) {
        getConnection().use { conn ->
            val pstmt = conn.prepareStatement("UPDATE petty_cash SET settled = 1 WHERE id = ?")
            pstmt.setLong(1, id)
            pstmt.executeUpdate()
        }
    }

    fun deletePettyCash(id: Long) {
        getConnection().use { conn ->
            val pstmt = conn.prepareStatement("DELETE FROM petty_cash WHERE id = ?")
            pstmt.setLong(1, id)
            pstmt.executeUpdate()
        }
    }

    // --- Merchant Loans ---
    fun getAllLoans(): List<MerchantLoan> {
        val list = mutableListOf<MerchantLoan>()
        getConnection().use { conn ->
            val rs = conn.createStatement().executeQuery("SELECT * FROM merchant_loans ORDER BY createdAt DESC")
            while (rs.next()) {
                list.add(
                    MerchantLoan(
                        id = rs.getLong("id"),
                        principalCents = rs.getLong("principalCents"),
                        feeCents = rs.getLong("feeCents"),
                        totalRepayCents = rs.getLong("totalRepayCents"),
                        remainingCents = rs.getLong("remainingCents"),
                        status = rs.getString("status"),
                        createdAt = rs.getLong("createdAt")
                    )
                )
            }
        }
        return list
    }

    fun insertLoan(principalCents: Long): Long {
        val feeCents = (principalCents * 0.10).toLong()
        val total = principalCents + feeCents
        getConnection().use { conn ->
            val sql = "INSERT INTO merchant_loans (principalCents, feeCents, totalRepayCents, remainingCents, status, createdAt) VALUES (?, ?, ?, ?, 'Active', ?)"
            conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { pstmt ->
                pstmt.setLong(1, principalCents)
                pstmt.setLong(2, feeCents)
                pstmt.setLong(3, total)
                pstmt.setLong(4, total)
                pstmt.setLong(5, System.currentTimeMillis())
                pstmt.executeUpdate()
                val rs = pstmt.generatedKeys
                return if (rs.next()) rs.getLong(1) else 0L
            }
        }
    }

    fun repayLoan(id: Long, amountCents: Long) {
        getConnection().use { conn ->
            val selectSql = "SELECT remainingCents FROM merchant_loans WHERE id = ?"
            var rem = 0L
            conn.prepareStatement(selectSql).use { pstmt ->
                pstmt.setLong(1, id)
                val rs = pstmt.executeQuery()
                if (rs.next()) rem = rs.getLong("remainingCents")
            }
            val newRem = (rem - amountCents).coerceAtLeast(0L)
            val newStatus = if (newRem == 0L) "Repaid" else "Active"
            val updateSql = "UPDATE merchant_loans SET remainingCents = ?, status = ? WHERE id = ?"
            conn.prepareStatement(updateSql).use { pstmt ->
                pstmt.setLong(1, newRem)
                pstmt.setString(2, newStatus)
                pstmt.setLong(3, id)
                pstmt.executeUpdate()
            }
        }
    }

    // --- Staff Members ---
    fun getAllStaff(): List<Staff> {
        val list = mutableListOf<Staff>()
        getConnection().use { conn ->
            val rs = conn.createStatement().executeQuery("SELECT * FROM staff_members ORDER BY name ASC")
            while (rs.next()) {
                list.add(
                    Staff(
                        id = rs.getLong("id"),
                        name = rs.getString("name"),
                        role = rs.getString("role"),
                        dailyWageCents = rs.getLong("dailyWageCents"),
                        phone = rs.getString("phone"),
                        active = rs.getInt("active") == 1,
                        synced = rs.getInt("synced") == 1
                    )
                )
            }
        }
        return list
    }

    fun upsertStaff(staff: Staff) {
        getConnection().use { conn ->
            if (staff.id == 0L) {
                val sql = "INSERT INTO staff_members (name, role, dailyWageCents, phone, active, synced) VALUES (?, ?, ?, ?, ?, ?)"
                conn.prepareStatement(sql).use { pstmt ->
                    pstmt.setString(1, staff.name)
                    pstmt.setString(2, staff.role)
                    pstmt.setLong(3, staff.dailyWageCents)
                    pstmt.setString(4, staff.phone)
                    pstmt.setInt(5, if (staff.active) 1 else 0)
                    pstmt.setInt(6, if (staff.synced) 1 else 0)
                    pstmt.executeUpdate()
                }
            } else {
                val sql = "UPDATE staff_members SET name = ?, role = ?, dailyWageCents = ?, phone = ?, active = ?, synced = ? WHERE id = ?"
                conn.prepareStatement(sql).use { pstmt ->
                    pstmt.setString(1, staff.name)
                    pstmt.setString(2, staff.role)
                    pstmt.setLong(3, staff.dailyWageCents)
                    pstmt.setString(4, staff.phone)
                    pstmt.setInt(5, if (staff.active) 1 else 0)
                    pstmt.setInt(6, if (staff.synced) 1 else 0)
                    pstmt.setLong(7, staff.id)
                    pstmt.executeUpdate()
                }
            }
        }
    }

    fun deleteStaff(id: Long) {
        getConnection().use { conn ->
            val pstmt = conn.prepareStatement("DELETE FROM staff_members WHERE id = ?")
            pstmt.setLong(1, id)
            pstmt.executeUpdate()
        }
    }

    // --- WhatsApp Logs ---
    fun getAllWhatsAppLogs(): List<WhatsAppShareLog> {
        val list = mutableListOf<WhatsAppShareLog>()
        getConnection().use { conn ->
            val rs = conn.createStatement().executeQuery("SELECT * FROM whatsapp_share_logs ORDER BY sharedAt DESC")
            while (rs.next()) {
                list.add(
                    WhatsAppShareLog(
                        id = rs.getLong("id"),
                        title = rs.getString("title"),
                        summary = rs.getString("summary"),
                        sharedAt = rs.getLong("sharedAt"),
                        synced = rs.getInt("synced") == 1
                    )
                )
            }
        }
        return list
    }

    fun insertWhatsAppLog(title: String, summary: String): Long {
        getConnection().use { conn ->
            val sql = "INSERT INTO whatsapp_share_logs (title, summary, sharedAt, synced) VALUES (?, ?, ?, 0)"
            conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { pstmt ->
                pstmt.setString(1, title)
                pstmt.setString(2, summary)
                pstmt.setLong(3, System.currentTimeMillis())
                pstmt.executeUpdate()
                val rs = pstmt.generatedKeys
                return if (rs.next()) rs.getLong(1) else 0L
            }
        }
    }

    fun deleteWhatsAppLog(id: Long) {
        getConnection().use { conn ->
            val pstmt = conn.prepareStatement("DELETE FROM whatsapp_share_logs WHERE id = ?")
            pstmt.setLong(1, id)
            pstmt.executeUpdate()
        }
    }

    // --- Knowledge Docs ---
    fun getAllKnowledgeDocs(): List<KnowledgeDoc> {
        val list = mutableListOf<KnowledgeDoc>()
        getConnection().use { conn ->
            val rs = conn.createStatement().executeQuery("SELECT * FROM knowledge_docs ORDER BY updatedAt DESC")
            while (rs.next()) {
                list.add(
                    KnowledgeDoc(
                        id = rs.getLong("id"),
                        title = rs.getString("title"),
                        content = rs.getString("content"),
                        category = rs.getString("category"),
                        updatedAt = rs.getLong("updatedAt")
                    )
                )
            }
        }
        return list
    }

    fun upsertKnowledgeDoc(doc: KnowledgeDoc) {
        getConnection().use { conn ->
            if (doc.id == 0L) {
                val sql = "INSERT INTO knowledge_docs (title, content, category, updatedAt) VALUES (?, ?, ?, ?)"
                conn.prepareStatement(sql).use { pstmt ->
                    pstmt.setString(1, doc.title)
                    pstmt.setString(2, doc.content)
                    pstmt.setString(3, doc.category)
                    pstmt.setLong(4, System.currentTimeMillis())
                    pstmt.executeUpdate()
                }
            } else {
                val sql = "UPDATE knowledge_docs SET title = ?, content = ?, category = ?, updatedAt = ? WHERE id = ?"
                conn.prepareStatement(sql).use { pstmt ->
                    pstmt.setString(1, doc.title)
                    pstmt.setString(2, doc.content)
                    pstmt.setString(3, doc.category)
                    pstmt.setLong(4, System.currentTimeMillis())
                    pstmt.setLong(5, doc.id)
                    pstmt.executeUpdate()
                }
            }
        }
    }
}
