package net.perfectdreams.dreamcore.utils

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Sign

object BlockUtils {
	fun attachWallSignAt(l: Location): Block? {
		l.block.type = Material.WALL_SIGN
		val s = l.block.state as Sign
		val matSign = org.bukkit.material.Sign(Material.WALL_SIGN)
		val bf = FaceUtils.getBlockFaceForWallSign(s.block) ?: run {
			l.block.type = Material.AIR
			return null
		}
		matSign.data = FaceUtils.fromBlockFaceToWallSignByte(bf)
		s.data = matSign
		s.update()
		return s.block
	}
}