package org.iascgo.rededelas

import android.content.Context
import androidx.work.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

/** Busca os artigos no site a cada 6 horas (e quando o widget é adicionado). */
class AtualizarWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result = try {
        val con = URL(Config.DADOS_URL).openConnection() as HttpURLConnection
        con.connectTimeout = 15000
        con.readTimeout = 15000
        con.setRequestProperty("Cache-Control", "no-cache")
        val texto = con.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        con.disconnect()
        JSONObject(texto) // valida antes de salvar
        Cache.salvar(applicationContext, texto)
        ArtigosWidget.atualizarTodos(applicationContext)
        Result.success()
    } catch (e: Exception) {
        Result.retry()
    }

    companion object {
        private const val NOME = "atualizar_artigos"
        private val rede = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        fun agendar(context: Context) {
            val pedido = PeriodicWorkRequestBuilder<AtualizarWorker>(6, TimeUnit.HOURS)
                .setConstraints(rede).build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(NOME, ExistingPeriodicWorkPolicy.KEEP, pedido)
        }

        fun atualizarAgora(context: Context) {
            val pedido = OneTimeWorkRequestBuilder<AtualizarWorker>().setConstraints(rede).build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(NOME + "_agora", ExistingWorkPolicy.REPLACE, pedido)
        }

        fun cancelar(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(NOME)
        }
    }
}
