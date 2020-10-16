package flutter.moum.hardware_buttons

import android.app.Application
import io.flutter.plugin.common.EventChannel

class HomeButtonStreamHandler(private val application: Application) : EventChannel.StreamHandler {

    private var mStreamSink: EventChannel.EventSink? = null

    private val mHomeButtonListener = object : HardwareButtonsWatcherManager.HomeButtonListener {
        override fun onHomeButtonEvent() {
            mStreamSink?.success(0)
        }
    }

    override fun onListen(args: Any?, sink: EventChannel.EventSink?) {
        this.mStreamSink = sink
        HardwareButtonsWatcherManager.getInstance(application).addHomeButtonListener(mHomeButtonListener)
    }

    override fun onCancel(args: Any?) {
        this.mStreamSink = null
        HardwareButtonsWatcherManager.getInstance(application).removeHomeButtonListener(mHomeButtonListener)
    }
}