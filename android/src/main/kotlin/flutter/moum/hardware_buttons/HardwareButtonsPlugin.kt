package flutter.moum.hardware_buttons

import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel

class HardwareButtonsPlugin : FlutterPlugin, ActivityAware {
    private lateinit var mVolumeEventChannel: EventChannel
    private lateinit var mHomeEventChannel: EventChannel
    private lateinit var mLockEventChannel: EventChannel

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        mVolumeEventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "flutter.moum.hardware_buttons.volume")
        mHomeEventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "flutter.moum.hardware_buttons.home")
        mLockEventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "flutter.moum.hardware_buttons.lock")
    }

    override fun onDetachedFromActivity() {
        // No-op
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        // No-op
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        mVolumeEventChannel.setStreamHandler(VolumeButtonStreamHandler(binding.activity.application))
        mHomeEventChannel.setStreamHandler(HomeButtonStreamHandler(binding.activity.application))
        mLockEventChannel.setStreamHandler(LockButtonStreamHandler(binding.activity.application))
    }

    override fun onDetachedFromActivityForConfigChanges() {
        // No-op
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        mVolumeEventChannel.setStreamHandler(null)
        mHomeEventChannel.setStreamHandler(null)
        mLockEventChannel.setStreamHandler(null)
    }
}
