package flutter.moum.hardware_buttons

import android.app.Application
import io.flutter.plugin.common.EventChannel


class VolumeButtonStreamHandler(private val application: Application) : EventChannel.StreamHandler {
    private var mStreamSink: EventChannel.EventSink? = null

    private val volumeButtonListener = object : HardwareButtonsWatcherManager.VolumeButtonListener {
        override fun onVolumeButtonEvent(volumeButton: VolumeButton) {
            mStreamSink?.success(volumeButton.direction)
        }
    }

    override fun onListen(args: Any?, sink: EventChannel.EventSink?) {
        this.mStreamSink = sink
        HardwareButtonsWatcherManager.getInstance(application).addVolumeButtonListener(volumeButtonListener)
    }

    // this function doesn't actually get called by flutter framework as of now: 2019/10/02
    override fun onCancel(args: Any?) {
        this.mStreamSink = null
        HardwareButtonsWatcherManager.getInstance(application).removeVolumeButtonListener(volumeButtonListener)
    }
}
