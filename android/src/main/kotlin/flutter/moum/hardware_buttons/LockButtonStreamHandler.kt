package flutter.moum.hardware_buttons

import android.app.Application
import io.flutter.plugin.common.EventChannel

class LockButtonStreamHandler(private val application: Application) : EventChannel.StreamHandler {
    private var mStreamSink: EventChannel.EventSink? = null

    private val lockButtonListener = object : HardwareButtonsWatcherManager.LockButtonListener {
        override fun onLockButtonEvent() {
            mStreamSink?.success(0)
        }
    }

    override fun onListen(args: Any?, sink: EventChannel.EventSink?) {
        this.mStreamSink = sink
        HardwareButtonsWatcherManager.getInstance(application).addLockButtonListener(lockButtonListener)
    }

    override fun onCancel(args: Any?) {
        this.mStreamSink = null
        HardwareButtonsWatcherManager.getInstance(application).removeLockButtonListener(lockButtonListener)
    }
}
