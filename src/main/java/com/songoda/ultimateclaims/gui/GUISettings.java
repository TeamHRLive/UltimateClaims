package com.songoda.ultimateclaims.gui;

import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.member.ClaimPerm;
import com.songoda.ultimateclaims.member.ClaimRole;
import com.songoda.ultimateclaims.utils.Methods;
import com.songoda.ultimateclaims.utils.ServerVersion;
import com.songoda.ultimateclaims.utils.gui.AbstractGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GUISettings extends AbstractGUI {

    private UltimateClaims plugin;
    private Player player;
    private Claim claim;
    private ClaimRole role;
    private boolean back;

    public GUISettings(Player player, Claim claim, ClaimRole role, boolean back) {
        super(player);
        this.player = player;
        this.claim = claim;
        this.role = role;
        this.plugin = UltimateClaims.getInstance();
        this.back = back;

        init(Methods.formatTitle(plugin.getLocale().getMessage("interface.settings.title")
                .processPlaceholder("role", Methods.formatText(role.toString().toLowerCase(), true)).getMessage()), 27);
    }

    @Override
    protected void constructGUI() {
        inventory.clear();
        resetClickables();
        registerClickables();

        inventory.setItem(1, Methods.getBackgroundGlass(true));
        inventory.setItem(7, Methods.getBackgroundGlass(true));
        inventory.setItem(9, Methods.getBackgroundGlass(true));
        inventory.setItem(17, Methods.getBackgroundGlass(true));
        inventory.setItem(18, Methods.getBackgroundGlass(true));
        inventory.setItem(26, Methods.getBackgroundGlass(true));
        inventory.setItem(19, Methods.getBackgroundGlass(true));
        inventory.setItem(25, Methods.getBackgroundGlass(true));

        inventory.setItem(2, Methods.getBackgroundGlass(false));
        inventory.setItem(3, Methods.getBackgroundGlass(false));
        inventory.setItem(4, Methods.getBackgroundGlass(false));
        inventory.setItem(5, Methods.getBackgroundGlass(false));
        inventory.setItem(6, Methods.getBackgroundGlass(false));
        inventory.setItem(20, Methods.getBackgroundGlass(false));
        inventory.setItem(21, Methods.getBackgroundGlass(false));
        inventory.setItem(22, Methods.getBackgroundGlass(false));
        inventory.setItem(23, Methods.getBackgroundGlass(false));
        inventory.setItem(24, Methods.getBackgroundGlass(false));


        ItemStack exit = new ItemStack(plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.OAK_FENCE_GATE : Material.valueOf("FENCE_GATE"));
        ItemMeta exitMeta = exit.getItemMeta();
        exitMeta.setDisplayName(plugin.getLocale().getMessage("general.interface.back").getMessage());
        exit.setItemMeta(exitMeta);

        ItemStack blockBreak = new ItemStack(Material.IRON_PICKAXE);
        ItemMeta breakMeta = blockBreak.getItemMeta();
        breakMeta.setDisplayName(plugin.getLocale().getMessage("interface.settings.breaktitle").getMessage());
        List<String> breakLore = new ArrayList<>();
        String[] breakSplit = plugin.getLocale().getMessage("general.interface.current")
                .processPlaceholder("current", role == ClaimRole.MEMBER
                        ? claim.getMemberPermissions().hasPermission(ClaimPerm.BREAK) : claim.getVisitorPermissions().hasPermission(ClaimPerm.BREAK))
                .getMessage().split("\\|");
        for (String line : breakSplit) breakLore.add(line);
        breakMeta.setLore(breakLore);
        blockBreak.setItemMeta(breakMeta);

        ItemStack place = new ItemStack(Material.STONE);
        ItemMeta placeMeta = place.getItemMeta();
        placeMeta.setDisplayName(plugin.getLocale().getMessage("interface.settings.placetitle").getMessage());
        List<String> placeLore = new ArrayList<>();
        String[] placeSplit = plugin.getLocale().getMessage("general.interface.current")
                .processPlaceholder("current", role == ClaimRole.MEMBER
                        ? claim.getMemberPermissions().hasPermission(ClaimPerm.PLACE) : claim.getVisitorPermissions().hasPermission(ClaimPerm.PLACE))
                .getMessage().split("\\|");
        for (String line : placeSplit) placeLore.add(line);
        placeMeta.setLore(placeLore);
        place.setItemMeta(placeMeta);

        ItemStack interact = new ItemStack(Material.LEVER);
        ItemMeta interactMeta = interact.getItemMeta();
        interactMeta.setDisplayName(plugin.getLocale().getMessage("interface.settings.interacttitle").getMessage());
        List<String> interactLore = new ArrayList<>();
        String[] interactSplit = plugin.getLocale().getMessage("general.interface.current")
                .processPlaceholder("current", role == ClaimRole.MEMBER
                        ? claim.getMemberPermissions().hasPermission(ClaimPerm.INTERACT) : claim.getVisitorPermissions().hasPermission(ClaimPerm.INTERACT))
                .getMessage().split("\\|");
        for (String line : interactSplit) interactLore.add(line);
        interactMeta.setLore(interactLore);
        interact.setItemMeta(interactMeta);

        inventory.setItem(0, exit);
        inventory.setItem(8, exit);
        inventory.setItem(11, blockBreak);
        inventory.setItem(13, place);
        inventory.setItem(15, interact);
    }

    @Override
    protected void registerClickables() {
        registerClickable(0, (player, inventory, cursor, slot, type) ->
                new GUIMembers(player, claim, back));

        registerClickable(8, (player, inventory, cursor, slot, type) ->
                new GUIMembers(player, claim, back));

        registerClickable(11, (player, inventory, cursor, slot, type) -> {
            // Toggle block break perms
            if (role == ClaimRole.MEMBER)
                claim.getMemberPermissions().setCanBreak(!claim.getMemberPermissions().hasPermission(ClaimPerm.BREAK));
            else
                claim.getVisitorPermissions().setCanBreak(!claim.getVisitorPermissions().hasPermission(ClaimPerm.BREAK));
            constructGUI();
        });

        registerClickable(13, (player, inventory, cursor, slot, type) -> {
            // Toggle block place perms
            if (role == ClaimRole.MEMBER)
                claim.getMemberPermissions().setCanPlace(!claim.getMemberPermissions().hasPermission(ClaimPerm.PLACE));
            else
                claim.getVisitorPermissions().setCanPlace(!claim.getVisitorPermissions().hasPermission(ClaimPerm.PLACE));
            constructGUI();
        });

        registerClickable(15, (player, inventory, cursor, slot, type) -> {
            // Toggle block interact perms
            if (role == ClaimRole.MEMBER)
                claim.getMemberPermissions().setCanInteract(!claim.getMemberPermissions().hasPermission(ClaimPerm.INTERACT));
            else
                claim.getVisitorPermissions().setCanInteract(!claim.getVisitorPermissions().hasPermission(ClaimPerm.INTERACT));
            constructGUI();
        });
    }

    @Override
    protected void registerOnCloses() {

    }
}
