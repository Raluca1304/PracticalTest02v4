package ro.pub.cs.systems.eim.practicaltest02v4

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.HttpURLConnection
import java.net.Socket
import java.net.URL

class CommunicationThread(
    private val clientSocket: Socket,
    private val cache: HashMap<String, String>
) : Thread() {

    companion object {
        private const val TAG = "CommunicationThread"
    }

    override fun run() {
        try {
            val reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            val writer = PrintWriter(clientSocket.getOutputStream(), true)

            // Read URL from client
            val url = reader.readLine()
            Log.d(TAG, "Received URL request: $url")

            if (url.isNullOrEmpty()) {
                writer.println("ERROR: Empty URL")
                return
            }

            // Check cache first
            val htmlContent: String
            synchronized(cache) {
                if (cache.containsKey(url)) {
                    Log.d(TAG, "Cache HIT for URL: $url")
                    htmlContent = cache[url]!!
                } else {
                    Log.d(TAG, "Cache MISS for URL: $url - Fetching from web...")
                    htmlContent = fetchHtmlContent(url)
                    cache[url] = htmlContent
                    Log.d(TAG, "Cached content for URL: $url")
                }
            }

            // Send HTML content back to client
            writer.println(htmlContent)
            writer.println("END_OF_RESPONSE")

            Log.d(TAG, "Sent HTML content to client (${htmlContent.length} chars)")

        } catch (e: Exception) {
            Log.e(TAG, "Error in communication: ${e.message}")
        } finally {
            try {
                clientSocket.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error closing client socket: ${e.message}")
            }
        }
    }

    private fun fetchHtmlContent(urlString: String): String {
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line).append("\n")
                }
                reader.close()
                connection.disconnect()
                response.toString()
            } else {
                "ERROR: HTTP response code $responseCode"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching HTML: ${e.message}")
            "ERROR: ${e.message}"
        }
    }
}