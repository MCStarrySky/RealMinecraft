package kim.minecraft.starlightcore.features

import kim.minecraft.starlightcore.StarLightCore
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapelessRecipe
import taboolib.common.platform.function.submit
import taboolib.module.nms.ItemTag
import taboolib.module.nms.ItemTagData
import taboolib.module.nms.getItemTag
import taboolib.platform.util.asLangText
import taboolib.platform.util.onlinePlayers
import java.util.*
import kotlin.random.Random

object PoisonousFood : Listener {

    private val minDeathTime = StarLightCore.config.getLong("Features.PoisonousFood.MinDeathTime", 600)
    private val maxDeathTime = StarLightCore.config.getLong("Features.PoisonousFood.MaxDeathTime", 24000)
    private val intervalTicks: Long = StarLightCore.config.getLong("Features.PoisonousFood.IntervalTicks", 100)

    init {
        Material.values().filter { it.isEdible }.forEach { material ->
            ShapelessRecipe(NamespacedKey(StarLightCore.plugin, "PoisonousFood_${material.name}"),
                    ItemStack(material).apply {
                        this.addPoison()
                    })
                    .addIngredient(material)
                    .addIngredient(Material.SPIDER_EYE)
                    .also { shapelessRecipe -> Bukkit.addRecipe(shapelessRecipe) }
        }
        submit(delay = 20, period = intervalTicks) {
            onlinePlayers.forEach { checkDeath(it) }
        }
    }

    private fun ItemStack.addPoison() {
        getItemTag().also {
            if (it.getDeep("StarLightCore.PoisonousFood") == null)
                it.putDeep("StarLightCore.PoisonousFood", ItemTag())
            it.saveTo(this)
        }
    }

    @EventHandler
    fun onRecipe(e: CraftItemEvent) {
        e.currentItem!!.getItemTag().also {
            if (it.getDeep("StarLightCore.PoisonousFood.Killer") != null) return
            if (it.getDeep("StarLightCore.PoisonousFood") != null)
                it.putDeep("StarLightCore.PoisonousFood", ItemTag().apply {
                    this["TimeFor"] = ItemTagData(Random.nextLong(minDeathTime, maxDeathTime))
                    this["Killer"] = ItemTagData(e.whoClicked.uniqueId.toString())
                })
            it.saveTo(e.currentItem!!)
        }
    }

    @EventHandler
    fun onEat(e: PlayerItemConsumeEvent) {
        if (!e.item.type.isEdible) return
        val nbt = e.item.getItemTag()
        if (nbt.getDeep("StarLightCore.PoisonousFood.TimeFor") == null) return
        if (e.player.scoreboardTags.any { it.startsWith("StarLightCore.PoisonousFood.TimeOn") }) return
        e.player.addScoreboardTag("StarLightCore.PoisonousFood.TimeOn:${Bukkit.getWorlds()[0].fullTime + nbt.getDeep("StarLightCore.PoisonousFood.TimeFor").asLong()}")
        e.player.addScoreboardTag("StarLightCore.PoisonousFood.Killer:${nbt.getDeep("StarLightCore.PoisonousFood.Killer").asString()}")
    }

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        checkDeath(e.player)
    }

    @EventHandler
    fun onDeath(e: PlayerDeathEvent) {
        if (!e.entity.scoreboardTags.stream().anyMatch { it.startsWith("StarLightCore.PoisonousFood.TimeOn") }) return
        e.deathMessage = e.entity.asLangText("Features-PoisonousFood-DeathMessage", e.entity.displayName, Bukkit.getOfflinePlayer(UUID.fromString(e.entity.scoreboardTags.find { it.startsWith("StarLightCore.PoisonousFood.Killer") }!!.split(':')[1])).name!!)
        e.entity.removeScoreboardTag(e.entity.scoreboardTags.find { it.startsWith("StarLightCore.PoisonousFood.TimeOn") }!!)
        e.entity.removeScoreboardTag(e.entity.scoreboardTags.find { it.startsWith("StarLightCore.PoisonousFood.Killer") }!!)
    }

    private fun checkDeath(player: Player) {
        if (!player.scoreboardTags.stream().anyMatch { it.startsWith("StarLightCore.PoisonousFood.TimeOn") }) return
        if (player.scoreboardTags.find { it.startsWith("StarLightCore.PoisonousFood.TimeOn") }!!
                        .split(':')[1]
                        .toLong() <= Bukkit.getWorlds()[0].fullTime) player.health = 0.0
    }
}