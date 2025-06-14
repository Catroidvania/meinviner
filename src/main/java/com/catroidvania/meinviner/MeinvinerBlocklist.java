package com.catroidvania.meinviner;

import net.minecraft.common.block.Block;
import net.minecraft.common.entity.player.EntityPlayer;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.util.math.Vec3D;
import net.minecraft.common.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MeinvinerBlocklist {

    public Block block;
    public EntityPlayer player;
    public World world;
    public ItemStack heldItem;
    public List<Vec3D> breakList;
    public HashMap<Vec3D, Boolean> inList;
    public int blocksBroken;
    public int maxBlocks;

    public MeinvinerBlocklist(World world, Block block, EntityPlayer player, ItemStack heldItem, int x, int y, int z, int maxBlocks) {
        this.world = world;
        this.block = block;
        this.player = player;
        this.heldItem = heldItem;
        this.maxBlocks = maxBlocks;
        blocksBroken = 0;
        breakList = new ArrayList<>();
        inList = new HashMap<>();
        createBreakList(x, y, z);
    }

    public void createBreakList(int x, int y, int z) {
        if (block == null || player == null || world == null) return;
        searchNewBlocks(x, y, z);
    }

    public void searchNewBlocks(int x, int y, int z) {
        if (Meinviner.CONFIG.diagonals) {
            for (int yo = -1; yo < 2; yo++) {
                for (int xo = -1; xo < 2; xo++) {
                    for (int zo = -1; zo < 2; zo++) {
                        if (xo == 0 && yo == 0 && zo == 0) continue;
                        addBlocks(x + xo, y + yo, z + zo);
                    }
                }
            }
        } else {
            addBlocks(x, y + 1, z);
            addBlocks(x + 1, y, z);
            addBlocks(x, y, z + 1);
            addBlocks(x - 1, y, z);
            addBlocks(x, y, z - 1);
            addBlocks(x, y - 1, z);
        }
    }

    public void addBlocks(int x, int y, int z) {
        if (breakList.size() >= 128) return;

        Vec3D coords = new Vec3D(x, y, z);

        if (world.getBlockId(x, y, z) == block.blockID && !inList.containsKey(coords)) {
            breakList.add(coords);
            inList.put(coords, true);
        }
    }

    public boolean breakBlock(Vec3D pos) {
        if (blocksBroken >= maxBlocks) return false;

        int x = (int) pos.xCoord;
        int y = (int) pos.yCoord;
        int z = (int) pos.zCoord;

        if (world.getBlockId(x, y, z) == 0) {
            breakList.remove(pos);
            return true;
        }

        if (!Meinviner.CONFIG.allowToolBreak && heldItem != null && heldItem.isItemStackDamageable() && heldItem.getItemDamage() == heldItem.getMaxDamage()) {
            return false;
        }

        if (player.canHarvestBlock(block) && player.getHeldItem() == heldItem) {
            if (world.getBlockId(x, y, z) == block.blockID) {
                if (world.setBlockWithNotify(x, y, z, 0)) {
                    int metadata = world.getBlockMetadata(x, y, z);
                    world.playAuxSFX(2001, x, y, z, block.blockID + (metadata << 16));

                    block.onBlockDestroyedByPlayer(world, x, y, z, metadata);
                    block.harvestBlock(world, player, x, y, z, metadata);

                    if (heldItem != null) {
                        heldItem.damageItem(1, null, false);
                        if (heldItem.stackSize < 1 || heldItem.getItemDamage() > heldItem.getMaxDamage()) {
                            heldItem.func_1097_a(player);  // item break function perhaps?
                            player.destroyCurrentEquippedItem();
                        }
                    }

                    blocksBroken += 1;
                    breakList.remove(pos);
                    searchNewBlocks(x, y, z);
                }
            }
        } else {
            return false;
        }
        return true;
    }

    public boolean updateBlocks() {
        if (breakList.isEmpty()) return false;

        if (Meinviner.CONFIG.oneAtATime) {
            return breakBlock(breakList.getFirst());
        } else {
            List<Vec3D> listCopy = new ArrayList<>(breakList);
            for (Vec3D pos : listCopy) {
                if (!breakBlock(pos)) {
                    return false;
                }
            }
        }
        return true;
    }

}
