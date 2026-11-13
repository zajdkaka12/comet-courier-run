package com.cometcourier.run.data.local

import android.content.Context
import com.cometcourier.run.domain.model.PlayerProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SharedPreferencesStore(context: Context, private val codec: ProfileJsonCodec) {
    private val preferences = context.getSharedPreferences("courier_progress", Context.MODE_PRIVATE)

    suspend fun read(): PlayerProfile = withContext(Dispatchers.IO) {
        val raw = preferences.getString("profile", null) ?: return@withContext PlayerProfile()
        runCatching { codec.decode(raw) }.getOrElse {
            val backup = preferences.getString("backup", null) ?: throw it
            codec.decode(backup)
        }
    }

    suspend fun write(profile: PlayerProfile, previous: PlayerProfile?): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val editor = preferences.edit().putString("profile", codec.encode(profile))
            if (previous != null) editor.putString("backup", codec.encode(previous))
            editor.commit()
        }.getOrDefault(false)
    }
}
