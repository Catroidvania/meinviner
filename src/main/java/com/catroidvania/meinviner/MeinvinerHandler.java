package com.catroidvania.meinviner;

import com.fox2code.foxevents.EventHandler;
import com.fox2code.foxloader.event.GlobalTickEvent;
import com.fox2code.foxloader.event.interaction.PlayerBreakBlockEvent;
import com.fox2code.foxloader.event.interaction.PlayerStartBreakBlockEvent;
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
import java.util.List;

public class MeinvinerHandler {

    public static final MeinvinerHandler INSTANCE = new MeinvinerHandler();

    public static Meinviner.MeinvinerConfig CONFIG = Meinviner.CONFIG;
    public List<MeinvinerBlocklist> blockCaches = new ArrayList<>();
    public static World WORLD;// = Minecraft.getInstance().theWorld;
    public Block selectedBlock;

    @EventHandler
    public void onTick(GlobalTickEvent event) {
        if (!CONFIG.enabled) {
            blockCaches.clear();
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
            if (event.getEntityPlayer().canHarvestBlock(block)) {
                selectedBlock = block;
            }
        }
    }

    @EventHandler
    public void onPlayerBreakBlockEvent(PlayerBreakBlockEvent event) {
        if (!CONFIG.enabled) return;

        EntityPlayer player = event.getEntitySource();
        ItemStack item = event.getHeldItem();
        if (selectedBlock != null && !player.capabilities.isCreativeMode && player.isSneaking()) {
            if (Meinviner.CONFIG.correctToolOnly && item != null && item.getItem() instanceof ItemTool && !((ItemTool)item.getItem()).isToolEffectiveOnBlock(selectedBlock)) {
                return;
            }
            blockCaches.add(new MeinvinerBlocklist(WORLD, selectedBlock, event.getEntitySource(), event.getHeldItem(), event.getX(), event.getY(), event.getZ(), CONFIG.maxBlocks - 1));
        }
    }
}
