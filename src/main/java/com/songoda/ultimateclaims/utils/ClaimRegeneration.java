package com.songoda.ultimateclaims.utils;

import com.fastasyncworldedit.core.FaweAPI;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.songoda.core.compatibility.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;

public class ClaimRegeneration {

    public static void regenerateChunk(Chunk chunk) {
        if (isFaweAvailable()) {
            try {
                World faweWorld = FaweAPI.getWorld(chunk.getWorld().getName());

                // Get world height and define region
                org.bukkit.World world = chunk.getWorld();
                BlockVector3 pos1 = BlockVector3.at(chunk.getX() * 16, world.getMinHeight(), chunk.getZ() * 16);
                BlockVector3 pos2 = BlockVector3.at((chunk.getX() + 1) * 16 - 1, world.getMaxHeight() - 1, (chunk.getZ() + 1) * 16 - 1);
                Region region = new CuboidRegion(faweWorld, pos1, pos2);

                //Trigger regeneration
                boolean success = faweWorld.regenerate(region, faweWorld);

                // Log results and fallback if necessary
                if (success) {
                    Bukkit.getLogger().warning("[UltimateClaims] Successfully regenerated chunk at x:" + chunk.getX() + " z:" + chunk.getZ() + " in world " + chunk.getWorld().getName() + " using FAWE API.");
                } else {
                    Bukkit.getLogger().warning("[UltimateClaims] FAWE regeneration failed. Falling back.");
                    fallbackRegeneration(chunk);
                }

                faweWorld.refreshChunk(chunk.getX(), chunk.getZ());
            } catch (Exception e) {
                Bukkit.getLogger().warning("[UltimateClaims] Error regenerating chunk with FAWE: " + e.getMessage());
                e.printStackTrace();
                fallbackRegeneration(chunk);
            }
        } else {
            fallbackRegeneration(chunk);
        }
    }

    private static boolean isFaweAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit");
    }

    private static void fallbackRegeneration(Chunk chunk) {
        try {
            // Skip Bukkit fallback regeneration for 1.21+
            if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_21)) {
                Bukkit.getLogger().warning("[UltimateClaims] Skipping Bukkit fallback regeneration: not supported on 1.21+");
                return;
            }

            // Unload the chunk if necessary and regenerate for older versions
            if (chunk.isLoaded()) chunk.unload(true);
            if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_8)) {
                chunk.getWorld().regenerateChunk(chunk.getX(), chunk.getZ());
                Bukkit.getLogger().warning("[UltimateClaims] Regenerating chunk at x:" + chunk.getX() + " z:" + chunk.getZ() + " in world " + chunk.getWorld().getName() + " (fallback).");
            } else {
                Bukkit.getLogger().warning("[UltimateClaims] Chunk regeneration fallback skipped for this version.");
            }
        } catch (UnsupportedOperationException e) {
            Bukkit.getLogger().warning("[UltimateClaims] Skipping Bukkit regeneration: not supported on this version.");
        }
    }
}
