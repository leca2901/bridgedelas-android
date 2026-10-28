package org.iascgo.rededelas

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import org.json.JSONObject

class ArtigosWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        // Mostra o que já está salvo e pede uma busca nova em segundo plano
        ids.forEach { desenhar(context, manager, it) }
        AtualizarWorker.agendar(context)
        AtualizarWorker.atualizarAgora(context)
    }

    override fun onEnabled(context: Context) = AtualizarWorker.agendar(context)

    override fun onDisabled(context: Context) = AtualizarWorker.cancelar(context)

    companion object {
        private val TITULOS = intArrayOf(R.id.artigo1_titulo, R.id.artigo2_titulo, R.id.artigo3_titulo)
        private val FONTES = intArrayOf(R.id.artigo1_fonte, R.id.artigo2_fonte, R.id.artigo3_fonte)
        private val BLOCOS = intArrayOf(R.id.artigo1, R.id.artigo2, R.id.artigo3)

        fun atualizarTodos(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, ArtigosWidget::class.java))
            ids.forEach { desenhar(context, manager, it) }
        }

        private fun abrir(context: Context, codigo: Int, intent: Intent): PendingIntent =
            PendingIntent.getActivity(
                context, codigo, intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        fun desenhar(context: Context, manager: AppWidgetManager, id: Int) {
            val v = RemoteViews(context.packageName, R.layout.widget_artigos)
            val json = Cache.ler(context)

            // Toques: 180 (discador), botão do mapa (site)
            v.setOnClickPendingIntent(R.id.alerta,
                abrir(context, 900, Intent(Intent.ACTION_DIAL, Uri.parse("tel:180"))))
            v.setOnClickPendingIntent(R.id.botao_mapa,
                abrir(context, 901, Intent(Intent.ACTION_VIEW, Uri.parse(Config.SITE_URL))))

            val artigos = try {
                JSONObject(json ?: "{}").optJSONArray("artigos")
            } catch (e: Exception) { null }

            for (i in 0 until Config.MAX_ARTIGOS) {
                val a = artigos?.optJSONObject(i)
                if (a == null) {
                    v.setViewVisibility(BLOCOS[i], View.GONE)
                    continue
                }
                v.setViewVisibility(BLOCOS[i], View.VISIBLE)
                v.setTextViewText(TITULOS[i], a.optString("titulo"))
                v.setTextViewText(FONTES[i], a.optString("fonte") + " · " + a.optString("data"))
                val url = a.optString("url", Config.SITE_URL)
                v.setOnClickPendingIntent(BLOCOS[i],
                    abrir(context, 100 + i, Intent(Intent.ACTION_VIEW, Uri.parse(url))))
            }
            v.setViewVisibility(R.id.vazio, if (artigos == null || artigos.length() == 0) View.VISIBLE else View.GONE)
            manager.updateAppWidget(id, v)
        }
    }
}

/** Guarda a última lista de artigos para o widget funcionar sem internet. */
object Cache {
    private const val PREFS = "rede_delas"
    private const val CHAVE = "artigos_json"
    fun ler(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(CHAVE, null)
    fun salvar(context: Context, json: String) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(CHAVE, json).apply()
}
