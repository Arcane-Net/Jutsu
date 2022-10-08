package com.projectkorra.projectkorra.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.projectkorra.projectkorra.util.ChatUtil;
import org.bukkit.Bukkit;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Ninja;
import com.projectkorra.projectkorra.Style;
import com.projectkorra.projectkorra.Style.SubElement;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent.Result;
import com.projectkorra.projectkorra.event.PlayerChangeSubElementEvent;

/**
 * Executor for /jutsu add. Extends {@link PKCommand}.
 */
public class AddCommand extends PKCommand {

	private final String playerNotFound;
	private final String invalidElement;
	private final String addedOtherCFW;
	private final String addedOtherAE;
	private final String addedCFW;
	private final String addedAE;
	private final String alreadyHasElementOther;
	private final String alreadyHasElement;
	private final String alreadyHasSubElementOther;
	private final String alreadyHasSubElement;
	private final String addedOtherAll;
	private final String addedAll;
	private final String alreadyHasAllElementsOther;
	private final String alreadyHasAllElements;

	public AddCommand() {
		super("add", "/jutsu add <Style/SubElement> [Player]", ConfigManager.languageConfig.get().getString("Commands.Add.Description"), new String[] { "add", "a" });

		this.playerNotFound = ConfigManager.languageConfig.get().getString("Commands.Add.PlayerNotFound");
		this.invalidElement = ConfigManager.languageConfig.get().getString("Commands.Add.InvalidElement");
		this.addedOtherCFW = ConfigManager.languageConfig.get().getString("Commands.Add.Other.SuccessfullyAddedCFW");
		this.addedOtherAE = ConfigManager.languageConfig.get().getString("Commands.Add.Other.SuccessfullyAddedAE");
		this.addedCFW = ConfigManager.languageConfig.get().getString("Commands.Add.SuccessfullyAddedCFW");
		this.addedAE = ConfigManager.languageConfig.get().getString("Commands.Add.SuccessfullyAddedAE");
		this.addedOtherAll = ConfigManager.languageConfig.get().getString("Commands.Add.Other.SuccessfullyAddedAll");
		this.addedAll = ConfigManager.languageConfig.get().getString("Commands.Add.SuccessfullyAddedAll");
		this.alreadyHasElementOther = ConfigManager.languageConfig.get().getString("Commands.Add.Other.AlreadyHasElement");
		this.alreadyHasElement = ConfigManager.languageConfig.get().getString("Commands.Add.AlreadyHasElement");
		this.alreadyHasSubElementOther = ConfigManager.languageConfig.get().getString("Commands.Add.Other.AlreadyHasSubElement");
		this.alreadyHasSubElement = ConfigManager.languageConfig.get().getString("Commands.Add.AlreadyHasSubElement");
		this.alreadyHasAllElementsOther = ConfigManager.languageConfig.get().getString("Commands.Add.Other.AlreadyHasAllElements");
		this.alreadyHasAllElements = ConfigManager.languageConfig.get().getString("Commands.Add.AlreadyHasAllElements");
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.correctLength(sender, args.size(), 1, 2)) {
			return;
		} else if (args.size() == 1) { // jutsu add style.
			if (!this.hasPermission(sender) || !this.isPlayer(sender)) {
				return;
			}
			this.add(sender, (Player) sender, args.get(0).toLowerCase());
		} else if (args.size() == 2) { // jutsu add style combo.
			if (!this.hasPermission(sender, "others")) {
				return;
			}
			final OfflinePlayer player = Bukkit.getOfflinePlayer(args.get(1));
			if (!player.isOnline() && !player.hasPlayedBefore()) {
				ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.playerNotFound);
				return;
			}
			this.add(sender, player, args.get(0).toLowerCase());
		}
	}

	/**
	 * Adds the ability to bend an style to a player.
	 *
	 * @param sender The CommandSender who issued the add command
	 * @param target The player to add the style to
	 * @param style The style to add
	 */
	private void add(final CommandSender sender, final OfflinePlayer target, final String style) {

		// if they aren't a Ninja, create them.
		Ninja.getOrLoadOfflineAsync(target).thenAccept(nPlayer -> {
			boolean online = nPlayer instanceof Ninja;

			if (nPlayer.isPermaRemoved()) { // ignore permabanned users.
				ChatUtil.sendBrandingMessage(sender, ChatColor.RED + ConfigManager.languageConfig.get().getString("Commands.Preset.Other.BendingPermanentlyRemoved"));
				return;
			}

			if (style.equalsIgnoreCase("all")) {
				final StringBuilder styles = new StringBuilder("");
				boolean elementFound = false;
				for (final Style e : Style.getAllElements()) {
					if (!nPlayer.hasElement(e) && e != Style.AVATAR) {
						elementFound = true;
						nPlayer.addElement(e);

						if (styles.length() > 1) {
							styles.append(ChatColor.YELLOW + ", ");
						}
						styles.append(e.toString());

						nPlayer.getSubElements().clear();
						if (online) {
							for (final SubElement sub : Style.getAllSubElements()) {
								if (nPlayer.hasElement(sub.getParentElement()) && ((Ninja)nPlayer).hasSubElementPermission(sub)) {
									nPlayer.addSubElement(sub);
								}
							}
							nPlayer.saveSubElements();
						}

						nPlayer.saveElements();

						if (online) Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(sender, (Player) target, e, Result.ADD));
					}
				}
				if (elementFound) {
					if (!(sender instanceof Player) || !((Player) sender).equals(target)) {
						ChatUtil.sendBrandingMessage(sender, ChatColor.YELLOW + this.addedOtherAll.replace("{target}", ChatColor.DARK_AQUA + target.getName() + ChatColor.YELLOW) + styles);
						if (online) ChatUtil.sendBrandingMessage((Player)target, ChatColor.YELLOW + this.addedAll + styles);
					} else {
						if (online) ChatUtil.sendBrandingMessage((Player)target, ChatColor.YELLOW + this.addedAll + styles);
					}
				} else {
					if (!(sender instanceof Player) || !((Player) sender).equals(target)) {
						ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.alreadyHasAllElementsOther.replace("{target}", ChatColor.DARK_AQUA + target.getName() + ChatColor.RED));
					} else {
						ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.alreadyHasAllElements);
					}
				}
				return;
			} else {

				// get the [sub]style.
				Style e = Style.fromString(style);
				if (e == null) {
					e = Style.fromString(style);
				}

				if (e == Style.AVATAR) {
					this.add(sender, target, Style.AIR.getName());
					this.add(sender, target, Style.EARTH.getName());
					this.add(sender, target, Style.FIRE.getName());
					this.add(sender, target, Style.WATER.getName());
					return;
				}

				// if it's an style:
				if (Arrays.asList(Style.getAllElements()).contains(e)) {
					if (nPlayer.hasElement(e)) { // if already had, determine who to send the error message to.
						if (!(sender instanceof Player) || !((Player) sender).equals(target)) {
							ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.alreadyHasElementOther.replace("{target}", ChatColor.DARK_AQUA + target.getName() + ChatColor.RED));
						} else {
							ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.alreadyHasElement);
						}
						return;
					}

					// add all allowed subelements.
					nPlayer.addElement(e);
					nPlayer.getSubElements().clear();
					if (online) {
						for (final SubElement sub : Style.getAllSubElements()) {
							if (nPlayer.hasElement(sub.getParentElement()) && ((Ninja)nPlayer).hasSubElementPermission(sub)) {
								nPlayer.addSubElement(sub);
							}
						}
					}


					// send the message.
					final ChatColor color = e.getColor();
					if (!(sender instanceof Player) || !((Player) sender).equals(target)) {
						if (e != Style.AIR && e != Style.EARTH && e != Style.BLUE_FIRE) {
							ChatUtil.sendBrandingMessage(sender, color + this.addedOtherCFW.replace("{target}", ChatColor.DARK_AQUA + target.getName() + color).replace("{style}", e.toString() + e.getType().getBender()));
							if (online) ChatUtil.sendBrandingMessage((Player)target, color + this.addedCFW.replace("{style}", e.toString() + e.getType().getBender()));
						} else {
							ChatUtil.sendBrandingMessage(sender, color + this.addedOtherAE.replace("{target}", ChatColor.DARK_AQUA + target.getName() + color).replace("{style}", e.toString() + e.getType().getBender()));
							if (online) ChatUtil.sendBrandingMessage((Player)target, color + this.addedAE.replace("{style}", e.toString() + e.getType().getBender()));
						}
					} else {
						if (e != Style.AIR && e != Style.EARTH) {
							if (online) ChatUtil.sendBrandingMessage((Player)target, color + this.addedCFW.replace("{style}", e.toString() + e.getType().getBender()));
						} else {
							if (online) ChatUtil.sendBrandingMessage((Player)target, color + this.addedAE.replace("{style}", e.toString() + e.getType().getBender()));
						}

					}
					nPlayer.saveElements();
					nPlayer.saveSubElements();
					if (online) Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(sender, (Player) target, e, Result.ADD));
					return;

					// if it's a sub style:
				} else if (Arrays.asList(Style.getAllSubElements()).contains(e)) {
					final SubElement sub = (SubElement) e;
					if (nPlayer.hasSubElement(sub)) { // if already had, determine  who to send the error message to.
						if (!(sender instanceof Player) || !((Player) sender).equals(target)) {
							ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.alreadyHasSubElementOther.replace("{target}", ChatColor.DARK_AQUA + target.getName() + ChatColor.RED));
						} else {
							ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.alreadyHasSubElement);
						}
						return;
					}
					nPlayer.addSubElement(sub);
					final ChatColor color = e.getColor();

					if (!(sender instanceof Player) || !((Player) sender).equals(target)) {
						if (e != Style.AIR && e != Style.EARTH) {
							ChatUtil.sendBrandingMessage(sender, color + this.addedOtherCFW.replace("{target}", ChatColor.DARK_AQUA + target.getName() + color).replace("{style}", sub.toString() + sub.getType().getBender()));
						} else {
							ChatUtil.sendBrandingMessage(sender, color + this.addedOtherAE.replace("{target}", ChatColor.DARK_AQUA + target.getName() + color).replace("{style}", sub.toString() + sub.getType().getBender()));
						}

					} else {
						if (e != Style.AIR && e != Style.EARTH) {
							if (online) ChatUtil.sendBrandingMessage((Player)target, color + this.addedCFW.replace("{style}", sub.toString() + sub.getType().getBender()));
						} else {
							if (online) ChatUtil.sendBrandingMessage((Player)target, color + this.addedAE.replace("{style}", sub.toString() + sub.getType().getBender()));
						}
					}
					nPlayer.saveSubElements();
					if (online) Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeSubElementEvent(sender, (Player) target, sub, com.projectkorra.projectkorra.event.PlayerChangeSubElementEvent.Result.ADD));
					return;

				} else { // bad style.
					sender.sendMessage(ChatColor.RED + this.invalidElement);
				}
			}
		});

	}

	public static boolean isVowel(final char c) {
		return "AEIOUaeiou".indexOf(c) != -1;
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (args.size() >= 2 || !sender.hasPermission("jutsu.command.add")) {
			return new ArrayList<String>();
		}
		final List<String> l = new ArrayList<String>();
		if (args.size() == 0) {

			l.add("Air");
			l.add("Earth");
			l.add("Fire");
			l.add("Water");
			l.add("Chi");
			for (final Style e : Style.getAddonElements()) {
				l.add(e.getName());
			}

			l.add("Blood");
			l.add("Combustion");
			l.add("Flight");
			l.add("Healing");
			l.add("Ice");
			l.add("Lava");
			l.add("Lightning");
			l.add("Metal");
			l.add("Plant");
			l.add("Sand");
			l.add("Spiritual");
			l.add("BlueFire");
			for (final SubElement e : Style.getAddonSubElements()) {
				l.add(e.getName());
			}
		} else {
			for (final Player p : Bukkit.getOnlinePlayers()) {
				l.add(p.getName());
			}
		}
		return l;
	}
}
