package net.perfectdreams.dreamcore.utils.extensions

import net.perfectdreams.dreamcore.utils.PlayerUtils
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player

fun Player.canBreakAt(location: Location, material: Material) = PlayerUtils.canBreakAt(location, this, material)
fun Player.canPlaceAt(location: Location, material: Material) = PlayerUtils.canPlaceAt(location, this, material)