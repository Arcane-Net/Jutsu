package com.projectkorra.projectkorra.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.projectkorra.projectkorra.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.projectkorra.projectkorra.Ninja;
import com.projectkorra.projectkorra.Style;
import com.projectkorra.projectkorra.Style.ElementType;
import com.projectkorra.projectkorra.Style.SubElement;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;

/**
 * Executor for /jutsu who. Extends {@link PKCommand}.
 */
public class WhoCommand extends PKCommand {
	/**
	 * Map storage of all ProjectKorra staffs' UUIDs and titles
	 */
	final Map<String, String> staff = new HashMap<String, String>(), playerInfoWords = new HashMap<String, String>();

	private final String databaseOverload, noPlayersOnline, playerOffline, playerUnknown;

	public WhoCommand() {
		super("who", "/jutsu who [Page/Player]", ConfigManager.languageConfig.get().getString("Commands.Who.Description"), new String[] { "who", "w" });

		this.databaseOverload = ConfigManager.languageConfig.get().getString("Commands.Who.DatabaseOverload");
		this.noPlayersOnline = ConfigManager.languageConfig.get().getString("Commands.Who.NoPlayersOnline");
		this.playerOffline = ConfigManager.languageConfig.get().getString("Commands.Who.PlayerOffline");
		this.playerUnknown = ConfigManager.languageConfig.get().getString("Commands.Who.PlayerUnknown");

		new BukkitRunnable() {
			@Override
			public void run() {
				final Map<String, String> updatedstaff = new HashMap<String, String>();
				try {

					// Create a URL for the desired page.
					final URLConnection url = new URL("https://projectkorra.com/staff.txt").openConnection();
					url.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");

					// Read all the text returned by the server.
					final BufferedReader in = new BufferedReader(new InputStreamReader(url.getInputStream(), Charset.forName("UTF-8")));
					String unparsed;
					while ((unparsed = in.readLine()) != null) {
						final String[] staffEntry = unparsed.split("/");
						if (staffEntry.length >= 2) {
							updatedstaff.put(staffEntry[0], ChatColor.translateAlternateColorCodes('&', staffEntry[1]));
						}
					}
					in.close();
					WhoCommand.this.staff.clear();
					WhoCommand.this.staff.putAll(updatedstaff);
				} catch (final SocketException e) {
					ProjectKorra.log.info("Could not update staff list.");
				} catch (final MalformedURLException e) {
					e.printStackTrace();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}.runTaskTimerAsynchronously(ProjectKorra.plugin, 0, 20 * 60 * 60);
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.hasPermission(sender) || !this.correctLength(sender, args.size(), 0, 1)) {
			return;
		} else if (args.size() == 1 && args.get(0).length() > 2) {
			this.whoPlayer(sender, args.get(0));
		} else if (args.size() == 0 || args.size() == 1) {
			int page = 1;
			if (args.size() == 1 && this.isNumeric(args.get(0))) {
				page = Integer.valueOf(args.get(0));
			}
			final List<String> players = new ArrayList<String>();
			for (final Player player : Bukkit.getOnlinePlayers()) {
				if (sender instanceof Player && !((Player) sender).canSee(player)) {
					continue;
				}
				
				final String playerName = player.getName();
				String result = "";
				Ninja bp = Ninja.getBendingPlayer(playerName);

				for (final Style style : bp.getElements()) {
					if (result == "") {
						result = ChatColor.WHITE + playerName + " - " + (((!bp.isElementToggled(style) || !bp.isToggled()) ? style.getColor() + "" + ChatColor.STRIKETHROUGH : style.getColor()) + style.getName().substring(0, 1));
					} else {
						result = result + ChatColor.WHITE + " | " + (((!bp.isElementToggled(style) || !bp.isToggled()) ? style.getColor() + "" + ChatColor.STRIKETHROUGH : style.getColor()) + style.getName().substring(0, 1));
					}
				}
				if (this.staff.containsKey(player.getUniqueId().toString())) {
					if (result == "") {
						result = ChatColor.WHITE + playerName + " | " + this.staff.get(player.getUniqueId().toString());
					} else {
						result = result + ChatColor.WHITE + " | " + this.staff.get(player.getUniqueId().toString());
					}
				}
				if (result == "") {
					result = ChatColor.WHITE + playerName;
				}
				players.add(result);
			}
			if (players.isEmpty()) {
				ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.noPlayersOnline);
			} else {
				boolean firstMessage = true;

				for (final String s : this.getPage(players, ChatColor.GOLD + "Players:", page, true)) {
					if (firstMessage) {
						ChatUtil.sendBrandingMessage(sender, s);
						firstMessage = false;
					} else {
						sender.sendMessage(s);
					}
				}
			}
		}
	}

	/**
	 * Sends information on the given player to the CommandSender.
	 *
	 * @param sender The CommandSender to display the information to
	 * @param playerName The Player to look up
	 */
	private void whoPlayer(final CommandSender sender, final String playerName) {
		final OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
		if (!player.isOnline() && !player.hasPlayedBefore()) {
			ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.playerUnknown.replace("{target}", playerName));
			return;
		}

		//If they are actually offline OR they are vanished
		boolean offline = !player.isOnline() || (sender instanceof Player && player instanceof Player && !((Player) sender).canSee((Player) player));
		if (offline) {
			ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.playerOffline.replace("{target}", playerName));
		}

		Ninja.getOrLoadOfflineAsync(player).thenAccept(nPlayer -> {
			if (!(nPlayer instanceof Ninja)) { //Uncache after 30s
				nPlayer.uncacheAfter(30_000);
			}

			sender.sendMessage(player.getName() + (offline ? ChatColor.RESET + " (Offline)" : ""));
			if (nPlayer.hasElement(Style.AIR)) {
				if (nPlayer.isElementToggled(Style.AIR)) {
					sender.sendMessage(Style.AIR.getColor() + "- Airbender");
				} else {
					sender.sendMessage(Style.AIR.getColor() + "" + ChatColor.STRIKETHROUGH + "- Airbender");
				}

				if (nPlayer.canUseFlight()) {
					sender.sendMessage(Style.FLIGHT.getColor() + "    Can Fly");
				}
				if (nPlayer.canUseSpiritualProjection()) {
					sender.sendMessage(Style.SPIRITUAL.getColor() + "    Can use Spiritual Projection");
				}
				for (final SubElement se : Style.getAddonSubElements(Style.AIR)) {
					if (nPlayer.canUseSubElement(se)) {
						sender.sendMessage(se.getColor() + "    Can " + (!se.getType().equals(ElementType.NO_SUFFIX) ? "" : "use ") + se.getName() + se.getType().getBend());
					}
				}
			}

			if (nPlayer.hasElement(Style.WATER)) {
				if (nPlayer.isElementToggled(Style.WATER)) {
					sender.sendMessage(Style.WATER.getColor() + "- Waterbender");
				} else {
					sender.sendMessage(Style.WATER.getColor() + "" + ChatColor.STRIKETHROUGH + "- Waterbender");
				}

				if (nPlayer.canPlantbend()) {
					sender.sendMessage(Style.PLANT.getColor() + "    Can Plantbend");
				}
				if (nPlayer.canBloodbend()) {
					if (nPlayer.canBloodbendAtAnytime()) {
						sender.sendMessage(Style.BLOOD.getColor() + "    Can Bloodbend anytime, on any day");
					} else {
						sender.sendMessage(Style.BLOOD.getColor() + "    Can Bloodbend");
					}
				}
				if (nPlayer.canIcebend()) {
					sender.sendMessage(Style.ICE.getColor() + "    Can Icebend");
				}
				if (nPlayer.canWaterHeal()) {
					sender.sendMessage(Style.HEALING.getColor() + "    Can Heal");
				}
				for (final SubElement se : Style.getAddonSubElements(Style.WATER)) {
					if (nPlayer.canUseSubElement(se)) {
						sender.sendMessage(se.getColor() + "    Can " + (!se.getType().equals(ElementType.NO_SUFFIX) ? "" : "use ") + se.getName() + se.getType().getBend());
					}
				}
			}

			if (nPlayer.hasElement(Style.EARTH)) {
				if (nPlayer.isElementToggled(Style.EARTH)) {
					sender.sendMessage(Style.EARTH.getColor() + "- Earthbender");
				} else {
					sender.sendMessage(Style.EARTH.getColor() + "" + ChatColor.STRIKETHROUGH + "- Earthbender");
				}

				if (nPlayer.canMetalbend()) {
					sender.sendMessage(Style.METAL.getColor() + "    Can Metalbend");
				}
				if (nPlayer.canLavabend()) {
					sender.sendMessage(Style.LAVA.getColor() + "    Can Lavabend");
				}
				if (nPlayer.canSandbend()) {
					sender.sendMessage(Style.SAND.getColor() + "    Can Sandbend");
				}
				for (final SubElement se : Style.getAddonSubElements(Style.EARTH)) {
					if (nPlayer.canUseSubElement(se)) {
						sender.sendMessage(se.getColor() + "    Can " + (!se.getType().equals(ElementType.NO_SUFFIX) ? "" : "use ") + se.getName() + se.getType().getBend());
					}
				}
			}

			if (nPlayer.hasElement(Style.FIRE)) {
				if (nPlayer.isElementToggled(Style.FIRE)) {
					sender.sendMessage(Style.FIRE.getColor() + "- Firebender");
				} else {
					sender.sendMessage(Style.FIRE.getColor() + "" + ChatColor.STRIKETHROUGH + "- Firebender");
				}

				if (nPlayer.canCombustionbend()) {
					sender.sendMessage(Style.COMBUSTION.getColor() + "    Can Combustionbend");
				}
				if (nPlayer.canLightningbend()) {
					sender.sendMessage(Style.LIGHTNING.getColor() + "    Can Lightningbend");
				}
				if (nPlayer.hasSubElement(Style.BLUE_FIRE)) {
					sender.sendMessage(Style.BLUE_FIRE.getColor() + "    Can use Blue Fire");
				}
				for (final SubElement se : Style.getAddonSubElements(Style.FIRE)) {
					if (nPlayer.canUseSubElement(se)) {
						sender.sendMessage(se.getColor() + "    Can " + (!se.getType().equals(ElementType.NO_SUFFIX) ? "" : "use ") + se.getName() + se.getType().getBend());
					}
				}
			}

			if (nPlayer.hasElement(Style.CHI)) {
				if (nPlayer.isElementToggled(Style.CHI)) {
					sender.sendMessage(Style.CHI.getColor() + "- Chiblocker");
				} else {
					sender.sendMessage(Style.CHI.getColor() + "" + ChatColor.STRIKETHROUGH + "- Chiblocker");
				}

				for (final SubElement se : Style.getAddonSubElements(Style.CHI)) {
					if (nPlayer.canUseSubElement(se)) {
						sender.sendMessage(se.getColor() + "    Can " + (!se.getType().equals(ElementType.NO_SUFFIX) ? "" : "use ") + se.getName() + se.getType().getBend());
					}
				}
			}

			for (final Style style : Style.getAddonElements()) {
				if (nPlayer.hasElement(style)) {
					sender.sendMessage(style.getColor() + "" + (nPlayer.isElementToggled(style) ? "" : ChatColor.STRIKETHROUGH) + "- " + style.getName() + (style.getType() != null ? style.getType().getBender() : ""));

					for (final SubElement subelement : Style.getSubElements(style)) {
						if (nPlayer.canUseSubElement(subelement)) {
							sender.sendMessage(subelement.getColor() + "    Can " + (!subelement.getType().equals(ElementType.NO_SUFFIX) ? "" : "use ") + subelement.getName() + subelement.getType().getBend());
						}
					}
				}
			}

			final UUID uuid = player.getUniqueId();

			sender.sendMessage("Abilities: ");
			for (int i = 1; i <= 9; i++) {
				final String ability = nPlayer.getAbilities().get(i);
				final CoreAbility coreAbil = CoreAbility.getAbility(ability);
				if (coreAbil == null) continue;

				sender.sendMessage(i + " - " + coreAbil.getElement().getColor() + ability);

			}

			if (this.staff.containsKey(uuid.toString())) {
				sender.sendMessage(this.staff.get(uuid.toString()));
			}

			if (player.getPlayer() != null && player.getPlayer().hasPermission("jutsu.donor")) {
				sender.sendMessage(Style.AVATAR.getColor() + "Server Donor");
			}
		});
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (args.size() >= 1 || !sender.hasPermission("jutsu.command.who")) {
			return new ArrayList<String>();
		}

		return getOnlinePlayerNames(sender);
	}
}
