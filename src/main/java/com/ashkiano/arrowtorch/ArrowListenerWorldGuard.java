package com.ashkiano.arrowtorch;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.metadata.FixedMetadataValue;

public class ArrowListenerWorldGuard implements Listener {

    private final ArrowTorch plugin;

    public ArrowListenerWorldGuard(ArrowTorch plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onArrowLand(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow)) return;

        Arrow arrow = (Arrow) event.getEntity();
        if (!arrow.hasMetadata(ArrowTorch.bowLore)) return;

        if (event.getHitBlock() == null) return;

        if (event.getEntity().getShooter() instanceof Player) {
            Player player = (Player) event.getEntity().getShooter();

            if (arrow.hasMetadata(ArrowTorch.bowLore)) {
                BlockFace blockFace = event.getHitBlockFace();
                Block blockHit = event.getHitBlock();
                Block toPlace = blockHit.getRelative(blockFace);

                if (toPlace.getType() == Material.AIR && canPlaceTorch(player, toPlace)) {
                    if (blockFace == BlockFace.UP) {
                        toPlace.setType(Material.TORCH);
                        logPlacement(player, toPlace, Material.TORCH);
                    } else {
                        toPlace.setType(Material.WALL_TORCH);
                        BlockData data = toPlace.getBlockData();
                        if (data instanceof org.bukkit.block.data.Directional) {
                            ((org.bukkit.block.data.Directional) data).setFacing(blockFace);
                            toPlace.setBlockData(data);
                            logPlacement(player, toPlace, Material.WALL_TORCH);
                        }
                    }
                }

                arrow.remove();
            }
        }
    }

    @EventHandler
    public void onBowShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        ItemStack bow = event.getBow();

        if (bow == null) return;

        if (bow.hasItemMeta() && bow.getItemMeta().hasLore() && bow.getItemMeta().getLore().contains(ArrowTorch.bowLore)) {
            event.setConsumeItem(false);

            if (player.getInventory().contains(Material.TORCH)) {
                player.getInventory().removeItem(new ItemStack(Material.TORCH, 1));
                event.getProjectile().setMetadata(ArrowTorch.bowLore, new FixedMetadataValue(plugin, true));
            } else {
                player.sendMessage("You don't have any torches!");
                event.setCancelled(true);
            }

            player.updateInventory();
        }
    }

    private void logPlacement(Player player, Block block, Material type) {
        if (plugin.getServer().getPluginManager().isPluginEnabled("CoreProtect")) {
            CoreProtectAPI coreProtect = ((CoreProtect) plugin.getServer().getPluginManager().getPlugin("CoreProtect")).getAPI();
            if (coreProtect != null) {
                coreProtect.logPlacement(player.getName(), block.getLocation(), type, block.getBlockData());
            }
        }
    }

    private boolean canPlaceTorch(Player player, Block block) {
        if (plugin.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(block.getWorld()));

            if (regions != null) {
                BlockVector3 vector = BlockVector3.at(block.getX(), block.getY(), block.getZ());
                ApplicableRegionSet regionSet = regions.getApplicableRegions(vector);
                return regionSet.testState(WorldGuardPlugin.inst().wrapPlayer(player), Flags.BUILD);
            }
        }
        // Pokud WorldGuard není dostupný, nebo region manager není definován, defaultně povolit
        return true;
    }
}