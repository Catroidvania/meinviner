package com.catroidvania.meinviner;

import com.fox2code.foxloader.config.ConfigEntry;
import com.fox2code.foxloader.event.FoxLoaderEvents;
import com.fox2code.foxloader.loader.Mod;

public class Meinviner extends Mod {

    public static MeinvinerConfig CONFIG = new MeinvinerConfig();

    @Override
    public void onPreInit() {
        setConfigObject(CONFIG);
        System.out.println("Meinviner initialised!");
    }

    @Override
    public void onPostInit() {
        FoxLoaderEvents.INSTANCE.registerEvents(MeinvinerHandler.INSTANCE);
    }

    public static class MeinvinerConfig {
        @ConfigEntry(configName = "Enabled", configComment = "Enable/Disable Veinminer")
        public boolean enabled = true;

        @ConfigEntry(configName = "Max Blocks Mined", configComment = "Max amount of blocks that can be veinmined at a time", lowerBounds = 0, upperBounds = 64)
        public int maxBlocks = 32;

        @ConfigEntry(configName = "Mine Diagonals", configComment = "If false veinminer only checks adjacent blocks")
        public boolean diagonals = true;

        @ConfigEntry(configName = "Allow Tool Break", configComment = "Will leave tools at 0 durability when false")
        public boolean allowToolBreak = false;

        @ConfigEntry(configName = "Proper Tools Only", configComment = "Only veinmine with the correct tools")
        public boolean correctToolOnly = true;

        @ConfigEntry(configName = "Slow Break", configComment = "Breaks 1 block per tick, if false then breaks more")
        public boolean oneAtATime = false;
    }
}
