package net.perfectdreams.dreamcore.utils.extensions

import com.sk89q.worldguard.protection.ApplicableRegionSet
import net.perfectdreams.dreamcore.utils.WorldGuardUtils
import org.bukkit.Location

/**
 * Retorna se a localização está dentro de uma determinada região do WorldGuard
 *
 * @param  region o ID da região
 * @return        se a localização está dentro da região
 * @see           WorldGuardUtils.isWithinRegion
 */
fun Location.isWithinRegion(region: String): Boolean {
	return WorldGuardUtils.isWithinRegion(this, region)
}

/**
 * Retorna todas as regiões que a localização está dentro dela
 *
 * @return todas as regiões em que a localização está dentro
 * @see WorldGuardUtils.getRegionsAt
 */
val Location.worldGuardRegions: ApplicableRegionSet
	get() = WorldGuardUtils.getRegionsAt(this)