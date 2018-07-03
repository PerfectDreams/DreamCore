package net.perfectdreams.dreamcore.utils

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import java.io.File
import java.util.*

/**
 * Classe para spawnar hologramas usando armor stands
 *
 * @author MrPowerGamerBR
 */
class ArmorStandHologram(var location: Location, internal var line: String?) {
	companion object {
		private val ARMOR_STANDS_UNIQUE_IDS = mutableSetOf<UUID>()
		private val ARMOR_STAND_FILE by lazy {
			val plugin = Bukkit.getPluginManager().getPlugin("DreamCore")
			plugin.dataFolder.mkdirs()
			File(plugin.dataFolder, "armor_stand_holograms")
		}

		/**
		 * Adiciona uma armor stand a lista de IDs de hologramas criados com armor stands
		 *
		 * Utilizado para deletar todas as armor stands quando o servidor reiniciar
		 */
		@Synchronized
		fun addUniqueId(uniqueId: UUID) {
			ARMOR_STANDS_UNIQUE_IDS.add(uniqueId)

			scheduler().schedule(Bukkit.getPluginManager().getPlugin("DreamCore"), SynchronizationContext.ASYNC) {
				ARMOR_STAND_FILE.writeText(
						ARMOR_STANDS_UNIQUE_IDS.joinToString("\n")
				)
			}
		}

		/**
		 * Carrega todos os IDs das armor stands
		 */
		fun loadArmorStandsUniqueIds() {
			if (!ARMOR_STAND_FILE.exists())
				return

			ARMOR_STANDS_UNIQUE_IDS.addAll(
					ARMOR_STAND_FILE.readLines().map { UUID.fromString(it) }
			)
		}

		/**
		 * Remove todas as armor stands em hologramas do servidor
		 */
		fun killAllStoredArmorStands() {
			ARMOR_STANDS_UNIQUE_IDS.forEach {
				Bukkit.getEntity(it).remove()
			}

			ARMOR_STANDS_UNIQUE_IDS.clear()
			ARMOR_STAND_FILE.writeText(
					ARMOR_STANDS_UNIQUE_IDS.joinToString("\n")
			)
		}
	}

	var armorStand: ArmorStand? = null

	fun spawn() {
		armorStand?.remove()

		val stand = location.world.spawnEntity(location, EntityType.ARMOR_STAND) as ArmorStand
		stand.customName = line
		stand.isCustomNameVisible = true
		stand.isMarker = true
		stand.isVisible = false

		addUniqueId(stand.uniqueId)
	}

	fun despawn() {
		armorStand?.remove()
		armorStand = null
	}

	fun teleport(newLocation: Location) {
		armorStand?.teleport(newLocation)
		location = newLocation
	}

	fun setLine(newLine: String) {
		armorStand?.customName = line
		line = newLine
	}

	fun addLineBelow(line: String): ArmorStandHologram {
		val hologram = ArmorStandHologram(location.clone().add(0.0, -0.285, 0.0), line)
		return hologram
	}

	fun addLineAbove(line: String): ArmorStandHologram {
		val hologram = ArmorStandHologram(location.clone().add(0.0, 0.285, 0.0), line)
		return hologram
	}
}