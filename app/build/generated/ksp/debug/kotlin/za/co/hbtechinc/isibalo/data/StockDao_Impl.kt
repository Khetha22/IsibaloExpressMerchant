package za.co.hbtechinc.isibalo.`data`

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlin.text.StringBuilder
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class StockDao_Impl(
  __db: RoomDatabase,
) : StockDao {
  private val __db: RoomDatabase

  private val __deleteAdapterOfStockItem: EntityDeleteOrUpdateAdapter<StockItem>

  private val __upsertAdapterOfStockItem: EntityUpsertAdapter<StockItem>
  init {
    this.__db = __db
    this.__deleteAdapterOfStockItem = object : EntityDeleteOrUpdateAdapter<StockItem>() {
      protected override fun createQuery(): String = "DELETE FROM `stock_items` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: StockItem) {
        statement.bindLong(1, entity.id)
      }
    }
    this.__upsertAdapterOfStockItem = EntityUpsertAdapter<StockItem>(object : EntityInsertAdapter<StockItem>() {
      protected override fun createQuery(): String = "INSERT INTO `stock_items` (`id`,`name`,`quantity`,`lowStockThreshold`,`costCents`,`priceCents`,`expiryDate`,`updatedAt`,`synced`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: StockItem) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindLong(3, entity.quantity.toLong())
        statement.bindLong(4, entity.lowStockThreshold.toLong())
        statement.bindLong(5, entity.costCents)
        statement.bindLong(6, entity.priceCents)
        statement.bindLong(7, entity.expiryDate)
        statement.bindLong(8, entity.updatedAt)
        val _tmp: Int = if (entity.synced) 1 else 0
        statement.bindLong(9, _tmp.toLong())
      }
    }, object : EntityDeleteOrUpdateAdapter<StockItem>() {
      protected override fun createQuery(): String = "UPDATE `stock_items` SET `id` = ?,`name` = ?,`quantity` = ?,`lowStockThreshold` = ?,`costCents` = ?,`priceCents` = ?,`expiryDate` = ?,`updatedAt` = ?,`synced` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: StockItem) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindLong(3, entity.quantity.toLong())
        statement.bindLong(4, entity.lowStockThreshold.toLong())
        statement.bindLong(5, entity.costCents)
        statement.bindLong(6, entity.priceCents)
        statement.bindLong(7, entity.expiryDate)
        statement.bindLong(8, entity.updatedAt)
        val _tmp: Int = if (entity.synced) 1 else 0
        statement.bindLong(9, _tmp.toLong())
        statement.bindLong(10, entity.id)
      }
    })
  }

  public override suspend fun delete(item: StockItem): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfStockItem.handle(_connection, item)
  }

  public override suspend fun upsert(item: StockItem): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfStockItem.upsert(_connection, item)
  }

  public override fun observeAll(): Flow<List<StockItem>> {
    val _sql: String = "SELECT * FROM stock_items ORDER BY name"
    return createFlow(__db, false, arrayOf("stock_items")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfQuantity: Int = getColumnIndexOrThrow(_stmt, "quantity")
        val _columnIndexOfLowStockThreshold: Int = getColumnIndexOrThrow(_stmt, "lowStockThreshold")
        val _columnIndexOfCostCents: Int = getColumnIndexOrThrow(_stmt, "costCents")
        val _columnIndexOfPriceCents: Int = getColumnIndexOrThrow(_stmt, "priceCents")
        val _columnIndexOfExpiryDate: Int = getColumnIndexOrThrow(_stmt, "expiryDate")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfSynced: Int = getColumnIndexOrThrow(_stmt, "synced")
        val _result: MutableList<StockItem> = mutableListOf()
        while (_stmt.step()) {
          val _item: StockItem
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpQuantity: Int
          _tmpQuantity = _stmt.getLong(_columnIndexOfQuantity).toInt()
          val _tmpLowStockThreshold: Int
          _tmpLowStockThreshold = _stmt.getLong(_columnIndexOfLowStockThreshold).toInt()
          val _tmpCostCents: Long
          _tmpCostCents = _stmt.getLong(_columnIndexOfCostCents)
          val _tmpPriceCents: Long
          _tmpPriceCents = _stmt.getLong(_columnIndexOfPriceCents)
          val _tmpExpiryDate: Long
          _tmpExpiryDate = _stmt.getLong(_columnIndexOfExpiryDate)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpSynced: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfSynced).toInt()
          _tmpSynced = _tmp != 0
          _item = StockItem(_tmpId,_tmpName,_tmpQuantity,_tmpLowStockThreshold,_tmpCostCents,_tmpPriceCents,_tmpExpiryDate,_tmpUpdatedAt,_tmpSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeLow(): Flow<List<StockItem>> {
    val _sql: String = "SELECT * FROM stock_items WHERE quantity <= lowStockThreshold ORDER BY quantity"
    return createFlow(__db, false, arrayOf("stock_items")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfQuantity: Int = getColumnIndexOrThrow(_stmt, "quantity")
        val _columnIndexOfLowStockThreshold: Int = getColumnIndexOrThrow(_stmt, "lowStockThreshold")
        val _columnIndexOfCostCents: Int = getColumnIndexOrThrow(_stmt, "costCents")
        val _columnIndexOfPriceCents: Int = getColumnIndexOrThrow(_stmt, "priceCents")
        val _columnIndexOfExpiryDate: Int = getColumnIndexOrThrow(_stmt, "expiryDate")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfSynced: Int = getColumnIndexOrThrow(_stmt, "synced")
        val _result: MutableList<StockItem> = mutableListOf()
        while (_stmt.step()) {
          val _item: StockItem
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpQuantity: Int
          _tmpQuantity = _stmt.getLong(_columnIndexOfQuantity).toInt()
          val _tmpLowStockThreshold: Int
          _tmpLowStockThreshold = _stmt.getLong(_columnIndexOfLowStockThreshold).toInt()
          val _tmpCostCents: Long
          _tmpCostCents = _stmt.getLong(_columnIndexOfCostCents)
          val _tmpPriceCents: Long
          _tmpPriceCents = _stmt.getLong(_columnIndexOfPriceCents)
          val _tmpExpiryDate: Long
          _tmpExpiryDate = _stmt.getLong(_columnIndexOfExpiryDate)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpSynced: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfSynced).toInt()
          _tmpSynced = _tmp != 0
          _item = StockItem(_tmpId,_tmpName,_tmpQuantity,_tmpLowStockThreshold,_tmpCostCents,_tmpPriceCents,_tmpExpiryDate,_tmpUpdatedAt,_tmpSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun count(): Int {
    val _sql: String = "SELECT COUNT(*) FROM stock_items"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun unsynced(): List<StockItem> {
    val _sql: String = "SELECT * FROM stock_items WHERE synced = 0"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfQuantity: Int = getColumnIndexOrThrow(_stmt, "quantity")
        val _columnIndexOfLowStockThreshold: Int = getColumnIndexOrThrow(_stmt, "lowStockThreshold")
        val _columnIndexOfCostCents: Int = getColumnIndexOrThrow(_stmt, "costCents")
        val _columnIndexOfPriceCents: Int = getColumnIndexOrThrow(_stmt, "priceCents")
        val _columnIndexOfExpiryDate: Int = getColumnIndexOrThrow(_stmt, "expiryDate")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfSynced: Int = getColumnIndexOrThrow(_stmt, "synced")
        val _result: MutableList<StockItem> = mutableListOf()
        while (_stmt.step()) {
          val _item: StockItem
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpQuantity: Int
          _tmpQuantity = _stmt.getLong(_columnIndexOfQuantity).toInt()
          val _tmpLowStockThreshold: Int
          _tmpLowStockThreshold = _stmt.getLong(_columnIndexOfLowStockThreshold).toInt()
          val _tmpCostCents: Long
          _tmpCostCents = _stmt.getLong(_columnIndexOfCostCents)
          val _tmpPriceCents: Long
          _tmpPriceCents = _stmt.getLong(_columnIndexOfPriceCents)
          val _tmpExpiryDate: Long
          _tmpExpiryDate = _stmt.getLong(_columnIndexOfExpiryDate)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpSynced: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfSynced).toInt()
          _tmpSynced = _tmp != 0
          _item = StockItem(_tmpId,_tmpName,_tmpQuantity,_tmpLowStockThreshold,_tmpCostCents,_tmpPriceCents,_tmpExpiryDate,_tmpUpdatedAt,_tmpSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun decrement(
    id: Long,
    qty: Int,
    now: Long,
  ) {
    val _sql: String = "UPDATE stock_items SET quantity = MAX(quantity - ?, 0), synced = 0, updatedAt = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, qty.toLong())
        _argIndex = 2
        _stmt.bindLong(_argIndex, now)
        _argIndex = 3
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun markSynced(ids: List<Long>) {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("UPDATE stock_items SET synced = 1 WHERE id IN (")
    val _inputSize: Int = ids.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        for (_item: Long in ids) {
          _stmt.bindLong(_argIndex, _item)
          _argIndex++
        }
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
