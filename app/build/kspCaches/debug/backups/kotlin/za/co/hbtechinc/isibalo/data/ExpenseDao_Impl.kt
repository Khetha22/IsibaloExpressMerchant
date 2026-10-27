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
public class ExpenseDao_Impl(
  __db: RoomDatabase,
) : ExpenseDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfExpense: EntityInsertAdapter<Expense>

  private val __deleteAdapterOfExpense: EntityDeleteOrUpdateAdapter<Expense>
  init {
    this.__db = __db
    this.__insertAdapterOfExpense = object : EntityInsertAdapter<Expense>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `expenses` (`id`,`title`,`amountCents`,`category`,`createdAt`,`synced`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Expense) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindLong(3, entity.amountCents)
        statement.bindText(4, entity.category)
        statement.bindLong(5, entity.createdAt)
        val _tmp: Int = if (entity.synced) 1 else 0
        statement.bindLong(6, _tmp.toLong())
      }
    }
    this.__deleteAdapterOfExpense = object : EntityDeleteOrUpdateAdapter<Expense>() {
      protected override fun createQuery(): String = "DELETE FROM `expenses` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Expense) {
        statement.bindLong(1, entity.id)
      }
    }
  }

  public override suspend fun insert(expense: Expense): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfExpense.insertAndReturnId(_connection, expense)
    _result
  }

  public override suspend fun delete(expense: Expense): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfExpense.handle(_connection, expense)
  }

  public override fun observeAll(): Flow<List<Expense>> {
    val _sql: String = "SELECT * FROM expenses ORDER BY createdAt DESC"
    return createFlow(__db, false, arrayOf("expenses")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfAmountCents: Int = getColumnIndexOrThrow(_stmt, "amountCents")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfSynced: Int = getColumnIndexOrThrow(_stmt, "synced")
        val _result: MutableList<Expense> = mutableListOf()
        while (_stmt.step()) {
          val _item: Expense
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpAmountCents: Long
          _tmpAmountCents = _stmt.getLong(_columnIndexOfAmountCents)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpSynced: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfSynced).toInt()
          _tmpSynced = _tmp != 0
          _item = Expense(_tmpId,_tmpTitle,_tmpAmountCents,_tmpCategory,_tmpCreatedAt,_tmpSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeTotalSince(from: Long): Flow<Long?> {
    val _sql: String = "SELECT SUM(amountCents) FROM expenses WHERE createdAt >= ?"
    return createFlow(__db, false, arrayOf("expenses")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, from)
        val _result: Long?
        if (_stmt.step()) {
          val _tmp: Long?
          if (_stmt.isNull(0)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(0)
          }
          _result = _tmp
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
