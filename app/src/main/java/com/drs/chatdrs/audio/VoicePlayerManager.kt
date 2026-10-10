package com.drs.chatdrs.audio

import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class VoicePlayerManager {

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    var currentPlayingPath: String? = null
        private set

    fun play(
        filePath: String,
        scope: CoroutineScope,
        onProgress: (currentMs: Int, totalMs: Int) -> Unit,
        onComplete: () -> Unit
    ) {
        stop()

        try {
            val player = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                start()
            }
            mediaPlayer = player
            currentPlayingPath = filePath

            val total = player.duration

            progressJob = scope.launch(Dispatchers.Main) {
                while (isActive && player.isPlaying) {
                    onProgress(player.currentPosition, total)
                    delay(80)
                }
            }

            player.setOnCompletionListener {
                stop()
                onComplete()
            }
        } catch (e: Exception) {
            Log.e("VoicePlayerManager", "Failed to play audio: $filePath", e)
            stop()
            onComplete()
        }
    }

    fun stop() {
        progressJob?.cancel()
        progressJob = null
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (e: Exception) {
            Log.w("VoicePlayerManager", "Error stopping player", e)
        } finally {
            mediaPlayer = null
            currentPlayingPath = null
        }
    }
}
