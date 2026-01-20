package ro.pub.cs.systems.eim.practicaltest02v4

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

class ServerThread(private val port: Int) : Thread() {

    companion object {
        private const val TAG = "ServerThread"
    }

    private var serverSocket: ServerSocket? = null
    private var isRunning = true

    // Cache for URL -> HTML content
    private val cache = HashMap<String, String>()

    override fun run() {
        try {
            serverSocket = ServerSocket(port)
            Log.d(TAG, "Server started on port $port")

            while (isRunning) {
                try {
                    val clientSocket: Socket = serverSocket!!.accept()
                    Log.d(TAG, "Client connected: ${clientSocket.inetAddress.hostAddress}")

                    // Create a communication thread for each client
                    val communicationThread = CommunicationThread(clientSocket, cache)
                    communicationThread.start()
                } catch (e: Exception) {
                    if (isRunning) {
                        Log.e(TAG, "Error accepting client connection: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting server: ${e.message}")
        }
    }

    fun stopServer() {
        isRunning = false
        try {
            serverSocket?.close()
            Log.d(TAG, "Server stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping server: ${e.message}")
        }
    }
}