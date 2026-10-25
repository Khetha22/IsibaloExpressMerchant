package za.co.hbtechinc.isibalo.sync

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import za.co.hbtechinc.isibalo.data.SaleWithItems
import za.co.hbtechinc.isibalo.data.StockItem
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/** Pushes unsynced rows to hbtechinc.co.za (see server/isibalo/api/sync.php). Safe to retry. */
class HttpRemoteApi(
    private val ctx: Context,
    private val url: String,
    private val apiKey: String
) : RemoteApi {

    private fun deviceId(): String {
        val prefs = ctx.getSharedPreferences("sync", Context.MODE_PRIVATE)
        return prefs.getString("device", null)
            ?: UUID.randomUUID().toString().also { prefs.edit().putString("device", it).apply() }
    }

    override suspend fun push(sales: List<SaleWithItems>, stock: List<StockItem>): Boolean =
        withContext(Dispatchers.IO) {
            val salesJson = JSONArray()
            sales.forEach { s ->
                val items = JSONArray()
                s.items.forEach { i ->
                    items.put(
                        JSONObject()
                            .put("id", i.id)
                            .put("stock_item_id", i.stockItemId ?: JSONObject.NULL)
                            .put("name", i.name)
                            .put("qty", i.qty)
                            .put("unit_price_cents", i.unitPriceCents)
                    )
                }
                salesJson.put(
                    JSONObject()
                        .put("id", s.sale.id)
                        .put("total_cents", s.sale.totalCents)
                        .put("method", s.sale.method.name)
                        .put("created_at", s.sale.createdAt)
                        .put("items", items)
                )
            }
            val stockJson = JSONArray()
            stock.forEach {
                stockJson.put(
                    JSONObject()
                        .put("id", it.id).put("name", it.name).put("quantity", it.quantity)
                        .put("low_threshold", it.lowStockThreshold)
                        .put("cost_cents", it.costCents).put("price_cents", it.priceCents)
                        .put("updated_at", it.updatedAt)
                )
            }
            val body = JSONObject().put("device", deviceId())
                .put("sales", salesJson).put("stock", stockJson).toString()

            val c = URL(url).openConnection() as HttpURLConnection
            try {
                c.requestMethod = "POST"
                c.connectTimeout = 15_000
                c.readTimeout = 20_000
                c.doOutput = true
                c.setRequestProperty("Content-Type", "application/json")
                c.setRequestProperty("X-Api-Key", apiKey)
                c.outputStream.use { it.write(body.toByteArray()) }
                c.responseCode in 200..299
            } finally {
                c.disconnect()
            }
        }
}
