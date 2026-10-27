package za.co.hbtechinc.isibalo.`data`

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _stockDao: Lazy<StockDao> = lazy {
    StockDao_Impl(this)
  }

  private val _salesDao: Lazy<SalesDao> = lazy {
    SalesDao_Impl(this)
  }

  private val _cashUpDao: Lazy<CashUpDao> = lazy {
    CashUpDao_Impl(this)
  }

  private val _knowledgeDao: Lazy<KnowledgeDao> = lazy {
    KnowledgeDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(2, "92dc359eb604de51b133d0e39f02d600", "bebaf64675ec78c0f8221316daca4953") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `stock_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `quantity` INTEGER NOT NULL, `lowStockThreshold` INTEGER NOT NULL, `costCents` INTEGER NOT NULL, `priceCents` INTEGER NOT NULL, `expiryDate` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `synced` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `sales` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `totalCents` INTEGER NOT NULL, `method` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `synced` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `sale_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `saleId` INTEGER NOT NULL, `stockItemId` INTEGER, `name` TEXT NOT NULL, `qty` INTEGER NOT NULL, `unitPriceCents` INTEGER NOT NULL, FOREIGN KEY(`saleId`) REFERENCES `sales`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_sale_items_saleId` ON `sale_items` (`saleId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `cash_up_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` INTEGER NOT NULL, `floatCents` INTEGER NOT NULL, `expectedCashCents` INTEGER NOT NULL, `actualCashCents` INTEGER NOT NULL, `varianceCents` INTEGER NOT NULL, `cardTotalCents` INTEGER NOT NULL, `mobileTotalCents` INTEGER NOT NULL, `notes` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `synced` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `knowledge_docs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `content` TEXT NOT NULL, `category` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '92dc359eb604de51b133d0e39f02d600')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `stock_items`")
        connection.execSQL("DROP TABLE IF EXISTS `sales`")
        connection.execSQL("DROP TABLE IF EXISTS `sale_items`")
        connection.execSQL("DROP TABLE IF EXISTS `cash_up_records`")
        connection.execSQL("DROP TABLE IF EXISTS `knowledge_docs`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA foreign_keys = ON")
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsStockItems: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsStockItems.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStockItems.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStockItems.put("quantity", TableInfo.Column("quantity", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStockItems.put("lowStockThreshold", TableInfo.Column("lowStockThreshold", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStockItems.put("costCents", TableInfo.Column("costCents", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStockItems.put("priceCents", TableInfo.Column("priceCents", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStockItems.put("expiryDate", TableInfo.Column("expiryDate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStockItems.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStockItems.put("synced", TableInfo.Column("synced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysStockItems: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesStockItems: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoStockItems: TableInfo = TableInfo("stock_items", _columnsStockItems, _foreignKeysStockItems, _indicesStockItems)
        val _existingStockItems: TableInfo = read(connection, "stock_items")
        if (!_infoStockItems.equals(_existingStockItems)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |stock_items(za.co.hbtechinc.isibalo.data.StockItem).
              | Expected:
              |""".trimMargin() + _infoStockItems + """
              |
              | Found:
              |""".trimMargin() + _existingStockItems)
        }
        val _columnsSales: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSales.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSales.put("totalCents", TableInfo.Column("totalCents", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSales.put("method", TableInfo.Column("method", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSales.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSales.put("synced", TableInfo.Column("synced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSales: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSales: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoSales: TableInfo = TableInfo("sales", _columnsSales, _foreignKeysSales, _indicesSales)
        val _existingSales: TableInfo = read(connection, "sales")
        if (!_infoSales.equals(_existingSales)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |sales(za.co.hbtechinc.isibalo.data.Sale).
              | Expected:
              |""".trimMargin() + _infoSales + """
              |
              | Found:
              |""".trimMargin() + _existingSales)
        }
        val _columnsSaleItems: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSaleItems.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSaleItems.put("saleId", TableInfo.Column("saleId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSaleItems.put("stockItemId", TableInfo.Column("stockItemId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSaleItems.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSaleItems.put("qty", TableInfo.Column("qty", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSaleItems.put("unitPriceCents", TableInfo.Column("unitPriceCents", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSaleItems: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysSaleItems.add(TableInfo.ForeignKey("sales", "CASCADE", "NO ACTION", listOf("saleId"), listOf("id")))
        val _indicesSaleItems: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesSaleItems.add(TableInfo.Index("index_sale_items_saleId", false, listOf("saleId"), listOf("ASC")))
        val _infoSaleItems: TableInfo = TableInfo("sale_items", _columnsSaleItems, _foreignKeysSaleItems, _indicesSaleItems)
        val _existingSaleItems: TableInfo = read(connection, "sale_items")
        if (!_infoSaleItems.equals(_existingSaleItems)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |sale_items(za.co.hbtechinc.isibalo.data.SaleItem).
              | Expected:
              |""".trimMargin() + _infoSaleItems + """
              |
              | Found:
              |""".trimMargin() + _existingSaleItems)
        }
        val _columnsCashUpRecords: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsCashUpRecords.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCashUpRecords.put("date", TableInfo.Column("date", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCashUpRecords.put("floatCents", TableInfo.Column("floatCents", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCashUpRecords.put("expectedCashCents", TableInfo.Column("expectedCashCents", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCashUpRecords.put("actualCashCents", TableInfo.Column("actualCashCents", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCashUpRecords.put("varianceCents", TableInfo.Column("varianceCents", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCashUpRecords.put("cardTotalCents", TableInfo.Column("cardTotalCents", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCashUpRecords.put("mobileTotalCents", TableInfo.Column("mobileTotalCents", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCashUpRecords.put("notes", TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCashUpRecords.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCashUpRecords.put("synced", TableInfo.Column("synced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysCashUpRecords: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesCashUpRecords: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoCashUpRecords: TableInfo = TableInfo("cash_up_records", _columnsCashUpRecords, _foreignKeysCashUpRecords, _indicesCashUpRecords)
        val _existingCashUpRecords: TableInfo = read(connection, "cash_up_records")
        if (!_infoCashUpRecords.equals(_existingCashUpRecords)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |cash_up_records(za.co.hbtechinc.isibalo.data.CashUpRecord).
              | Expected:
              |""".trimMargin() + _infoCashUpRecords + """
              |
              | Found:
              |""".trimMargin() + _existingCashUpRecords)
        }
        val _columnsKnowledgeDocs: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsKnowledgeDocs.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsKnowledgeDocs.put("title", TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsKnowledgeDocs.put("content", TableInfo.Column("content", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsKnowledgeDocs.put("category", TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsKnowledgeDocs.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysKnowledgeDocs: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesKnowledgeDocs: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoKnowledgeDocs: TableInfo = TableInfo("knowledge_docs", _columnsKnowledgeDocs, _foreignKeysKnowledgeDocs, _indicesKnowledgeDocs)
        val _existingKnowledgeDocs: TableInfo = read(connection, "knowledge_docs")
        if (!_infoKnowledgeDocs.equals(_existingKnowledgeDocs)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |knowledge_docs(za.co.hbtechinc.isibalo.data.KnowledgeDoc).
              | Expected:
              |""".trimMargin() + _infoKnowledgeDocs + """
              |
              | Found:
              |""".trimMargin() + _existingKnowledgeDocs)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "stock_items", "sales", "sale_items", "cash_up_records", "knowledge_docs")
  }

  public override fun clearAllTables() {
    super.performClear(true, "stock_items", "sales", "sale_items", "cash_up_records", "knowledge_docs")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(StockDao::class, StockDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(SalesDao::class, SalesDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(CashUpDao::class, CashUpDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(KnowledgeDao::class, KnowledgeDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun stockDao(): StockDao = _stockDao.value

  public override fun salesDao(): SalesDao = _salesDao.value

  public override fun cashUpDao(): CashUpDao = _cashUpDao.value

  public override fun knowledgeDao(): KnowledgeDao = _knowledgeDao.value
}
