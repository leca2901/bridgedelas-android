package org.iascgo.rededelas

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle

/** Ao tocar no ícone do app, abre o site Rede Delas e já agenda a atualização do widget. */
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AtualizarWorker.agendar(this)
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Config.SITE_URL)))
        finish()
    }
}
