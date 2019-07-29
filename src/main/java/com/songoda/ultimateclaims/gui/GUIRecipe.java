package com.songoda.ultimateclaims.gui;

import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.utils.Methods;
import com.songoda.ultimateclaims.utils.ServerVersion;
import com.songoda.ultimateclaims.utils.gui.AbstractGUI;
import com.songoda.ultimateclaims.utils.settings.Setting;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GUIRecipe extends AbstractGUI {

    private UltimateClaims plugin;

    public GUIRecipe(Player player) {
        super(player);
        this.plugin = UltimateClaims.getInstance();

        init(Methods.formatTitle(plugin.getLocale().getMessage("interface.recipe.title").getMessage()), 27);
    }

    @Override
    protected void constructGUI() {

        List<String> recipe = Setting.POWERCELL_RECIPE.getStringList();
        for (String line : recipe) {
            String[] split = line.split(":");
            createButton(Integer.parseInt(split[0]), Material.valueOf(split[1]), split[1]);
        }
    }

    @Override
    protected void registerClickables() {
    }

    @Override
    protected void registerOnCloses() {

    }
}