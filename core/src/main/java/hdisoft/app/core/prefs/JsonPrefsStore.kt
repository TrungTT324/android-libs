package hdisoft.app.core.prefs

import android.content.Context
import android.util.Base64

/**
 * For a handful of small records (not a growing log - that belongs in a real database)
 * that still need a JSON shape rather than flat key/value pairs: stores one JSON string
 * per key, Base64-encoded, inside a [PrefsStore]-backed SharedPreferences file. The
 * caller owns the JSON schema (serializes/parses its own data class to/from a JSON
 * string, e.g. via org.json); this only handles the storage plumbing, following the same
 * "declare a file name and keys" pattern as [PrefsStore] itself.
 */
class JsonPrefsStore(context: Context, name: String) {

    private val prefs = PrefsStore(context, name)

    fun saveJson(key: String, json: String) {
        val encoded = Base64.encodeToString(json.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        prefs.putString(key, encoded)
    }

    fun loadJson(key: String): String? {
        val encoded = prefs.getString(key) ?: return null
        return try {
            String(Base64.decode(encoded, Base64.NO_WRAP), Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    fun clear(key: String) {
        prefs.remove(key)
    }
}
