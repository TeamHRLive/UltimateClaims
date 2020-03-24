package com.songoda.ultimateclaims;

import com.songoda.core.SongodaCore;
import com.songoda.core.SongodaPlugin;
import com.songoda.core.commands.CommandManager;
import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.configuration.Config;
import com.songoda.core.database.DataMigrationManager;
import com.songoda.core.database.DatabaseConnector;
import com.songoda.core.database.MySQLConnector;
import com.songoda.core.database.SQLiteConnector;
import com.songoda.core.gui.GuiManager;
import com.songoda.core.hooks.EconomyManager;
import com.songoda.core.hooks.HologramManager;
import com.songoda.core.hooks.WorldGuardHook;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.claim.ClaimManager;
import com.songoda.ultimateclaims.commands.*;
import com.songoda.ultimateclaims.commands.admin.CommandRemoveClaim;
import com.songoda.ultimateclaims.database.DataManager;
import com.songoda.ultimateclaims.database.migrations._1_InitialMigration;
import com.songoda.ultimateclaims.database.migrations._2_NewPermissions;
import com.songoda.ultimateclaims.database.migrations._3_MemberNames;
import com.songoda.ultimateclaims.listeners.*;
import com.songoda.ultimateclaims.dynmap.DynmapManager;
import com.songoda.ultimateclaims.placeholder.PlaceholderManager;
import com.songoda.ultimateclaims.settings.PluginSettings;
import com.songoda.ultimateclaims.settings.Settings;
import com.songoda.ultimateclaims.tasks.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import java.util.Collections;
import java.util.List;

public class UltimateClaims extends SongodaPlugin {

    private static UltimateClaims INSTANCE;

    private PluginSettings pluginSettings;

    private DatabaseConnector databaseConnector;

    private final GuiManager guiManager = new GuiManager(this);
    private CommandManager commandManager;
    private ClaimManager claimManager;
    private DynmapManager dynmapManager;

    private DataMigrationManager dataMigrationManager;
    private DataManager dataManager;

    private InviteTask inviteTask;

    public static UltimateClaims getInstance() {
        return INSTANCE;
    }

    @Override
    public void onPluginLoad() {
        INSTANCE = this;
        WorldGuardHook.addHook("allow-claims", true);
    }

    @Override
    public void onPluginEnable() {
        // Register in Songoda Core
        SongodaCore.registerPlugin(this, 65, CompatibleMaterial.CHEST);

        // Load Economy & Hologram hooks
        EconomyManager.load();
        HologramManager.load(this);

        // Setup Config
        Settings.setupConfig();
        this.setLocale(Settings.LANGUGE_MODE.getString(), false);

        // Set Economy & Hologram preference
        EconomyManager.getManager().setPreferredHook(Settings.ECONOMY.getString());
        HologramManager.getManager().setPreferredHook(Settings.HOLOGRAM.getString());

        PluginManager pluginManager = Bukkit.getPluginManager();

        // Listeners
        guiManager.init();
        pluginManager.registerEvents(new EntityListeners(this), this);
        pluginManager.registerEvents(new BlockListeners(this), this);
        pluginManager.registerEvents(new InteractListeners(this), this);
        pluginManager.registerEvents(new InventoryListeners(this), this);
        pluginManager.registerEvents(new LoginListeners(this), this);

        // Load Commands
        this.commandManager = new CommandManager(this);
        this.commandManager.addMainCommand("c")
                .addSubCommands(
                        new CommandSettings(this),
                        new CommandReload(this),
                        new CommandClaim(this),
                        new CommandUnClaim(this),
                        new CommandShow(this),
                        new CommandInvite(this),
                        new CommandAccept(this),
                        new CommandAddMember(this),
                        new CommandKick(this),
                        new CommandDissolve(this),
                        new CommandLeave(this),
                        new CommandLock(this),
                        new CommandHome(this),
                        new CommandSetHome(this),
                        new CommandBan(this),
                        new CommandUnBan(this),
                        new CommandRecipe(this),
                        new CommandSetSpawn(this),
                        new CommandName(this),

                        new CommandRemoveClaim(this)
                );

        // Tasks
        this.inviteTask = InviteTask.startTask(this);
        AnimateTask.startTask(this);
        if (!Settings.DISABLE_FUEL.getBoolean())
            PowerCellTask.startTask(this);
        TrackerTask.startTask(this);
        VisualizeTask.startTask(this);

        // Register Placeholders
        if (pluginManager.isPluginEnabled("PlaceholderAPI"))
            new PlaceholderManager(this).register();

        if (pluginManager.isPluginEnabled("dynmap"))
            this.dynmapManager = new DynmapManager(this);

        // Start our databases
        this.claimManager = new ClaimManager();

        // Database stuff, go!
        try {
            if (Settings.MYSQL_ENABLED.getBoolean()) {
                String hostname = Settings.MYSQL_HOSTNAME.getString();
                int port = Settings.MYSQL_PORT.getInt();
                String database = Settings.MYSQL_DATABASE.getString();
                String username = Settings.MYSQL_USERNAME.getString();
                String password = Settings.MYSQL_PASSWORD.getString();
                boolean useSSL = Settings.MYSQL_USE_SSL.getBoolean();

                this.databaseConnector = new MySQLConnector(this, hostname, port, database, username, password, useSSL);
                this.getLogger().info("Data handler connected using MySQL.");
            } else {
                this.databaseConnector = new SQLiteConnector(this);
                this.getLogger().info("Data handler connected using SQLite.");
            }
        } catch (Exception ex) {
            this.getLogger().severe("Fatal error trying to connect to database. Please make sure all your connection settings are correct and try again. Plugin has been disabled.");
            this.emergencyStop();
        }

        this.dataManager = new DataManager(this.databaseConnector, this);
        this.dataMigrationManager = new DataMigrationManager(this.databaseConnector, this.dataManager,
                new _1_InitialMigration(),
                new _2_NewPermissions(),
                new _3_MemberNames());
        this.dataMigrationManager.runMigrations();

        Bukkit.getScheduler().runTaskLater(this, () -> {
            this.dataManager.getPluginSettings((pluginSettings) -> this.pluginSettings = pluginSettings);
            final boolean useHolo = Settings.POWERCELL_HOLOGRAMS.getBoolean() && HologramManager.getManager().isEnabled();
            this.dataManager.getClaims((claims) -> {
                this.claimManager.addClaims(claims);
                if (useHolo)
                    this.claimManager.getRegisteredClaims().stream().filter(Claim::hasPowerCell).forEach(x -> x.getPowerCell().updateHologram());
            });
        }, 20L);
    }

    @Override
    public void onPluginDisable() {
        // save all claims data
        this.guiManager.closeAll();
        this.dataManager.bulkUpdateClaims(this.claimManager.getRegisteredClaims());
        this.databaseConnector.closeConnection();

        // cleanup holograms
        HologramManager.removeAllHolograms();

        // cleanup boss bars
        if (Settings.CLAIMS_BOSSBAR.getBoolean()) {
            this.claimManager.getRegisteredClaims().forEach(x -> {
                x.getVisitorBossBar().removeAll();
                x.getMemberBossBar().removeAll();
            });
        }
    }

    @Override
    public List<Config> getExtraConfig() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public void onConfigReload() {
        this.setLocale(Settings.LANGUGE_MODE.getString(), true);
        this.dynmapManager.reload();
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }

    public CommandManager getCommandManager() {
        return this.commandManager;
    }

    public ClaimManager getClaimManager() {
        return claimManager;
    }

    public DynmapManager getDynmapManager() {
        return dynmapManager;
    }

    public DataMigrationManager getDataMigrationManager() {
        return this.dataMigrationManager;
    }

    public DataManager getDataManager() {
        return this.dataManager;
    }

    public DatabaseConnector getDatabaseConnector() {
        return this.databaseConnector;
    }

    public InviteTask getInviteTask() {
        return inviteTask;
    }

    public PluginSettings getPluginSettings() {
        return pluginSettings;
    }
}
