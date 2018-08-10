package net.perfectdreams.dreamcore.utils.socket

import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.scheduler
import org.bukkit.Bukkit
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetAddress
import java.net.ServerSocket
import kotlin.concurrent.thread

class SocketServer(val socketPort: Int) {
	fun start() {
		val listener = ServerSocket(socketPort, 0, InetAddress.getLoopbackAddress())
		try {
			while (true) {
				val socket = listener.accept()
				launch {
					try {
						val fromClient = BufferedReader(InputStreamReader(socket.getInputStream(), "UTF-8"))
						val reply = fromClient.readLine()
						val jsonObject = DreamUtils.jsonParser.parse(reply).obj
						val response = JsonObject()

						scheduler().schedule(Bukkit.getPluginManager().getPlugin("DreamCore"), SynchronizationContext.SYNC) {
							val event = SocketReceivedEvent(jsonObject, response)
							Bukkit.getPluginManager().callEvent(event)

							switchContext(SynchronizationContext.ASYNC)
							val asyncEvent = AsyncSocketReceivedEvent(jsonObject, event.response)
							Bukkit.getPluginManager().callEvent(asyncEvent)

							val out = PrintWriter(socket.getOutputStream(), true)
							out.println(asyncEvent.response.toString() + "\n")
							out.flush()
							fromClient.close()
						}
					} finally {
						socket.close()
					}
				}
			}
		} finally {
			listener.close()
		}
	}
}