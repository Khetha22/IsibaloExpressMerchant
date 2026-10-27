package za.co.hbtechinc.isibalo.`data`

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
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
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class CustomerDebtDao_Impl(
  __db: RoomDatabase,
) : CustomerDebtDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfCustomerDebt: EntityInsertAdapter<CustomerDebt>

  private val __deleteAdapterOfCustomerDebt: EntityDeleteOrUpdateAdapter<CustomerDebt>
  init {
    this.__db = __db
    this.__insertAdapterOfCustomerDebt = object : EntityInsertAdapter<CustomerDebt>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `customer_debts` (`id`,`customerName`,`phone`,`amountCents`,`note`,`settled`,`createdAt`,`synced`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: CustomerDebt) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.customerName)
        statement.bindText(3, entity.phone)
        statement.bindLong(4, entity.amountCents)
        statement.bindText(5, entity.note)
        val _tmp: Int = if (entity.settled) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        statement.bindLong(7, entity.createdAt)
        val _tmp_1: Int = if (entity.synced) 1 else 0
        statement.bindLong(8, _tmp_1.toLong())
      }
    }
    this.__deleteAdapterOfCustomerDebt = object : EntityDeleteOrUpdateAdapter<CustomerDebt>() {
      protected override fun createQuery(): String = "DELETE FROM `customer_debts` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: CustomerDebt) {
        statement.bindLong(1, entity.id)
      }
    }
  }

  public override suspend fun insert(debt: CustomerDebt): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfCustomerDebt.insertAndReturnId(_connection, debt)
    _result
  }

  public override suspend fun delete(debt: CustomerDebt): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfCustomerDebt.handle(_connection, debt)
  }

  public override fun observeAll(): Flow<List<CustomerDebt>> {
    val _sql: String = "SELECT * FROM customer_debts ORDER BY settled ASC, createdAt DESC"
    return createFlow(__db, false, arrayOf("customer_debts")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfCustomerName: Int = getColumnIndexOrThrow(_stmt, "customerName")
        val _columnIndexOfPhone: Int = getColumnIndexOrThrow(_stmt, "phone")
        val _columnIndexOfAmountCents: Int = getColumnIndexOrThrow(_stmt, "amountCents")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfSettled: Int = getColumnIndexOrThrow(_stmt, "settled")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfSynced: Int = getColumnIndexOrThrow(_stmt, "synced")
        val _result: MutableList<CustomerDebt> = mutableListOf()
        while (_stmt.step()) {
          val _item: CustomerDebt
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpCustomerName: String
          _tmpCustomerName = _stmt.getText(_columnIndexOfCustomerName)
          val _tmpPhone: String
          _tmpPhone = _stmt.getText(_columnIndexOfPhone)
          val _tmpAmountCents: Long
          _tmpAmountCents = _stmt.getLong(_columnIndexOfAmountCents)
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          val _tmpSettled: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfSettled).toInt()
          _tmpSettled = _tmp != 0
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpSynced: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfSynced).toInt()
          _tmpSynced = _tmp_1 != 0
          _item = CustomerDebt(_tmpId,_tmpCustomerName,_tmpPhone,_tmpAmountCents,_tmpNote,_tmpSettled,_tmpCreatedAt,_tmpSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun settle(id: Long) {
    val _sql: String = "UPDATE customer_debts SET settled = 1 WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
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
