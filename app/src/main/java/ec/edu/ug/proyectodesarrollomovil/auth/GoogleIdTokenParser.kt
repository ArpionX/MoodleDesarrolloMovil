package ec.edu.ug.proyectodesarrollomovil.auth

import android.util.Base64
import org.json.JSONObject
import java.nio.charset.StandardCharsets

/**
 * El id_token es un JWT ya firmado y validado por Google/Credential Manager.
 * Alcanza con decodificar el payload (segundo segmento) para leer el email verificado,
 * sin necesidad de re-verificar la firma en el cliente.
 */
object GoogleIdTokenParser {

    fun extractEmail(idToken: String): String {
        val segments = idToken.split(".")
        require(segments.size == 3) { "El id_token de Google no tiene el formato JWT esperado" }

        val payloadJson = String(
            Base64.decode(segments[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING),
            StandardCharsets.UTF_8
        )
        val payload = JSONObject(payloadJson)

        return payload.optString("email").takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("El token de Google no incluye un correo verificado")
    }
}
