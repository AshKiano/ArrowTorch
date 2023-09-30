package com.ashkiano.arrowtorch;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ArrowTorch extends JavaPlugin {

    private String permission;  // Variable to store the permission
    static String bowLore;  // Variable to store the lore
    static String bowName;  // Variable to store the name

    @Override
    public void onEnable() {
        saveDefaultConfig();
        permission = getConfig().getString("torchbow_permission", "arrowtorch.use");
        bowLore = getConfig().getString("torchbow_lore", "Special Torch Bow");
        bowName = getConfig().getString("torchbow_name", "Torch Bow");  // Load bow name from config

        getServer().getPluginManager().registerEvents(new ArrowListener(this), this);
        Metrics metrics = new Metrics(this, 19517);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("gettorchbow")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                // Check for permission
                if (!player.hasPermission(permission)) {
                    player.sendMessage("You do not have permission to use this command.");
                    return true;
                }

                ItemStack bow = new ItemStack(Material.BOW);
                ItemMeta meta = bow.getItemMeta();

                if (meta != null) {
                    meta.setDisplayName(bowName);  // Set the bow name
                    meta.setLore(Arrays.asList(bowLore));
                    meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
                    meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
                    bow.setItemMeta(meta);
                }

                player.getInventory().addItem(bow);
                player.sendMessage("You've received the special enchanted bow!");
                return true;
            }
        }
        return false;
    }
}
