package com.ashkiano.arrowtorch;

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

public class ArrowListener implements Listener {

    private final ArrowTorch plugin;

    public ArrowListener(ArrowTorch plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onArrowLand(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow)) return;

        Arrow arrow = (Arrow) event.getEntity();
        if (!arrow.hasMetadata(ArrowTorch.bowLore)) return;

        if (event.getHitBlock() == null) return;

        Block blockHit = event.getHitBlock();
        if (event.getEntity() instanceof Arrow && event.getEntity().getShooter() instanceof Player) {

            if (arrow.hasMetadata(ArrowTorch.bowLore)) {
                BlockFace blockFace = event.getHitBlockFace();

                Block toPlace = blockHit.getRelative(blockFace);

                if (toPlace.getType() == Material.AIR) {
                    if (blockFace == BlockFace.UP) {
                        toPlace.setType(Material.TORCH);
                    } else {
                        toPlace.setType(Material.WALL_TORCH);
                        BlockData data = toPlace.getBlockData();
                        if (data instanceof org.bukkit.block.data.Directional) {
                            ((org.bukkit.block.data.Directional) data).setFacing(blockFace);
                            toPlace.setBlockData(data);
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
}