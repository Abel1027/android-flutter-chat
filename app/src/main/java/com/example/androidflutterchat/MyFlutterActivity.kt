package com.example.androidflutterchat

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel

class MyFlutterActivity : FlutterActivity() {
    private class IntentBuilder() : CachedEngineIntentBuilder(
        MyFlutterActivity::class.java,
        "flutter_engine"
    )

    companion object {
        private lateinit var cachedFlutterEngine : FlutterEngine
        private const val EVENT_CHANNEL_NAME = "com.example.flutter/event"
        private const val METHOD_CHANNEL_NAME = "com.example.flutter/method"
        private var eventChannelSink : EventChannel.EventSink? = null
        private var eventChannel : EventChannel? = null
        private var methodChannel : MethodChannel? = null

        fun init(context: Context) {
            if (Companion::cachedFlutterEngine.isInitialized) {
                return
            }

            cachedFlutterEngine = FlutterEngine(context)

            cachedFlutterEngine.dartExecutor.executeDartEntrypoint(
                DartExecutor.DartEntrypoint.createDefault()
            )

            FlutterEngineCache.getInstance().put("flutter_engine", cachedFlutterEngine)

            MyFlutterActivity().setupEventChannel()
        }

        fun createBuilder() : CachedEngineIntentBuilder {
            return IntentBuilder()
        }
    }

    private fun setupEventChannel() {
        eventChannel = EventChannel(cachedFlutterEngine.dartExecutor, EVENT_CHANNEL_NAME)
        eventChannel!!.setStreamHandler(object: EventChannel.StreamHandler {
            override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                eventChannelSink = events
            }

            override fun onCancel(arguments: Any?) {
            }

        })
    }

    private fun setupMethodChannel(context: Context, messenger: BinaryMessenger) {
        methodChannel = MethodChannel(messenger, METHOD_CHANNEL_NAME)
        methodChannel!!.setMethodCallHandler { call, result ->
            when(call.method) {
                "sendMessage" -> {
                    val returnIntent = Intent()
                    val args = call.arguments

                    if (args is String) {
                        returnIntent.putExtra(context.resources.getString(R.string.message), args)
                        activity.setResult(Activity.RESULT_OK, returnIntent)
                    }
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = intent?.extras?.keySet()?.associateBy({it}, {intent.extras?.get(it)})

        if (data != null) {
            val messageValue = data[this.resources.getString(R.string.message)]
            if (messageValue is String) {
                eventChannelSink?.success(messageValue)
            }
        }

        setupMethodChannel(context, cachedFlutterEngine.dartExecutor)
    }
}