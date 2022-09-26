package kim.minecraft.starlightcore.features

import kim.minecraft.starlightcore.StarLightCore
import kim.minecraft.starlightcore.utils.WorldInteraction.isUnderTree
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import taboolib.common.platform.function.submit
import taboolib.platform.util.sendLang
import kotlin.random.Random

object TreeThunder {

    private val intervalTicks: Long = StarLightCore.config.getLong("Features.TreeThunder.IntervalTicks", 100)
    private val chance: Double = StarLightCore.config.getDouble("Features.TreeThunder.Chance", 0.2)
    private val helmetList: List<Material> = StarLightCore.config.getStringList("Features.TreeThunder.HelmetList").map { Material.getMaterial(it)!! }

    fun run() {
        submit(delay = 10, period = intervalTicks) {
            Bukkit.getWorlds().forEach { world ->
                if (world.hasStorm() && world.isThundering) {
                    world.loadedChunks.forEach { chunk ->
                        chunk.entities.forEach {
                            if (it is LivingEntity
                                && it.equipment?.helmet != null
                                && it.equipment!!.helmet!!.type in helmetList
                                && it.isUnderTree()
                                && Random.nextDouble() <= chance) {
                                world.strikeLightning(it.location)
                                if (it is Player)
                                    it.sendLang("Features-TreeThunder-Hit")
                            }
                        }
                    }
                }
            }
        }
    }
}