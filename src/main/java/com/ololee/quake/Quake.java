package com.ololee.quake;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.ololee.quake.constants.Constants;
import com.ololee.quake.item.hammer.entity.HammerEntity;
import com.ololee.quake.item.hammer.render.HammerRenderer;
import com.ololee.quake.utils.HomeHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.SystemToast;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.TridentRenderer;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.util.FoodStats;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.util.HashMap;

@Mod(Utils.MOD_ID)
public class Quake {


    private static final Logger LOGGER = LogManager.getLogger();
    private static HashMap<String, Vector3d> playerHomeMap = new HashMap<>();





    public Quake() {
        HammerRegistry.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void init(final FMLServerStartingEvent event) {
        try {
            playerHomeMap = HomeHelper.constructPlayerHomeData(HomeHelper.readFile());
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }



    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        init(event);
    }

    @SubscribeEvent
    public void onPlayerJoinIn(PlayerEvent.PlayerLoggedInEvent playerLoggedInEvent) {
        SystemToast toast = new SystemToast(SystemToast.Type.NARRATOR_TOGGLE,
                new StringTextComponent(TextFormatting.RED + String.format("Hello, Mr ",playerLoggedInEvent.getEntity().getName())),
                new StringTextComponent(""));
        Minecraft.getInstance().getToasts().addToast(toast);
    }


    @SubscribeEvent
    public void healthMax(RegisterCommandsEvent commandEvent) {
        CommandDispatcher<CommandSource> dispatcher = commandEvent.getDispatcher();
        LiteralCommandNode<CommandSource> cmd = dispatcher.register(Commands.literal("healthMax").executes(context -> {
            Entity entity = context.getSource().getEntity();
            try {
                PlayerEntity playerEntity = (PlayerEntity) entity;
                FoodStats foodData = playerEntity.getFoodData();
                int foodLevel = foodData.getFoodLevel();
                String currentFoodLevelString = String.format("your current food level is %d", foodLevel);
                StringTextComponent currentFoodLevelComment = new StringTextComponent(currentFoodLevelString);
                playerEntity.sendMessage(currentFoodLevelComment, playerEntity.getUUID());
                foodData.setFoodLevel(20);
                playerEntity.setHealth(20);
            } catch (ClassCastException e) {
                StringTextComponent errorTips = new StringTextComponent("u must be a person~~");
                entity.sendMessage(errorTips, entity.getUUID());
            }
            return 0;
        }));
    }



    @SubscribeEvent
    public void setHome(RegisterCommandsEvent commandsEvent) {
        CommandDispatcher<CommandSource> dispatcher = commandsEvent.getDispatcher();
        dispatcher.register(Commands.literal("setHome").executes(context -> {
            CommandSource source = context.getSource();
            Entity entity = source.getEntity();
            try {
                PlayerEntity playerEntity = (PlayerEntity) entity;
                Vector3d targetPosition = playerEntity.position();
                StringTextComponent setHomeTips = new StringTextComponent(String.format("set home at postion(%g,%g,%g", targetPosition.x, targetPosition.y, targetPosition.z));
                entity.sendMessage(setHomeTips, entity.getUUID());
                playerHomeMap.put(playerEntity.getName().getString(), targetPosition);
                saveHomeData();
            } catch (ClassCastException e) {
                StringTextComponent errorTips = new StringTextComponent("u must be a person~~");
                entity.sendMessage(errorTips, entity.getUUID());
            }
            return 0;
        }));
    }



    @SubscribeEvent
    public void goHome(RegisterCommandsEvent commandsEvent) {
        CommandDispatcher<CommandSource> dispatcher = commandsEvent.getDispatcher();
        dispatcher.register(Commands.literal("goHome").executes(context -> {
            CommandSource source = context.getSource();
            Entity entity = source.getEntity();
            try {
                PlayerEntity playerEntity = (PlayerEntity) entity;
                Vector3d targetPosition = playerHomeMap.get(playerEntity.getName().getString());
                if (targetPosition == null) {
                    StringTextComponent errorTips = new StringTextComponent("you hadn't set your home yet. please setHome first!!!");
                    entity.sendMessage(errorTips, entity.getUUID());
                } else {
                    StringTextComponent transTips = new StringTextComponent("Welcome home, Sir.");
                    entity.sendMessage(transTips, entity.getUUID());
                    playerEntity.teleportTo(targetPosition.x, targetPosition.y, targetPosition.z);
                }
            } catch (ClassCastException e) {
                StringTextComponent errorTips = new StringTextComponent("u must be a person~~");
                entity.sendMessage(errorTips, entity.getUUID());
            }

            return 0;
        }));
    }


    private void saveHomeData() {
        try {
            HomeHelper.writeFile(HomeHelper.seralizePlayersHomeData(playerHomeMap));
        } catch (Exception e) {
            LOGGER.error("write File failed....");
            e.printStackTrace();
        }
    }
}
