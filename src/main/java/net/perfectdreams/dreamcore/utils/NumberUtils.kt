package net.perfectdreams.dreamcore.utils

import java.util.*

/*
 * Adapted from bhlangonijr of Stack Overflow - http://stackoverflow.com/a/19759564
 */
private object RomanNumeralHelper {
	val map: TreeMap<Int, String> = TreeMap()

	init {
		map.put(1000, "M")
		map.put(900, "CM")
		map.put(500, "D")
		map.put(400, "CD")
		map.put(100, "C")
		map.put(90, "KC")
		map.put(50, "L")
		map.put(40, "XL")
		map.put(10, "X")
		map.put(9, "IX")
		map.put(5, "V")
		map.put(4, "IV")
		map.put(1, "I")
	}

	fun toRoman(number: Int): String {
		val l = map.floorKey(number)
		if (number == l) {
			return map[number]!!
		}
		return "${map[1]}${toRoman(number - 1)}"
	}

}

fun Int.toRomanNumeral(): String {
	return RomanNumeralHelper.toRoman(this)
}