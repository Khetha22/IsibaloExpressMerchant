package za.co.hbtechinc.isibalo.`data`

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
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class CashUpDao_Impl(
  __db: RoomDatabase,
) : CashUpDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfCashUpRecord: EntityInsertAdapter<CashUpRecord>
  init {
    this.__db = __db
    this.__insertAdapterOfCashUpRecord = object : EntityInsertAdapter<CashUpRecord>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `cash_up_records` (`id`,`date`,`floatCents`,`expectedCashCents`,`actualCashCents`,`varianceCents`,`cardTotalCents`,`mobileTotalCents`,`notes`,`createdAt`,`synced`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: CashUpRecord) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.date)
        statement.bindLong(3, entity.floatCents)
        statement.bindLong(4, entity.expectedCashCents)
        statement.bindLong(5, entity.actualCashCents)
        statement.bindLong(6, entity.varianceCents)
        statement.bindLong(7, entity.cardTotalCents)
        statement.bindLong(8, entity.mobileTotalCents)
        statement.bindText(9, entity.notes)
        statement.bindLong(10, entity.createdAt)
        val _tmp: Int = if (entity.synced) 1 else 0
        statement.bindLong(11, _tmp.toLong())
      }
    }
  }

  public override suspend fun insert(record: CashUpRecord): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfCashUpRecord.insertAndReturnId(_connection, record)
    _result
  }

  public override fun observeAll(): Flow<List<CashUpRecord>> {
    val _sql: String = "SELECT * FROM cash_up_records ORDER BY createdAt DESC"
    return createFlow(__db, false, arrayOf("cash_up_records")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfFloatCents: Int = getColumnIndexOrThrow(_stmt, "floatCents")
        val _columnIndexOfExpectedCashCents: Int = getColumnIndexOrThrow(_stmt, "expectedCashCents")
        val _columnIndexOfActualCashCents: Int = getColumnIndexOrThrow(_stmt, "actualCashCents")
        val _columnIndexOfVarianceCents: Int = getColumnIndexOrThrow(_stmt, "varianceCents")
        val _columnIndexOfCardTotalCents: Int = getColumnIndexOrThrow(_stmt, "cardTotalCents")
        val _columnIndexOfMobileTotalCents: Int = getColumnIndexOrThrow(_stmt, "mobileTotalCents")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfSynced: Int = getColumnIndexOrThrow(_stmt, "synced")
        val _result: MutableList<CashUpRecord> = mutableListOf()
        while (_stmt.step()) {
          val _item: CashUpRecord
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDate: Long
          _tmpDate = _stmt.getLong(_columnIndexOfDate)
          val _tmpFloatCents: Long
          _tmpFloatCents = _stmt.getLong(_columnIndexOfFloatCents)
          val _tmpExpectedCashCents: Long
          _tmpExpectedCashCents = _stmt.getLong(_columnIndexOfExpectedCashCents)
          val _tmpActualCashCents: Long
          _tmpActualCashCents = _stmt.getLong(_columnIndexOfActualCashCents)
          val _tmpVarianceCents: Long
          _tmpVarianceCents = _stmt.getLong(_columnIndexOfVarianceCents)
          val _tmpCardTotalCents: Long
          _tmpCardTotalCents = _stmt.getLong(_columnIndexOfCardTotalCents)
          val _tmpMobileTotalCents: Long
          _tmpMobileTotalCents = _stmt.getLong(_columnIndexOfMobileTotalCents)
          val _tmpNotes: String
          _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpSynced: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfSynced).toInt()
          _tmpSynced = _tmp != 0
          _item = CashUpRecord(_tmpId,_tmpDate,_tmpFloatCents,_tmpExpectedCashCents,_tmpActualCashCents,_tmpVarianceCents,_tmpCardTotalCents,_tmpMobileTotalCents,_tmpNotes,_tmpCreatedAt,_tmpSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun latestSince(from: Long): CashUpRecord? {
    val _sql: String = "SELECT * FROM cash_up_records WHERE createdAt >= ? ORDER BY createdAt DESC LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, from)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfFloatCents: Int = getColumnIndexOrThrow(_stmt, "floatCents")
        val _columnIndexOfExpectedCashCents: Int = getColumnIndexOrThrow(_stmt, "expectedCashCents")
        val _columnIndexOfActualCashCents: Int = getColumnIndexOrThrow(_stmt, "actualCashCents")
        val _columnIndexOfVarianceCents: Int = getColumnIndexOrThrow(_stmt, "varianceCents")
        val _columnIndexOfCardTotalCents: Int = getColumnIndexOrThrow(_stmt, "cardTotalCents")
        val _columnIndexOfMobileTotalCents: Int = getColumnIndexOrThrow(_stmt, "mobileTotalCents")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfSynced: Int = getColumnIndexOrThrow(_stmt, "synced")
        val _result: CashUpRecord?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDate: Long
          _tmpDate = _stmt.getLong(_columnIndexOfDate)
          val _tmpFloatCents: Long
          _tmpFloatCents = _stmt.getLong(_columnIndexOfFloatCents)
          val _tmpExpectedCashCents: Long
          _tmpExpectedCashCents = _stmt.getLong(_columnIndexOfExpectedCashCents)
          val _tmpActualCashCents: Long
          _tmpActualCashCents = _stmt.getLong(_columnIndexOfActualCashCents)
          val _tmpVarianceCents: Long
          _tmpVarianceCents = _stmt.getLong(_columnIndexOfVarianceCents)
          val _tmpCardTotalCents: Long
          _tmpCardTotalCents = _stmt.getLong(_columnIndexOfCardTotalCents)
          val _tmpMobileTotalCents: Long
          _tmpMobileTotalCents = _stmt.getLong(_columnIndexOfMobileTotalCents)
          val _tmpNotes: String
          _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpSynced: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfSynced).toInt()
          _tmpSynced = _tmp != 0
          _result = CashUpRecord(_tmpId,_tmpDate,_tmpFloatCents,_tmpExpectedCashCents,_tmpActualCashCents,_tmpVarianceCents,_tmpCardTotalCents,_tmpMobileTotalCents,_tmpNotes,_tmpCreatedAt,_tmpSynced)
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
