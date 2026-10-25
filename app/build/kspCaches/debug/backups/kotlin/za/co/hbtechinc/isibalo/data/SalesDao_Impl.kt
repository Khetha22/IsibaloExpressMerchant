package za.co.hbtechinc.isibalo.`data`

import androidx.collection.LongSparseArray
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndex
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.room.util.recursiveFetchLongSparseArray
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.IllegalArgumentException
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
public class SalesDao_Impl(
  __db: RoomDatabase,
) : SalesDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfSale: EntityInsertAdapter<Sale>

  private val __insertAdapterOfSaleItem: EntityInsertAdapter<SaleItem>
  init {
    this.__db = __db
    this.__insertAdapterOfSale = object : EntityInsertAdapter<Sale>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `sales` (`id`,`totalCents`,`method`,`createdAt`,`synced`) VALUES (nullif(?, 0),?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Sale) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.totalCents)
        statement.bindText(3, __PaymentMethod_enumToString(entity.method))
        statement.bindLong(4, entity.createdAt)
        val _tmp: Int = if (entity.synced) 1 else 0
        statement.bindLong(5, _tmp.toLong())
      }
    }
    this.__insertAdapterOfSaleItem = object : EntityInsertAdapter<SaleItem>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `sale_items` (`id`,`saleId`,`stockItemId`,`name`,`qty`,`unitPriceCents`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SaleItem) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.saleId)
        val _tmpStockItemId: Long? = entity.stockItemId
        if (_tmpStockItemId == null) {
          statement.bindNull(3)
        } else {
          statement.bindLong(3, _tmpStockItemId)
        }
        statement.bindText(4, entity.name)
        statement.bindLong(5, entity.qty.toLong())
        statement.bindLong(6, entity.unitPriceCents)
      }
    }
  }

  public override suspend fun insertSale(sale: Sale): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfSale.insertAndReturnId(_connection, sale)
    _result
  }

  public override suspend fun insertItems(items: List<SaleItem>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfSaleItem.insert(_connection, items)
  }

  public override fun observeSince(from: Long): Flow<List<Sale>> {
    val _sql: String = "SELECT * FROM sales WHERE createdAt >= ? ORDER BY createdAt DESC"
    return createFlow(__db, false, arrayOf("sales")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, from)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTotalCents: Int = getColumnIndexOrThrow(_stmt, "totalCents")
        val _columnIndexOfMethod: Int = getColumnIndexOrThrow(_stmt, "method")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfSynced: Int = getColumnIndexOrThrow(_stmt, "synced")
        val _result: MutableList<Sale> = mutableListOf()
        while (_stmt.step()) {
          val _item: Sale
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTotalCents: Long
          _tmpTotalCents = _stmt.getLong(_columnIndexOfTotalCents)
          val _tmpMethod: PaymentMethod
          _tmpMethod = __PaymentMethod_stringToEnum(_stmt.getText(_columnIndexOfMethod))
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpSynced: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfSynced).toInt()
          _tmpSynced = _tmp != 0
          _item = Sale(_tmpId,_tmpTotalCents,_tmpMethod,_tmpCreatedAt,_tmpSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeTotalsSince(from: Long): Flow<List<MethodTotal>> {
    val _sql: String = "SELECT method, SUM(totalCents) AS total FROM sales WHERE createdAt >= ? GROUP BY method"
    return createFlow(__db, false, arrayOf("sales")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, from)
        val _columnIndexOfMethod: Int = 0
        val _columnIndexOfTotal: Int = 1
        val _result: MutableList<MethodTotal> = mutableListOf()
        while (_stmt.step()) {
          val _item: MethodTotal
          val _tmpMethod: PaymentMethod
          _tmpMethod = __PaymentMethod_stringToEnum(_stmt.getText(_columnIndexOfMethod))
          val _tmpTotal: Long
          _tmpTotal = _stmt.getLong(_columnIndexOfTotal)
          _item = MethodTotal(_tmpMethod,_tmpTotal)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observePending(): Flow<Int> {
    val _sql: String = "SELECT COUNT(*) FROM sales WHERE synced = 0"
    return createFlow(__db, false, arrayOf("sales")) { _connection ->
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

  public override suspend fun topSellers(from: Long): List<TopSeller> {
    val _sql: String = "SELECT i.name AS name, SUM(i.qty) AS qty, SUM(i.qty * i.unitPriceCents) AS revenue FROM sale_items i JOIN sales s ON s.id = i.saleId WHERE s.createdAt >= ? GROUP BY i.name ORDER BY qty DESC LIMIT 5"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, from)
        val _columnIndexOfName: Int = 0
        val _columnIndexOfQty: Int = 1
        val _columnIndexOfRevenue: Int = 2
        val _result: MutableList<TopSeller> = mutableListOf()
        while (_stmt.step()) {
          val _item: TopSeller
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpQty: Int
          _tmpQty = _stmt.getLong(_columnIndexOfQty).toInt()
          val _tmpRevenue: Long
          _tmpRevenue = _stmt.getLong(_columnIndexOfRevenue)
          _item = TopSeller(_tmpName,_tmpQty,_tmpRevenue)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun unsynced(): List<SaleWithItems> {
    val _sql: String = "SELECT * FROM sales WHERE synced = 0"
    return performSuspending(__db, true, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTotalCents: Int = getColumnIndexOrThrow(_stmt, "totalCents")
        val _columnIndexOfMethod: Int = getColumnIndexOrThrow(_stmt, "method")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfSynced: Int = getColumnIndexOrThrow(_stmt, "synced")
        val _collectionItems: LongSparseArray<MutableList<SaleItem>> = LongSparseArray<MutableList<SaleItem>>()
        while (_stmt.step()) {
          val _tmpKey: Long
          _tmpKey = _stmt.getLong(_columnIndexOfId)
          if (!_collectionItems.containsKey(_tmpKey)) {
            _collectionItems.put(_tmpKey, mutableListOf())
          }
        }
        _stmt.reset()
        __fetchRelationshipsaleItemsAszaCoHbtechincIsibaloDataSaleItem(_connection, _collectionItems)
        val _result: MutableList<SaleWithItems> = mutableListOf()
        while (_stmt.step()) {
          val _item: SaleWithItems
          val _tmpSale: Sale
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTotalCents: Long
          _tmpTotalCents = _stmt.getLong(_columnIndexOfTotalCents)
          val _tmpMethod: PaymentMethod
          _tmpMethod = __PaymentMethod_stringToEnum(_stmt.getText(_columnIndexOfMethod))
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpSynced: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfSynced).toInt()
          _tmpSynced = _tmp != 0
          _tmpSale = Sale(_tmpId,_tmpTotalCents,_tmpMethod,_tmpCreatedAt,_tmpSynced)
          val _tmpItemsCollection: MutableList<SaleItem>
          val _tmpKey_1: Long
          _tmpKey_1 = _stmt.getLong(_columnIndexOfId)
          _tmpItemsCollection = checkNotNull(_collectionItems.get(_tmpKey_1))
          _item = SaleWithItems(_tmpSale,_tmpItemsCollection)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun markSynced(ids: List<Long>) {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("UPDATE sales SET synced = 1 WHERE id IN (")
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

  private fun __PaymentMethod_enumToString(_value: PaymentMethod): String = when (_value) {
    PaymentMethod.CASH -> "CASH"
    PaymentMethod.CARD -> "CARD"
    PaymentMethod.MOBILE -> "MOBILE"
  }

  private fun __PaymentMethod_stringToEnum(_value: String): PaymentMethod = when (_value) {
    "CASH" -> PaymentMethod.CASH
    "CARD" -> PaymentMethod.CARD
    "MOBILE" -> PaymentMethod.MOBILE
    else -> throw IllegalArgumentException("Can't convert value to enum, unknown value: " + _value)
  }

  private fun __fetchRelationshipsaleItemsAszaCoHbtechincIsibaloDataSaleItem(_connection: SQLiteConnection, _map: LongSparseArray<MutableList<SaleItem>>) {
    if (_map.isEmpty()) {
      return
    }
    if (_map.size() > 999) {
      recursiveFetchLongSparseArray(_map, true) { _tmpMap ->
        __fetchRelationshipsaleItemsAszaCoHbtechincIsibaloDataSaleItem(_connection, _tmpMap)
      }
      return
    }
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT `id`,`saleId`,`stockItemId`,`name`,`qty`,`unitPriceCents` FROM `sale_items` WHERE `saleId` IN (")
    val _inputSize: Int = _map.size()
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    val _stmt: SQLiteStatement = _connection.prepare(_sql)
    var _argIndex: Int = 1
    for (i in 0 until _map.size()) {
      val _item: Long = _map.keyAt(i)
      _stmt.bindLong(_argIndex, _item)
      _argIndex++
    }
    try {
      val _itemKeyIndex: Int = getColumnIndex(_stmt, "saleId")
      if (_itemKeyIndex == -1) {
        return
      }
      val _columnIndexOfId: Int = 0
      val _columnIndexOfSaleId: Int = 1
      val _columnIndexOfStockItemId: Int = 2
      val _columnIndexOfName: Int = 3
      val _columnIndexOfQty: Int = 4
      val _columnIndexOfUnitPriceCents: Int = 5
      while (_stmt.step()) {
        val _tmpKey: Long
        _tmpKey = _stmt.getLong(_itemKeyIndex)
        val _tmpRelation: MutableList<SaleItem>? = _map.get(_tmpKey)
        if (_tmpRelation != null) {
          val _item_1: SaleItem
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpSaleId: Long
          _tmpSaleId = _stmt.getLong(_columnIndexOfSaleId)
          val _tmpStockItemId: Long?
          if (_stmt.isNull(_columnIndexOfStockItemId)) {
            _tmpStockItemId = null
          } else {
            _tmpStockItemId = _stmt.getLong(_columnIndexOfStockItemId)
          }
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpQty: Int
          _tmpQty = _stmt.getLong(_columnIndexOfQty).toInt()
          val _tmpUnitPriceCents: Long
          _tmpUnitPriceCents = _stmt.getLong(_columnIndexOfUnitPriceCents)
          _item_1 = SaleItem(_tmpId,_tmpSaleId,_tmpStockItemId,_tmpName,_tmpQty,_tmpUnitPriceCents)
          _tmpRelation.add(_item_1)
        }
      }
    } finally {
      _stmt.close()
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
