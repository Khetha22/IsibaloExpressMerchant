package za.co.hbtechinc.isibalo.`data`

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
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
public class KnowledgeDao_Impl(
  __db: RoomDatabase,
) : KnowledgeDao {
  private val __db: RoomDatabase

  private val __deleteAdapterOfKnowledgeDoc: EntityDeleteOrUpdateAdapter<KnowledgeDoc>

  private val __upsertAdapterOfKnowledgeDoc: EntityUpsertAdapter<KnowledgeDoc>
  init {
    this.__db = __db
    this.__deleteAdapterOfKnowledgeDoc = object : EntityDeleteOrUpdateAdapter<KnowledgeDoc>() {
      protected override fun createQuery(): String = "DELETE FROM `knowledge_docs` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: KnowledgeDoc) {
        statement.bindLong(1, entity.id)
      }
    }
    this.__upsertAdapterOfKnowledgeDoc = EntityUpsertAdapter<KnowledgeDoc>(object : EntityInsertAdapter<KnowledgeDoc>() {
      protected override fun createQuery(): String = "INSERT INTO `knowledge_docs` (`id`,`title`,`content`,`category`,`updatedAt`) VALUES (nullif(?, 0),?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: KnowledgeDoc) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.content)
        statement.bindText(4, entity.category)
        statement.bindLong(5, entity.updatedAt)
      }
    }, object : EntityDeleteOrUpdateAdapter<KnowledgeDoc>() {
      protected override fun createQuery(): String = "UPDATE `knowledge_docs` SET `id` = ?,`title` = ?,`content` = ?,`category` = ?,`updatedAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: KnowledgeDoc) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.content)
        statement.bindText(4, entity.category)
        statement.bindLong(5, entity.updatedAt)
        statement.bindLong(6, entity.id)
      }
    })
  }

  public override suspend fun delete(doc: KnowledgeDoc): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfKnowledgeDoc.handle(_connection, doc)
  }

  public override suspend fun upsert(doc: KnowledgeDoc): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfKnowledgeDoc.upsert(_connection, doc)
  }

  public override fun observeAll(): Flow<List<KnowledgeDoc>> {
    val _sql: String = "SELECT * FROM knowledge_docs ORDER BY updatedAt DESC"
    return createFlow(__db, false, arrayOf("knowledge_docs")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfContent: Int = getColumnIndexOrThrow(_stmt, "content")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<KnowledgeDoc> = mutableListOf()
        while (_stmt.step()) {
          val _item: KnowledgeDoc
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpContent: String
          _tmpContent = _stmt.getText(_columnIndexOfContent)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _item = KnowledgeDoc(_tmpId,_tmpTitle,_tmpContent,_tmpCategory,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAll(): List<KnowledgeDoc> {
    val _sql: String = "SELECT * FROM knowledge_docs"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfContent: Int = getColumnIndexOrThrow(_stmt, "content")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<KnowledgeDoc> = mutableListOf()
        while (_stmt.step()) {
          val _item: KnowledgeDoc
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpContent: String
          _tmpContent = _stmt.getText(_columnIndexOfContent)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _item = KnowledgeDoc(_tmpId,_tmpTitle,_tmpContent,_tmpCategory,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun count(): Int {
    val _sql: String = "SELECT COUNT(*) FROM knowledge_docs"
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

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
