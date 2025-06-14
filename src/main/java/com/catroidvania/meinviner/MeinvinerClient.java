package com.catroidvania.meinviner;

import com.fox2code.foxevents.EventHandler;
import com.fox2code.foxloader.event.GlobalTickEvent;
import com.fox2code.foxloader.event.interaction.PlayerBreakBlockEvent;
import com.fox2code.foxloader.event.interaction.PlayerStartBreakBlockEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.common.block.Block;
import net.minecraft.common.block.Blocks;
import net.minecraft.common.world.World;

import java.util.ArrayList;
import java.util.List;

public class MeinvinerClient {

    public static final MeinvinerClient INSTANCE = new MeinvinerClient();

    public static Meinviner.MeinvinerConfig CONFIG = Meinviner.CONFIG;
    public List<MeinvinerBlocklist> blockCaches = new ArrayList<>();
    public static World WORLD = Minecraft.getInstance().theWorld;
    public Block selectedBlock;

    @EventHandler
    public void onTick(GlobalTickEvent event) {
        if (!CONFIG.enabled) {
            blockCaches.clear();
            return;
        }

        WORLD = Minecraft.getInstance().theWorld;

        if (WORLD == null) {
            blockCaches.clear();
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

        if (selectedBlock != null && !event.getEntitySource().capabilities.isCreativeMode && event.getEntitySource().isSneaking()) {
            blockCaches.add(new MeinvinerBlocklist(WORLD, selectedBlock, event.getEntitySource(), event.getHeldItem(), event.getX(), event.getY(), event.getZ(), CONFIG.maxBlocks - 1));
        }
    }
}
