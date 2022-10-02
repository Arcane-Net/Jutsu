package com.jutsu.jutsu;

import java.util.HashMap;
import java.util.logging.Logger;

import com.bekvon.bukkit.residence.protection.FlagPermissions;

import co.aikar.timings.lib.MCTiming;
import co.aikar.timings.lib.TimingManager;

import com.jutsu.jutsu.region.RegionProtection;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.jutsu.jutsu.ability.CoreAbility;
import com.jutsu.jutsu.ability.util.CollisionInitializer;
import com.jutsu.jutsu.ability.util.CollisionManager;
import com.jutsu.jutsu.ability.util.ComboManager;
import com.jutsu.jutsu.ability.util.MultiAbilityManager;
import com.jutsu.jutsu.airbending.util.AirbendingManager;
import com.jutsu.jutsu.board.BendingBoardManager;
import com.jutsu.jutsu.chiblocking.util.ChiblockingManager;
import com.jutsu.jutsu.command.Commands;
import com.jutsu.jutsu.configuration.ConfigManager;
import com.jutsu.jutsu.earthbending.util.EarthbendingManager;
import com.jutsu.jutsu.firebending.util.FirebendingManager;
import com.jutsu.jutsu.hooks.PlaceholderAPIHook;
import com.jutsu.jutsu.hooks.WorldGuardFlag;
import com.jutsu.jutsu.object.Preset;
import com.jutsu.jutsu.storage.DBConnection;
import com.jutsu.jutsu.util.Metrics;
import com.jutsu.jutsu.util.RevertChecker;
import com.jutsu.jutsu.util.StatisticsManager;
import com.jutsu.jutsu.util.TempBlock;
import com.jutsu.jutsu.util.Updater;
import com.jutsu.jutsu.waterbending.util.WaterbendingManager;

public class ProjectKorra extends JavaPlugin {

	public static Jutsu plugin;
	public static Logger log;
	public static CollisionManager collisionManager;
	public static CollisionInitializer collisionInitializer;
	public static long time_step = 1;
	public Updater updater;
	private BukkitTask revertChecker;
	private static TimingManager timingManager;

	@Override
	public void onEnable() {
		plugin = this;
		Jutsu.log = this.getLogger();

		timingManager = TimingManager.of(this);

		new ConfigManager();
		new GeneralMethods(this);
		final boolean checkUpdateOnStartup = ConfigManager.getConfig().getBoolean("Properties.UpdateChecker");
		this.updater = new Updater(this, "https://projectkorra.com/forum/resources/projectkorra-core.1/", checkUpdateOnStartup);
		new Commands(this);
		new MultiAbilityManager();
		new ComboManager();
		new RegionProtection();
		collisionManager = new CollisionManager();
		collisionInitializer = new CollisionInitializer(collisionManager);
		CoreAbility.registerAbilities();
		collisionInitializer.initializeDefaultCollisions();
		collisionManager.startCollisionDetection();

		Preset.loadExternalPresets();

		DBConnection.init();
		if (!DBConnection.isOpen()) {
			return;
		}

		Manager.startup();
		BendingBoardManager.setup();

		this.getServer().getPluginManager().registerEvents(new PKListener(this), this);
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new BendingManager(), 0, 1);
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new AirbendingManager(this), 0, 1);
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new WaterbendingManager(this), 0, 1);
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new EarthbendingManager(this), 0, 1);
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new FirebendingManager(this), 0, 1);
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new LightningStyle(this), 0, 1);
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new ChiblockingManager(this), 0, 1);
		this.revertChecker = this.getServer().getScheduler().runTaskTimerAsynchronously(this, new RevertChecker(this), 0, 200);

		TempBlock.startReversion();

		for (final Player player : Bukkit.getOnlinePlayers()) {
			PKListener.getJumpStatistics().put(player, player.getStatistic(Statistic.JUMP));

			OfflineBendingPlayer.loadAsync(player.getUniqueId(), true);
			Manager.getManager(StatisticsManager.class).load(player.getUniqueId());
		}

		final Metrics metrics = new Metrics(this);
		metrics.addCustomChart(new Metrics.AdvancedPie("Elements") {

			@Override
			public HashMap<String, Integer> getValues(final HashMap<String, Integer> valueMap) {
				for (final Element element : Element.getMainElements()) {
					valueMap.put(element.getName(), this.getPlayersWithElement(element));
				}

				return valueMap;
			}

			private int getPlayersWithElement(final Element element) {
				int counter = 0;
				for (final Player player : Bukkit.getOnlinePlayers()) {
					final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
					if (bPlayer != null && bPlayer.hasElement(element)) {
						counter++;
					}
				}

				return counter;
			}
		});

		final double cacheTime = ConfigManager.getConfig().getDouble("Properties.RegionProtection.CacheBlockTime");

		GeneralMethods.deserializeFile();
		RegionProtection.startCleanCacheTask(cacheTime);

		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			new PlaceholderAPIHook(this).register();
		}
	}

	@Override
	public void onDisable() {
		this.revertChecker.cancel();
		GeneralMethods.stopBending();
		for (final Player player : this.getServer().getOnlinePlayers()) {
			if (isStatisticsEnabled()) {
				Manager.getManager(StatisticsManager.class).save(player.getUniqueId(), false);
			}
			final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
			if (bPlayer != null && isDatabaseCooldownsEnabled()) {
				bPlayer.saveCooldowns(false);
			}
		}
		Manager.shutdown();
		if (DBConnection.isOpen()) {
			DBConnection.sql.close();
		}
	}

	@Override
	public void onLoad() {
		if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
			WorldGuardFlag.registerBendingWorldGuardFlag();
		}
	}

	public static CollisionManager getCollisionManager() {
		return collisionManager;
	}

	public static void setCollisionManager(final CollisionManager collisionManager) {
		ProjectKorra.collisionManager = collisionManager;
	}

	public static CollisionInitializer getCollisionInitializer() {
		return collisionInitializer;
	}

	public static void setCollisionInitializer(final CollisionInitializer collisionInitializer) {
		ProjectKorra.collisionInitializer = collisionInitializer;
	}

	public static boolean isStatisticsEnabled() {
		return ConfigManager.getConfig().getBoolean("Properties.Statistics");
	}

	public static boolean isDatabaseCooldownsEnabled() {
		return ConfigManager.getConfig().getBoolean("Properties.DatabaseCooldowns");
	}

	public static MCTiming timing(final String name) {
		return timingManager.of(name);
	}
}
