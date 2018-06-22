package net.perfectdreams.dreamcore.utils

import net.perfectdreams.dreamcore.utils.LocationUtils.isLocationBetweenLocations
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity

object LocationUtils {
	val axis = arrayOf(BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH)
	val radial = arrayOf(BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST, BlockFace.NORTH, BlockFace.NORTH_EAST)

	fun locationToFace(location: Location): BlockFace {
		return yawToFace(location.yaw)
	}

	fun yawToFace(yaw: Float): BlockFace {
		return yawToFace(yaw, true)
	}

	fun yawToFace(yaw: Float, useSubCardinalDirections: Boolean): BlockFace {
		return if (useSubCardinalDirections) {
			radial[Math.round(yaw / 45f) and 0x7]
		} else {
			axis[Math.round(yaw / 90f) and 0x3]
		}
	}

	fun center(location: Location): Location {
		val loc = location.clone()
		loc.x = location.blockX + 0.5
		loc.y = location.blockY + 0.5
		loc.z = location.blockZ + 0.5
		return loc
	}

	fun level(location: Location): Location {
		val loc = location.clone()
		loc.pitch = 0.0f
		return loc
	}

	fun straighten(location: Location): Location {
		val loc = location.clone()
		loc.yaw = Math.round(location.yaw / 90.0f) * 90.0f
		return loc
	}

	fun faceEntity(fromLocation: Location, at: Entity): Location {
		return if (fromLocation.world !== at.world) {
			fromLocation
		} else faceLocation(fromLocation, at.location)
	}

	fun faceLocation(fromLocation: Location, to: Location): Location {
		if (fromLocation.world !== to.world) {
			return fromLocation
		}
		val xDiff = to.x - fromLocation.x
		val yDiff = to.y - fromLocation.y
		val zDiff = to.z - fromLocation.z
		val distanceXZ = Math.sqrt(xDiff * xDiff + zDiff * zDiff)
		val distanceY = Math.sqrt(distanceXZ * distanceXZ + yDiff * yDiff)
		var yaw = Math.toDegrees(Math.acos(xDiff / distanceXZ)) - 180.0
		val pitch = Math.toDegrees(Math.acos(yDiff / distanceY)) - 90.0
		if (zDiff < 0.0) {
			yaw += Math.abs(yaw) * 2.0
		}
		yaw += 90.0
		fromLocation.pitch = pitch.toFloat()
		fromLocation.yaw = yaw.toFloat()
		return fromLocation
	}

	fun isLocationBetweenLocations(location: Location, loc1: Location, loc2: Location): Boolean {
        val minX = Math.min(loc1.x, loc2.x)
		val minY = Math.min(loc1.y, loc2.y)
		val minZ = Math.min(loc1.z, loc2.z)
		val maxX = Math.max(loc1.x, loc2.x)
		val maxY = Math.max(loc1.y, loc2.y)
		val maxZ = Math.max(loc1.z, loc2.z)
        return location.world == loc1.world && location.x in minX..maxX && location.y in minY..maxY && location.z in minZ..maxZ
	}
}

fun Location.isBetween(loc1: Location, loc2: Location): Boolean {
	return isLocationBetweenLocations(this, loc1, loc2)
}