package net.perfectdreams.dreamcore.utils

import com.sk89q.worldedit.bukkit.BukkitUtil
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.ApplicableRegionSet
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player
import java.util.*

object WorldGuardUtils {
	val worldGuard: WorldGuardPlugin?
		get() {
			val plugin = Bukkit.getServer().pluginManager.getPlugin("WorldGuard")
			return if (plugin == null || plugin !is WorldGuardPlugin) {
				null
			} else plugin
		}

	fun isWithinRegion(block: Block, region: String): Boolean {
		return isWithinRegion(block.location, region)
	}

	fun isWithinRegion(player: Player, region: String): Boolean {
		return isWithinRegion(player.location, region)
	}

	fun isWithinRegion(loc: Location, region: String): Boolean {
		val guard = worldGuard
		val v = BukkitUtil.toVector(loc)
		val manager = guard!!.getRegionManager(loc.world)
		val set = manager.getApplicableRegions(v)
		return set.any { it.id.equals(region, ignoreCase = true) }
	}

	/**
	 * Retorna todas as regiões do WorldGuard em uma localização específica
	 *
	 * @return um set com todas as regiões na localização
	 */
	fun getRegionsAt(loc: Location): ApplicableRegionSet {
		val guard = worldGuard
		val v = BukkitUtil.toVector(loc)
		val manager = guard!!.getRegionManager(loc.world)
		val set = manager.getApplicableRegions(v)
		return set
	}

	/**
	 * Retorna ID de todas as regiões do WorldGuard em uma localização específica
	 *
	 * @return um set com todas as regiões na localização
	 * @see getRegionsAt
	 */
	fun getRegionIdsAt(loc: Location): List<String> {
		return getRegionsAt(loc).map { it.id }
	}
}
