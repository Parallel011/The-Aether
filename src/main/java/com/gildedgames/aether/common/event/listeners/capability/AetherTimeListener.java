package com.gildedgames.aether.common.event.listeners.capability;

import com.gildedgames.aether.common.event.hooks.CapabilityHooks;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Listener for Forge events to handle syncing the data for the Aether Time capability.
 */
@Mod.EventBusSubscriber
public class AetherTimeListener {
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getPlayer();
        CapabilityHooks.AetherTimeHooks.login(player);
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getPlayer();
        CapabilityHooks.AetherTimeHooks.changeDimension(player);
    }
}
