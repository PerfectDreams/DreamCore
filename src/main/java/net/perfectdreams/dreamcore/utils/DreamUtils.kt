package net.perfectdreams.dreamcore.utils

import com.github.salomonbrys.kotson.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mongodb.ServerAddress
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.event.ServerHeartbeatFailedEvent
import com.mongodb.event.ServerHeartbeatStartedEvent
import com.mongodb.event.ServerHeartbeatSucceededEvent
import com.mongodb.event.ServerMonitorListener
import net.md_5.bungee.api.chat.BaseComponent
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.codecs.LocationCodec
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistries.fromProviders
import org.bson.codecs.configuration.CodecRegistries.fromRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.bson.codecs.pojo.PojoCodecProvider
import org.bson.conversions.Bson
import org.bukkit.*
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarFlag
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import org.bukkit.scheduler.BukkitScheduler
import protocolsupport.api.ProtocolSupportAPI
import protocolsupport.api.ProtocolVersion
import java.util.*
import java.util.logging.Level

/**
 * Extensões gerais e variáveis utilitárias
 */
object DreamUtils {
	@JvmStatic
	val random = SplittableRandom()
	@JvmStatic
	val mongoClient: MongoClient
	@JvmStatic
	val gson: Gson
	@JvmStatic
	val jsonParser = JsonParser()
	val pojoCodecProvider: PojoCodecProvider
	val pojoCodecRegistry: CodecRegistry
	val database: MongoDatabase
	val usersCollection: MongoCollection<Document>

	init {
		pojoCodecProvider = PojoCodecProvider.builder()
				.automatic(true)
				.build()

		pojoCodecRegistry = fromRegistries(
				CodecRegistries.fromCodecs(LocationCodec()),
				MongoClient.getDefaultCodecRegistry(),
				fromProviders(pojoCodecProvider)
		)

		val clientOptions = MongoClientOptions.Builder()
                .addServerMonitorListener(MongoServerMonitor())
				.codecRegistry(pojoCodecRegistry)
                .build()

		mongoClient = MongoClient(ServerAddress("localhost", 27017), clientOptions)

		val gsonBuilder = GsonBuilder()
				.registerTypeAdapter<Location> {
					serialize {
						val jsonObject = JsonObject()
						jsonObject["x"] = it.src.x
						jsonObject["y"] = it.src.y
						jsonObject["z"] = it.src.z
						jsonObject["yaw"] = it.src.yaw
						jsonObject["pitch"] = it.src.pitch
						jsonObject["world"] = it.src.world.name
						return@serialize jsonObject
					}

					deserialize {
						val x = it.json["x"].double
						val y = it.json["y"].double
						val z = it.json["z"].double
						val yaw = it.json["yaw"].float
						val pitch = it.json["pitch"].float
						val worldName = it.json["world"].string

						Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch)
					}
				}
				.registerTypeAdapter<ItemStack> {
					serialize {
						val jsonObject = JsonObject()
						jsonObject["type"] = it.src.type.name
						jsonObject["durability"] = it.src.durability
						jsonObject["amount"] = it.src.amount

						val enchantmentMap = JsonObject()
						for ((enchantment, level) in it.src.enchantments) {
							enchantmentMap[enchantment.name] = level
						}
						jsonObject["enchantments"] = enchantmentMap

						if (it.src.hasItemMeta()) {
							val jsonMeta = JsonObject()
							val meta = it.src.itemMeta
							jsonMeta["displayName"] = meta.displayName
							jsonMeta["lore"] = it.context.serialize(meta.lore)

							jsonMeta["isUnbreakable"] = meta.isUnbreakable
							jsonMeta["itemFlags"] = it.context.serialize(meta.itemFlags)

							if (meta is LeatherArmorMeta) {
								val leatherMeta = JsonObject()
								leatherMeta["r"] = meta.color.red
								leatherMeta["g"] = meta.color.green
								leatherMeta["b"] = meta.color.blue

								jsonMeta["color"] = leatherMeta
							}

							jsonObject["itemMeta"] = jsonMeta
						}

						return@serialize jsonObject
					}

					deserialize {
						val jsonObject = it.json.obj
						val type = Material.valueOf(jsonObject["type"].string)
						val amount = jsonObject["amount"].nullInt ?: 0
						val durability = jsonObject["durability"].nullShort ?: 0
						val enchantments = jsonObject["enchantments"].nullObj
						val jsonMeta = jsonObject["itemMeta"].nullObj
						val attributes = jsonObject["attributes"].nullObj

						var itemStack = ItemStack(type, amount, durability)

						if (enchantments != null) {
							for ((enchantmentName, level) in it.context.deserialize<Map<String, Int>>(enchantments)) {
								itemStack.addUnsafeEnchantment(Enchantment.getByName(enchantmentName), level)
							}
						}

						if (jsonMeta != null) {
							val meta = itemStack.itemMeta
							meta.displayName = jsonMeta["displayName"].nullString

							if (jsonMeta.has("lore")) {
								meta.lore = it.context.deserialize<List<String>>(jsonMeta["lore"])
							}

							meta.isUnbreakable = jsonMeta["isUnbreakable"].nullBool ?: false

							if (jsonMeta.has("itemFlags")) {
								for (itemFlagName in it.context.deserialize<List<String>>(jsonMeta["itemFlags"])) {
									meta.addItemFlags(ItemFlag.valueOf(itemFlagName))
								}
							}

							if (jsonMeta.has("color")) {
								val leatherMeta = jsonMeta["color"].obj

								meta as LeatherArmorMeta
								meta.color = Color.fromRGB(leatherMeta["r"].int, leatherMeta["g"].int, leatherMeta["b"].int)
							}

							itemStack.itemMeta = meta
						}

						if (attributes != null) {
							for ((key, element) in attributes.entrySet()) {
								itemStack = itemStack.setStorageData(element.string, UUID.fromString(key))
							}
						}
						return@deserialize itemStack
					}
				}
		gson = gsonBuilder.create()

		database = getMongoDatabase("perfectdreams")
		usersCollection = database.getCollection("users")
	}

	fun getMongoDatabase(name: String): MongoDatabase {
		val database = mongoClient.getDatabase(name)
		return database.withCodecRegistry(pojoCodecRegistry)
	}

	/**
	 * Retorna as informações de um usuário do PerfectDreams
	 *
	 * @param player instância do player
	 * @returns      as informações do usuário do PerfectDreams
	 */
	fun getPerfectDreamsPlayerInfo(player: Player): Document? {
		return getPerfectDreamsPlayerInfo(player.uniqueId)
	}

	/**
	 * Retorna as informações de um usuário do PerfectDreams
	 *
	 * @param username nome do player
	 * @returns        as informações do usuário do PerfectDreams
	 */
	fun getPerfectDreamsPlayerInfo(username: String): Document? {
		return usersCollection.find(Filters.eq("username", username)).firstOrNull()
	}

	/**
	 * Retorna as informações de um usuário do PerfectDreams a partir do username em lower case
	 *
	 * @param lowerCaseUsername nome do player em lower case
	 * @returns                 as informações do usuário do PerfectDreams
	 */
	fun getPerfectDreamsPlayerInfoByLowerCaseUsername(lowerCaseUsername: String): Document? {
		return usersCollection.find(Filters.eq("lowerCaseUsername", lowerCaseUsername)).firstOrNull()
	}

	/**
	 * Retorna as informações de um usuário do PerfectDreams a partir do UUID do jogador
	 *
	 * @param uuid UUID do jogador
	 * @returns    as informações do usuário do PerfectDreams
	 */
	fun getPerfectDreamsPlayerInfo(uuid: UUID): Document? {
		return usersCollection.find(Filters.eq("_id", uuid)).firstOrNull()
	}

	/**
	 * Cria as informações de um jogador do PerfectDreams
	 *
	 * @param player instância do player
	 * @returns      as informações criadas do usuário
	 */
	fun createPerfectDreamsPlayerInfo(player: Player): Document {
		return createPerfectDreamsPlayerInfo(player.uniqueId, player.name)
	}

	/**
	 * Cria as informações de um jogador do PerfectDreams
	 *
	 * @param uuid     UUID do player
	 * @param username nome do player
	 * @returns        as informações criadas do usuário
	 */
	fun createPerfectDreamsPlayerInfo(uuid: UUID, username: String): Document {
		val document = Document()
		document["_id"] = uuid
		document["username"] = username
		document["lowerCaseUsername"] = username.toLowerCase()

		return document
	}

	/**
	 * Retorna o username do usuário a partir do UUID dele
	 *
	 * @param uuid uuid do usuário
	 * @returns    o username do usuário
	 */
	fun getUsernameFromUniqueId(uuid: UUID): String? {
		return getPerfectDreamsPlayerInfo(uuid)?.getString("username")
	}

	/**
	 * Retorna o UUID do usuário a partir do nome dele
	 *
	 * @param username username do usuário
	 * @returns        o UUID do usuário
	 */
	fun getUniqueIdFromUsername(username: String): UUID? {
		return getPerfectDreamsPlayerInfo(username)?.get("_id", UUID::class.java)
	}

	/**
	 * Retorna o username do usuário a partir do username em lowercase dele
	 *
	 * @param lowerCaseUsername o nome do usuário em lowercase
	 * @returns                 o real nome do usuário
	 */
	fun getPrettyUsernameFromUsername(lowerCaseUsername: String): String? {
		return getPerfectDreamsPlayerInfoByLowerCaseUsername(lowerCaseUsername)?.getString("username")
	}

	class MongoServerMonitor : ServerMonitorListener {
		override fun serverHeartbeatFailed(p0: ServerHeartbeatFailedEvent) {
			Bukkit.getPluginManager().getPlugin("DreamCore").logger.log(Level.SEVERE, "Lost MongoDB connection! Shutting down...")
			Bukkit.shutdown()
		}

		override fun serverHeartbeatSucceeded(p0: ServerHeartbeatSucceededEvent) {
		}

		override fun serverHearbeatStarted(p0: ServerHeartbeatStartedEvent) {
		}
	}
}

fun broadcast(message: String, permission: String = Server.BROADCAST_CHANNEL_USERS): Int {
	return Bukkit.broadcast(message, permission)
}

fun broadcast(baseComponent: BaseComponent) {
	Bukkit.spigot().broadcast(baseComponent)
}

fun Plugin.registerEvents(listener: Listener) {
	Bukkit.getPluginManager().registerEvents(listener, this)
}

fun <T> List<T>.getRandom(): T {
	return this[DreamUtils.random.nextInt(this.size)]
}

fun scheduler(): BukkitScheduler {
	return Bukkit.getScheduler()
}

fun server(): Server {
	return Bukkit.getServer()
}

fun onlinePlayers(): Collection<Player> {
	return Bukkit.getOnlinePlayers()
}

fun createInventory(holder: InventoryHolder? = null, size: Int = 9, name: String? = null): Inventory {
	return Bukkit.createInventory(holder, size, name)
}

fun createBossBar(title: String, color: BarColor, style: BarStyle, vararg flags: BarFlag): BossBar {
	return Bukkit.getServer().createBossBar(title, color, style, *flags)
}

fun pluginManager(): PluginManager {
	return Bukkit.getServer().pluginManager
}

val isPrimaryThread: Boolean
	get() = Bukkit.getServer().isPrimaryThread

val withoutPermission: String
	get() = DreamCore.dreamConfig.withoutPermission

val Player.version: ProtocolVersion
	get() = ProtocolSupportAPI.getProtocolVersion(this)

val serverName: String
	get() = DreamCore.dreamConfig.serverName

val bungeeName: String
	get() = DreamCore.dreamConfig.bungeeName

val World.blacklistedTeleport: Boolean
	get() {
		return DreamCore.dreamConfig.blacklistedWorldsTeleport.contains(this.name)
	}

val isStaffPermission: String
	get() = DreamCore.dreamConfig.isStaffPermission

val Location.blacklistedTeleport: Boolean
	get() {
		if (DreamCore.dreamConfig.blacklistedWorldsTeleport.contains(this.world.name)) {
			return true
		}
		val regions = WorldGuardUtils.getRegionIdsAt(this)
		return regions.any { DreamCore.dreamConfig.blacklistedRegionsTeleport.contains(it) }
	}

fun String.toPlayerExact(): Player {
	return Bukkit.getPlayerExact(this)
}

fun generateCommandInfo(command: String, arguments: Map<String, String> = mapOf(), tips: List<String> = listOf()): String {
	var base = "§eComo usar: §6/$command"

	for ((key, _) in arguments) {
		base += " " + key
	}

	base += "\n"
	for ((argument, info) in arguments) {
		base += "§8★ §f$argument §8- §7$info\n"
	}

	base += "§f\n"
	if (tips.isNotEmpty()) {
		for ((index, tip) in tips.withIndex()) {
			base += "\n"
			if (index == tips.size - 1)
				base += "§8• §7$tip"
		}
	}
	return base
}

fun <T> MongoCollection<T>.save(obj: T, filters: Bson) {
	val replaceOptions = ReplaceOptions().upsert(true)
	this.replaceOne(
			filters,
			obj,
			replaceOptions
	)
}

fun <T> MongoCollection<T>.save(obj: T) {
	this.insertOne(
			obj
	)
}