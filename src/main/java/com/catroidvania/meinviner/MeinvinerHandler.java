package com.catroidvania.meinviner;

import com.fox2code.foxevents.EventHandler;
import com.fox2code.foxloader.event.GlobalTickEvent;
import com.fox2code.foxloader.event.interaction.PlayerBreakBlockEvent;
import com.fox2code.foxloader.event.interaction.PlayerStartBreakBlockEvent;
import com.fox2code.foxloader.event.world.WorldTickEvent;
import com.fox2code.foxloader.launcher.FoxLauncher;
import net.minecraft.client.Minecraft;
import net.minecraft.common.block.Block;
import net.minecraft.common.block.Blocks;
import net.minecraft.common.entity.player.EntityPlayer;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.item.children.ItemTool;
import net.minecraft.common.world.World;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MeinvinerHandler {

    public static final MeinvinerHandler INSTANCE = new MeinvinerHandler();

    public static Meinviner.MeinvinerConfig CONFIG = Meinviner.CONFIG;
    public List<MeinvinerBlocklist> blockCaches = new ArrayList<>();
    public static World WORLD;// = Minecraft.getInstance().theWorld;
    public HashMap<EntityPlayer, Block> selectedBlocks = new HashMap<>();
    //public Block selectedBlock;

    @EventHandler
    public void onTick(GlobalTickEvent event) {
        if (!CONFIG.enabled) {
            blockCaches.clear();
            selectedBlocks.clear();
            return;
        }

        if (FoxLauncher.isClient()) {
            if (Minecraft.theMinecraft.isMultiplayerWorld()) {
                return;
            }
            WORLD = Minecraft.getInstance().theWorld;
        } else if (FoxLauncher.isServer()) {
            WORLD = MinecraftServer.getInstance().getWorld();
        } else {
            WORLD = null;
        }

        if (WORLD == null) {
            blockCaches.clear();
            return;
        }
    }

    @EventHandler
    public void onWorldTickEvent(WorldTickEvent event) {
        List<MeinvinerBlocklist> copyList = new ArrayList<>(blockCaches);
        for (MeinvinerBlocklist bc : copyList) {
            if (!bc.updateBlocks()) {
                blockCaches.remove(bc);
            }
        }
    }

    @EventHandler
    public void onPlayerStarBreakBlockEvent(PlayerStartBreakBlockEvent event) {
        if (!CONFIG.enabled) return;

        if (WORLD != null) {
            Block block = Blocks.BLOCKS_LIST[WORLD.getBlockId(event.getX(), event.getY(), event.getZ())];
            //if (event.getEntityPlayer().canHarvestBlock(block)) {
                selectedBlocks.put(event.getEntityPlayer(), block);
                //selectedBlock = block;
            //}
        }
    }

    @EventHandler
    public void onPlayerBreakBlockEvent(PlayerBreakBlockEvent event) {
        if (!CONFIG.enabled) return;

        EntityPlayer player = event.getEntitySource();
        ItemStack item = event.getHeldItem();
        Block block = selectedBlocks.get(player);
        if (block != null && !player.capabilities.isCreativeMode && player.isSneaking()) {
            if (Meinviner.CONFIG.correctToolOnly) {
                if (item == null) return;

                boolean isTool = item.getItem() instanceof ItemTool;
                if (!isTool || !((ItemTool)item.getItem()).isToolEffectiveOnBlock(block)) {
                    return;
                }
            }
            blockCaches.add(new MeinvinerBlocklist(WORLD, block, player, item, event.getX(), event.getY(), event.getZ(), CONFIG.maxBlocks - 1));
        }
    }
}
