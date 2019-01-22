package net.perfectdreams.dreamcore.eventmanager

import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.scheduler
import org.bukkit.Bukkit

class DreamEventManager {
	val events = mutableListOf<ServerEvent>()

	fun startEventsTask() {
		scheduler().schedule(DreamCore.INSTANCE) {
			while (true) {
				val upcoming = getUpcomingEvents()

				for (event in upcoming) {
					val diff = System.currentTimeMillis() - (event.lastTime + event.delayBetween)

					if (diff >= 0 && Bukkit.getOnlinePlayers().size >= event.requiredPlayers) {
						event.preStart()
					}
				}
				waitFor(20)
			}
		}
	}

	fun getRunningEvents(): List<ServerEvent> {
		return events.filter { it.running }
	}

	fun getUpcomingEvents(): List<ServerEvent> {
		return events.filter { !it.running }
	}
}