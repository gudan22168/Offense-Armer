package com.cmcy.offense_armer;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = Offense_armer.MODID, value = Dist.CLIENT)
public class ClientSetup {
    public static final KeyMapping TOGGLE_ABILITY_KEY = new KeyMapping(
            "key.offense_armer.toggle_ability",
            InputConstants.KEY_X,
            "key.categories.offense_armer"
    );

    public static final KeyMapping BURST_KEY = new KeyMapping(
            "key.offense_armer.burst",
            InputConstants.KEY_R,
            "key.categories.offense_armer"
    );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_ABILITY_KEY);
        event.register(BURST_KEY);
    }

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(Offense_armer.MODID, "nitrogen_bar"),
                NitrogenBarOverlay::render
        );
    }

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.NITROGEN_GENERATOR.get(), NitrogenGeneratorScreen::new);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.MACH_RING.get(), MachRingParticle.Provider::new);
        event.registerSpriteSet(ModParticles.NITRO_GLOW.get(), NitroGlowParticle.Provider::new);
    }

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            renderer.addLayer(new NitrogenShellLayer(renderer));
        }
    }
}
