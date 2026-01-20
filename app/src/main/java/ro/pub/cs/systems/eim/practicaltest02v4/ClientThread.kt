package ro.pub.cs.systems.eim.practicaltest02v4

import android.os.Handler
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class ClientThread(
    private val address: String,
    private val port: Int,
    private val url: String,
    private val handler: Handler
) : Thread() {

    companion object {
        private const val TAG = "ClientThread"
    }

    override fun run() {
        try {
            Log.d(TAG, "Connecting to server at $address:$port")
            val socket = Socket(address, port)

            val writer = PrintWriter(socket.getOutputStream(), true)
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

            // Send URL to server
            writer.println(url)
            Log.d(TAG, "Sent URL to server: $url")

            // Read HTML response from server
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (line == "END_OF_RESPONSE") {
                    break
                }
                response.append(line).append("\n")
            }

            socket.close()
            Log.d(TAG, "Received response (${response.length} chars)")

            // Update UI via handler
            handler.post {
                PracticalTest02MainActivityv4.updateHtmlContent(response.toString())
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in client: ${e.message}")
            handler.post {
                PracticalTest02MainActivityv4.updateHtmlContent("ERROR: ${e.message}")
            }
        }
    }
}