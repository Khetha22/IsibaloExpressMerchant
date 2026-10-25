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

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1, "3b9b025f3a1c06aca9f8a0c637b7806e", "dea67f6afe6cff88824b886f836dec5a") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `stock_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `quantity` INTEGER NOT NULL, `lowStockThreshold` INTEGER NOT NULL, `costCents` INTEGER NOT NULL, `priceCents` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `synced` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `sales` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `totalCents` INTEGER NOT NULL, `method` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `synced` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `sale_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `saleId` INTEGER NOT NULL, `stockItemId` INTEGER, `name` TEXT NOT NULL, `qty` INTEGER NOT NULL, `unitPriceCents` INTEGER NOT NULL, FOREIGN KEY(`saleId`) REFERENCES `sales`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_sale_items_saleId` ON `sale_items` (`saleId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '3b9b025f3a1c06aca9f8a0c637b7806e')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `stock_items`")
        connection.execSQL("DROP TABLE IF EXISTS `sales`")
        connection.execSQL("DROP TABLE IF EXISTS `sale_items`")
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
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "stock_items", "sales", "sale_items")
  }

  public override fun clearAllTables() {
    super.performClear(true, "stock_items", "sales", "sale_items")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(StockDao::class, StockDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(SalesDao::class, SalesDao_Impl.getRequiredConverters())
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
}
