package kim.minecraft.starlightcore.features

import kim.minecraft.starlightcore.StarLightCore
import org.bukkit.entity.Entity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.event.vehicle.VehicleExitEvent
import taboolib.platform.util.sendLang

object MultiPassengerVehicles : Listener {

    private val maxPlayer = StarLightCore.config.getInt("Features.MultiPassengerVehicles.MaxPlayer", 20)

    @EventHandler
    fun joinVehicle(e: PlayerInteractEntityEvent) {
        val rightClicked = e.rightClicked
        if (rightClicked.isInsideVehicle) {
            val passengers = rightClicked.passengers
            if (passengers.isEmpty()) {
                rightClicked.addPassenger(e.player)
            } else {
                var player = 0
                passengers.iterator().forEachRemaining {
                    if (it != e.player && it.passengers.isEmpty()) {
                        if (player <= maxPlayer)
                            it.addPassenger(e.player)
                        else
                         e.player.sendLang("Features-MultiPassengerVehicles-Full")
                    } else
                        player++
                }
            }
        }
    }

    @EventHandler
    fun leaveVehicle(e: VehicleExitEvent) {
        var entityPastBy: Entity = e.exited
        e.exited.passengers.iterator().forEachRemaining {
            entityPastBy.removePassenger(it)
            entityPastBy = it
        }
    }

    @EventHandler
    fun onShift(e: PlayerToggleSneakEvent) {
        var entityPastBy: Entity = e.player
        e.player.passengers.iterator().forEachRemaining {
            entityPastBy.removePassenger(it)
            entityPastBy = it
        }
    }
}