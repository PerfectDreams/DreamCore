package net.perfectdreams.dreamcore.utils.socket

import com.google.gson.JsonObject
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.player.AsyncPlayerChatEvent

class AsyncSocketReceivedEvent(val json: JsonObject, var response: JsonObject) : Event() {
	override fun getHandlers(): HandlerList {
		return AsyncSocketReceivedEvent.handlers
	}

	companion object {
		private val handlers = HandlerList()

		@JvmStatic
		fun getHandlerList(): HandlerList {
			return handlers
		}
	}
}