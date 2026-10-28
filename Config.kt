package org.iascgo.rededelas

/**
 * TROQUE AQUI pelo endereço onde o site foi publicado (ex.: Netlify).
 * O widget lê os artigos de SITE_URL + "widgets/artigos-data.json",
 * o mesmo arquivo usado pelo site e pelo widget do Windows e do iPhone.
 */
object Config {
    const val SITE_URL = "https://rede-delas.netlify.app/"
    const val DADOS_URL = SITE_URL + "widgets/artigos-data.json"
    const val MAX_ARTIGOS = 3
}
