package com.ololee.quake;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Utils.MOD_ID)
public class Quake {
    public Quake() {
        HammerRegistry.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
