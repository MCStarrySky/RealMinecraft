package kim.minecraft.starlightcore

import kim.minecraft.starlightcore.features.*
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import taboolib.common.platform.Platform
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.info
import taboolib.common.platform.function.pluginVersion
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.metrics.Metrics
import taboolib.platform.BukkitPlugin

object StarLightCore : Plugin() {

    @Config(migrate = true)
    lateinit var config: Configuration

    val plugin by lazy {
        BukkitPlugin.getInstance()
    }

    private fun isFeatureEnable(featureName: String): Boolean {
        return config.getBoolean("Features.$featureName.Enable", false).also {
            if (it) info("已启用特性 $featureName")
            else info("已禁用特性 $featureName")
        }
    }

    override fun onEnable() {
        registerFeatures()
        info("注册 bStats 监听")
        Metrics(9875, pluginVersion, Platform.BUKKIT)
    }

    private fun registerFeatures() {
        if (isFeatureEnable("BreakableBucket"))
            Bukkit.getPluginManager().registerEvents(BreakableBucket, plugin)
        if (isFeatureEnable("MultiPassengerVehicles"))
            Bukkit.getPluginManager().registerEvents(MultiPassengerVehicles, plugin)
        if (isFeatureEnable("TreeThunder"))
            TreeThunder.run()
        if (isFeatureEnable("PoisonousFood"))
            Bukkit.getPluginManager().registerEvents(PoisonousFood, plugin)
        if (isFeatureEnable("RealPickup"))
            Bukkit.getPluginManager().registerEvents(RealPickup, plugin)
        if (isFeatureEnable("UnsafeTNT"))
            Bukkit.getPluginManager().registerEvents(UnsafeTNT, plugin)
    }

    override fun onDisable() {
        HandlerList.unregisterAll(plugin)
    }
}
