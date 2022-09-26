package kim.minecraft.starlightcore.features

import kim.minecraft.starlightcore.StarLightCore
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.TNTPrimed
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import taboolib.platform.util.sendLang
import kotlin.random.Random

object UnsafeTNT : Listener {

    private val misfireChange = StarLightCore.config.getDouble("Features.UnsafeTNT.MisfireChange", 0.1)
    private val sabotageChange = StarLightCore.config.getDouble("Features.UnsafeTNT.SabotageChange", 0.1)

    @EventHandler
    fun onExplodeTNT(e: PlayerInteractEvent) {
        if (e.action != Action.RIGHT_CLICK_BLOCK || e.material != Material.FLINT_AND_STEEL || e.clickedBlock?.type != Material.TNT) return
        if (Random.nextDouble() <= misfireChange) {
            e.player.sendLang("Features-UnsafeTNT-MisfireMessage")
            e.isCancelled = true
            e.clickedBlock!!.type = Material.AIR
            e.player.world.spawn(e.clickedBlock!!.location, TNTPrimed::class.java).also {
                it.source = e.player
                Bukkit.getScheduler().runTaskLater(StarLightCore.plugin, it::remove, (it.fuseTicks - 1).toLong())
            }
        }
    }

    @EventHandler
    fun onBreakTNT(e: BlockBreakEvent) {
        if (e.block.type != Material.TNT) return
        if (Random.nextDouble() <= sabotageChange) {
            e.player.sendLang("Features-UnsafeTNT-SabotageMessage")
            e.player.world.spawn(e.block.location, TNTPrimed::class.java).source = e.player
        }
    }
}