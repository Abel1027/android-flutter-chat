package com.example.androidflutterchat

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyFlutterActivity.init(this)
        setContentView(R.layout.activity_main)

        val sendButton : Button = findViewById(R.id.sendBtn)

        val editableText : EditText = findViewById(R.id.messageInput)

        sendButton.setOnClickListener {
            val myFlutterActivityIntent = MyFlutterActivity.createBuilder().build(this)

            myFlutterActivityIntent.putExtra(
                this.resources.getString(R.string.message),
                editableText.text.toString()
            )

            startForResult.launch(myFlutterActivityIntent)
        }
    }

    private var startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->
        when(result.resultCode) {
            Activity.RESULT_OK -> {
                val data = result.data?.extras?.keySet()?.associateBy( {it}, {result.data?.extras?.get(it) })

                if (data != null) {
                    val value = data[this.resources.getString(R.string.message)]
                    if (value is String) {
                        val editableText: EditText = findViewById(R.id.messageInput)
                        editableText.setText(value)
                    }
                }
            }
            Activity.RESULT_CANCELED -> {
                println("RESULT CANCELED")
            }
        }
    }
}