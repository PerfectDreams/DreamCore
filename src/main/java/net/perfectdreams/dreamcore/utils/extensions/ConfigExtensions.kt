package net.perfectdreams.dreamcore.utils.extensions

import net.perfectdreams.dreamcore.utils.ConfigUtils
import org.bukkit.Location
import org.bukkit.configuration.file.FileConfiguration

fun FileConfiguration.getLocation(path: String) = ConfigUtils.loadLocation(this, path)
fun FileConfiguration.setLocation(path: String, location: Location) = ConfigUtils.saveLocation(location, this, path)