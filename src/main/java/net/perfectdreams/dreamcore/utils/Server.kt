package net.perfectdreams.dreamcore.utils

import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonObject
import net.perfectdreams.dreamcore.utils.Constants.LOCAL_HOST
import net.perfectdreams.dreamcore.utils.Constants.LORITTA_PORT
import net.perfectdreams.dreamcore.utils.Constants.PANTUFA_PORT
import net.perfectdreams.dreamcore.utils.Constants.PERFECTDREAMS_BUNGEE_PORT
import net.perfectdreams.dreamcore.utils.Constants.PERFECTDREAMS_LOBBY_PORT
import net.perfectdreams.dreamcore.utils.socket.SocketUtils

open class Server(val host: String, val socketPort: Int, val internalName: String, val fancyName: String, val name: String) {
	companion object {
		val LORITTA = Server(LOCAL_HOST, LORITTA_PORT, "loritta", "Loritta", "Loritta")
		val PANTUFA = PantufaServer(LOCAL_HOST, PANTUFA_PORT, "pantufa", "Pantufa", "Pantufa")
		val PERFECTDREAMS_BUNGEE = MinecraftServer(LOCAL_HOST, PERFECTDREAMS_BUNGEE_PORT, "bungeecord", "PerfectDreams BungeeCord", "BungeeCord")
		val PERFECTDREAMS_LOBBY = MinecraftServer(LOCAL_HOST, PERFECTDREAMS_LOBBY_PORT, "perfectdreams_lobby", "PerfectDreams Lobby", "Lobby")
		val servers = mutableListOf<Server>()

		init {
			servers.add(LORITTA)
			servers.add(PANTUFA)
			servers.add(PERFECTDREAMS_BUNGEE)
			servers.add(PERFECTDREAMS_LOBBY)
		}

		fun getByInternalName(internalName: String) = servers.firstOrNull { it.internalName == internalName }
	}

	class MinecraftServer(host: String, socketPort: Int, internalName: String, fancyName: String, name: String) : Server(host, socketPort, internalName, fancyName, name)

	class PantufaServer(host: String, socketPort: Int, internalName: String, fancyName: String, name: String) : Server(host, socketPort, internalName, fancyName, name) {
		fun sendMessageAsync(channelId: String, message: String, success: ((JsonObject) -> Unit)? = null, error: (() -> Unit)? = null) {
			return sendAsync(
					jsonObject(
							"type" to "sendMessage",
							"textChannelId" to channelId
					),
					success,
					error
			)
		}

		fun sendMessage(channelId: String, message: String): JsonObject {
			return send(
					jsonObject(
							"type" to "sendMessage",
							"textChannelId" to channelId
					)
			)
		}
	}

	fun sendAsync(jsonObject: JsonObject, success: ((JsonObject) -> Unit)? = null, error: (() -> Unit)? = null) = SocketUtils.sendAsync(jsonObject, host, socketPort, success, error)
	fun send(jsonObject: JsonObject) = SocketUtils.send(jsonObject, host, socketPort)
}