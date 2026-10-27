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
public class PettyCashDao_Impl(
  __db: RoomDatabase,
) : PettyCashDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfPettyCash: EntityInsertAdapter<PettyCash>

  private val __deleteAdapterOfPettyCash: EntityDeleteOrUpdateAdapter<PettyCash>
  init {
    this.__db = __db
    this.__insertAdapterOfPettyCash = object : EntityInsertAdapter<PettyCash>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `petty_cash` (`id`,`recipient`,`amountCents`,`note`,`issuedAt`,`settled`,`synced`) VALUES (nullif(?, 0),?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: PettyCash) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.recipient)
        statement.bindLong(3, entity.amountCents)
        statement.bindText(4, entity.note)
        statement.bindLong(5, entity.issuedAt)
        val _tmp: Int = if (entity.settled) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        val _tmp_1: Int = if (entity.synced) 1 else 0
        statement.bindLong(7, _tmp_1.toLong())
      }
    }
    this.__deleteAdapterOfPettyCash = object : EntityDeleteOrUpdateAdapter<PettyCash>() {
      protected override fun createQuery(): String = "DELETE FROM `petty_cash` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: PettyCash) {
        statement.bindLong(1, entity.id)
      }
    }
  }

  public override suspend fun insert(record: PettyCash): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfPettyCash.insertAndReturnId(_connection, record)
    _result
  }

  public override suspend fun delete(record: PettyCash): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfPettyCash.handle(_connection, record)
  }

  public override fun observeAll(): Flow<List<PettyCash>> {
    val _sql: String = "SELECT * FROM petty_cash ORDER BY settled ASC, issuedAt DESC"
    return createFlow(__db, false, arrayOf("petty_cash")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRecipient: Int = getColumnIndexOrThrow(_stmt, "recipient")
        val _columnIndexOfAmountCents: Int = getColumnIndexOrThrow(_stmt, "amountCents")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfIssuedAt: Int = getColumnIndexOrThrow(_stmt, "issuedAt")
        val _columnIndexOfSettled: Int = getColumnIndexOrThrow(_stmt, "settled")
        val _columnIndexOfSynced: Int = getColumnIndexOrThrow(_stmt, "synced")
        val _result: MutableList<PettyCash> = mutableListOf()
        while (_stmt.step()) {
          val _item: PettyCash
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRecipient: String
          _tmpRecipient = _stmt.getText(_columnIndexOfRecipient)
          val _tmpAmountCents: Long
          _tmpAmountCents = _stmt.getLong(_columnIndexOfAmountCents)
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          val _tmpIssuedAt: Long
          _tmpIssuedAt = _stmt.getLong(_columnIndexOfIssuedAt)
          val _tmpSettled: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfSettled).toInt()
          _tmpSettled = _tmp != 0
          val _tmpSynced: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfSynced).toInt()
          _tmpSynced = _tmp_1 != 0
          _item = PettyCash(_tmpId,_tmpRecipient,_tmpAmountCents,_tmpNote,_tmpIssuedAt,_tmpSettled,_tmpSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun settle(id: Long) {
    val _sql: String = "UPDATE petty_cash SET settled = 1 WHERE id = ?"
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
