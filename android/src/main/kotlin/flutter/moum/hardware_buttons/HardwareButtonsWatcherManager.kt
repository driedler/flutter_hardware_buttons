package flutter.moum.hardware_buttons

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.VolumeProviderCompat

enum class VolumeButton(val direction: Int) {
    VOLUME_UP(1),
    VOLUME_DOWN(-1),
}

/*
Singleton object for managing various resources related with getting hardware button events.
Those who need to listen to any hardware button events add listener to this single instance.
e.g. HardwareButtonsWatcherManager.getInstance(application).addVolumeButtonListener(volumeButtonListener)
*/

class HardwareButtonsWatcherManager {

    companion object {
        private val INSTANCE: HardwareButtonsWatcherManager by lazy { HardwareButtonsWatcherManager() }

        fun getInstance(application: Application): HardwareButtonsWatcherManager {
            val instance = INSTANCE
            instance.mApplication = application
            return instance
        }
    }

    private var mApplication: Application? = null

    private var mMediaSession: MediaSessionCompat? = null
    private var mVolumeButtonListeners: ArrayList<VolumeButtonListener> = arrayListOf()

    private var mHomeButtonWatcher: HomeButtonWatcher? = null
    private var mHomeButtonListeners: ArrayList<HomeButtonListener> = arrayListOf()

    private var mScreenOffWatcher: ScreenOffWatcher? = null
    private var mLockButtonListeners: ArrayList<LockButtonListener> = arrayListOf()

    fun addVolumeButtonListener(listener: VolumeButtonListener) {
        if (!mVolumeButtonListeners.contains(listener)) {
            mVolumeButtonListeners.add(listener)
        }
        attachVolumeButtonWatcherIfNeeded()
    }

    fun addHomeButtonListener(listener: HomeButtonListener) {
        if (!mHomeButtonListeners.contains(listener)) {
            mHomeButtonListeners.add(listener)
        }
        attachHomeButtonWatcherIfNeeded()
    }

    fun addLockButtonListener(listener: LockButtonListener) {
        if (!mLockButtonListeners.contains(listener)) {
            mLockButtonListeners.add(listener)
        }
        attachScreenOffWatcherIfNeeded()
    }

    fun removeVolumeButtonListener(listener: VolumeButtonListener) {
        mVolumeButtonListeners.remove(listener)
        if (mVolumeButtonListeners.size == 0) {
            detachVolumeButtonWatcher()
        }
    }

    fun removeHomeButtonListener(listener: HomeButtonListener) {
        mHomeButtonListeners.remove(listener)
        if (mHomeButtonListeners.size == 0) {
            detachHomeButtonWatcher()
        }
    }

    fun removeLockButtonListener(listener: LockButtonListener) {
        mLockButtonListeners.remove(listener)
        if (mLockButtonListeners.size == 0) {
            detachScreenOffWatcher()
        }
    }

    private fun attachVolumeButtonWatcherIfNeeded() {
        val application = mApplication ?: return
        if (mVolumeButtonListeners.size > 0 && mMediaSession == null) {
            mMediaSession = MediaSessionCompat(application.applicationContext, "DetectVolumeService")
            mMediaSession?.setPlaybackState(PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING, 0, 0f) //you simulate a player which plays something.
                    .build())

            // This will only work on Lollipop and up, see https://code.google.com/p/android/issues/detail?id=224134
            val volumeProvider = object : VolumeProviderCompat(VOLUME_CONTROL_RELATIVE, /*max volume*/100, /*initial volume level*/50) {
                override fun onAdjustVolume(direction: Int) {
                    if (direction == 0) return
                    dispatchVolumeButtonEvent(direction)
                }
            }
            mMediaSession?.setPlaybackToRemote(volumeProvider)
            mMediaSession?.isActive = true
        }
    }

    private fun attachHomeButtonWatcherIfNeeded() {
        val application = mApplication ?: return
        if (mHomeButtonListeners.size > 0 && mHomeButtonWatcher == null) {
            mHomeButtonWatcher = HomeButtonWatcher {
                dispatchHomeButtonEvent()
            }
            val intentFilter = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            application.registerReceiver(mHomeButtonWatcher, intentFilter)
        }
    }

    private fun attachScreenOffWatcherIfNeeded() {
        val application = mApplication ?: return
        if (mLockButtonListeners.size > 0 && mScreenOffWatcher == null) {
            mScreenOffWatcher = ScreenOffWatcher {
                if (it == ScreenOffWatcher.REASON_POWER_BUTTON) {
                    dispatchLockButtonEvent()
                }
                detachScreenOffWatcher()
            }
            val intentFilter = IntentFilter(Intent.ACTION_SCREEN_OFF)
            application.registerReceiver(mScreenOffWatcher, intentFilter)
        }
    }

    private fun detachVolumeButtonWatcher() {
        mMediaSession?.apply {
            isActive = false
            release()
        }
        mMediaSession = null
    }

    private fun detachHomeButtonWatcher() {
        val application = mApplication ?: return
        val homeButtonWatcher = mHomeButtonWatcher ?: return
        application.unregisterReceiver(homeButtonWatcher)
        mHomeButtonWatcher = null
    }

    private fun detachScreenOffWatcher() {
        val application = mApplication ?: return
        val screenOffWatcher = mScreenOffWatcher ?: return
        application.unregisterReceiver(screenOffWatcher)
        this.mScreenOffWatcher = null
    }

    private fun dispatchVolumeButtonEvent(direction: Int) {
        val volumeButton = when (direction) {
            VolumeButton.VOLUME_UP.direction -> VolumeButton.VOLUME_UP
            else -> VolumeButton.VOLUME_DOWN
        }
        for (listener in mVolumeButtonListeners) {
            listener.onVolumeButtonEvent(volumeButton)
        }
    }

    private fun dispatchHomeButtonEvent() {
        for (listener in mHomeButtonListeners) {
            listener.onHomeButtonEvent()
        }
    }

    private fun dispatchLockButtonEvent() {
        for (listener in mLockButtonListeners) {
            listener.onLockButtonEvent()
        }
    }

    interface VolumeButtonListener {
        fun onVolumeButtonEvent(volumeButton: VolumeButton)
    }

    interface HomeButtonListener {
        fun onHomeButtonEvent()
    }

    interface LockButtonListener {
        fun onLockButtonEvent()
    }
}

private class HomeButtonWatcher(private val callback: () -> Unit) : BroadcastReceiver() {
    companion object {
        private const val KEY_REASON = "reason"
        private const val REASON_HOME_KEY = "homekey"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val intent = intent ?: return
        if (intent.action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
            if (intent.getStringExtra(KEY_REASON) == REASON_HOME_KEY) {
                callback()
            }
        }
    }
}

private class ScreenOffWatcher(private val callback: (reason: Int) -> Unit) : BroadcastReceiver() {
    companion object {
        private const val KEY_REASON = "reason"
        // same value as PowerManager.GO_TO_SLEEP_REASON_POWER_BUTTON, but since it's @hide, we can't access it.
        const val REASON_POWER_BUTTON = 4
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val intent = intent ?: return
        if (intent.action == Intent.ACTION_SCREEN_OFF) {
            callback(intent.getIntExtra(KEY_REASON, -1))
        }
    }
}