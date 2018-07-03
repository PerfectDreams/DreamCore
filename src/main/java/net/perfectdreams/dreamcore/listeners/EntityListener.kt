package net.perfectdreams.dreamcore.listeners

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent
import net.perfectdreams.dreamcore.utils.ArmorStandHologram
import org.bukkit.entity.ArmorStand
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent

class EntityListener : Listener {
	@EventHandler
	fun onEntityAdd(e: EntityAddToWorldEvent) {
		if (e.entity !is ArmorStand)
			return

		val markedForRemoval = ArmorStandHologram.ARMOR_STANDS_UNIQUE_IDS[e.entity.uniqueId] ?: return

		if (markedForRemoval) {
			e.entity.remove()

			ArmorStandHologram.updateFile()
		}
	}

	@EventHandler
	fun onEntityKill(e: EntityDeathEvent) {
		if (e.entity !is ArmorStand)
			return

		if (ArmorStandHologram.ARMOR_STANDS_UNIQUE_IDS.contains(e.entity.uniqueId)) {
			e.entity.remove()

			ArmorStandHologram.updateFile()
		}
	}
}