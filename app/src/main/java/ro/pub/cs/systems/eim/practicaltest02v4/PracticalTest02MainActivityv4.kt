package ro.pub.cs.systems.eim.practicaltest02v4

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PracticalTest02MainActivityv4 : AppCompatActivity() {

    private lateinit var portEditText: EditText
    private lateinit var startServerButton: Button
    private lateinit var urlEditText: EditText
    private lateinit var getButton: Button
    private lateinit var htmlContentTextView: TextView

    private var serverThread: ServerThread? = null

    companion object {
        private const val TAG = "PracticalTest02MainActivityv4"
        private var htmlContentTextViewRef: TextView? = null

        fun updateHtmlContent(content: String) {
            htmlContentTextViewRef?.text = content
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_practical_test02v4_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        portEditText = findViewById(R.id.portEditText)
        startServerButton = findViewById(R.id.startServerButton)
        urlEditText = findViewById(R.id.urlEditText)
        getButton = findViewById(R.id.getButton)
        htmlContentTextView = findViewById(R.id.htmlContentTextView)
        htmlContentTextViewRef = htmlContentTextView

        startServerButton.setOnClickListener {
            val portStr = portEditText.text.toString()
            if (portStr.isEmpty()) {
                Toast.makeText(this, "Server port is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val port = portStr.toInt()

            serverThread?.stopServer()
            serverThread = ServerThread(port)
            serverThread?.start()
            Toast.makeText(this, "Server started on port $port", Toast.LENGTH_SHORT).show()
        }

        getButton.setOnClickListener {
            val portStr = portEditText.text.toString()
            val url = urlEditText.text.toString()

            if (portStr.isEmpty() || url.isEmpty()) {
                Toast.makeText(this, "Port and URL are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val port = portStr.toInt()
            val clientThread = ClientThread(
                "127.0.0.1",
                port,
                url,
                Handler(Looper.getMainLooper())
            )
            clientThread.start()
        }
    }

    override fun onDestroy() {
        serverThread?.stopServer()
        htmlContentTextViewRef = null
        super.onDestroy()
    }
}
