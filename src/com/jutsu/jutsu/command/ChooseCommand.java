package com.projectkorra.projectkorra.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.projectkorra.projectkorra.util.ChatUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
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
import com.projectkorra.projectkorra.util.TimeUtil;

/**
 * Executor for /jutsu choose. Extends {@link PKCommand}.
 */
public class ChooseCommand extends PKCommand {

	private final String invalidElement;
	private final String playerNotFound;
	private final String onCooldown;
	private final String chosenCFW;
	private final String chosenAE;
	private final String chosenOtherCFW;
	private final String chosenOtherAE;
	private final long cooldown;

	public ChooseCommand() {
		super("choose", "/jutsu choose <Style> [Player]", ConfigManager.languageConfig.get().getString("Commands.Choose.Description"), new String[] { "choose", "ch" });

		this.playerNotFound = ConfigManager.languageConfig.get().getString("Commands.Choose.PlayerNotFound");
		this.invalidElement = ConfigManager.languageConfig.get().getString("Commands.Choose.InvalidElement");
		this.onCooldown = ConfigManager.languageConfig.get().getString("Commands.Choose.OnCooldown");
		this.chosenCFW = ConfigManager.languageConfig.get().getString("Commands.Choose.SuccessfullyChosenCFW");
		this.chosenAE = ConfigManager.languageConfig.get().getString("Commands.Choose.SuccessfullyChosenAE");
		this.chosenOtherCFW = ConfigManager.languageConfig.get().getString("Commands.Choose.Other.SuccessfullyChosenCFW");
		this.chosenOtherAE = ConfigManager.languageConfig.get().getString("Commands.Choose.Other.SuccessfullyChosenAE");
		this.cooldown = ConfigManager.defaultConfig.get().getLong("Properties.ChooseCooldown");
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.correctLength(sender, args.size(), 1, 2)) {
			return;
		}
		if (args.size() == 1) {
			if (!this.hasPermission(sender) || !this.isPlayer(sender)) {
				return;
			}

			//Don't need to bother with offline players here because the sender is always online
			Ninja nPlayer = Ninja.getBendingPlayer(sender.getName());
			if (nPlayer.isPermaRemoved()) {
				ChatUtil.sendBrandingMessage(sender, ChatColor.RED + ConfigManager.languageConfig.get().getString("Commands.Preset.BendingPermanentlyRemoved"));
				return;
			}
			if (!nPlayer.getElements().isEmpty() && !sender.hasPermission("jutsu.command.rechoose")) {
				ChatUtil.sendBrandingMessage(sender, super.noPermissionMessage);
				return;
			}
			String style = args.get(0).toLowerCase();
			if (style.equalsIgnoreCase("a")) {
				style = "air";
			} else if (style.equalsIgnoreCase("e")) {
				style = "earth";
			} else if (style.equalsIgnoreCase("f")) {
				style = "fire";
			} else if (style.equalsIgnoreCase("w")) {
				style = "water";
			} else if (style.equalsIgnoreCase("c")) {
				style = "chi";
			}
			final Style targetElement = Style.getElement(style);
			if (Arrays.asList(Style.getAllElements()).contains(targetElement)) {
				if (!this.hasPermission(sender, style)) {
					return;
				}
				if (nPlayer.isOnCooldown("ChooseElement")) {
					if (sender.hasPermission("jutsu.command.choose.ignorecooldown") || sender.hasPermission("jutsu.admin.choose")) {
						nPlayer.removeCooldown("ChooseElement");
					} else {
						ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.onCooldown.replace("%cooldown%", TimeUtil.formatTime(nPlayer.getCooldown("ChooseElement") - System.currentTimeMillis())));
						return;
					}
				}

				this.add(sender, (Player) sender, targetElement);

				if (sender.hasPermission("jutsu.command.choose.ignorecooldown") || sender.hasPermission("jutsu.admin.choose")) {
					return;
				}

				nPlayer.addCooldown("ChooseElement", this.cooldown, true);
			} else {
				ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.invalidElement);
			}
		} else if (args.size() == 2) {
			if (!sender.hasPermission("jutsu.admin.choose")) {
				ChatUtil.sendBrandingMessage(sender, super.noPermissionMessage);
				return;
			}
			final OfflinePlayer target = Bukkit.getOfflinePlayer(args.get(1));
			if (!target.hasPlayedBefore() && !target.isOnline()) {
				ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.playerNotFound);
				return;
			}
			String style = args.get(0).toLowerCase();
			if (style.equalsIgnoreCase("a")) {
				style = "air";
			} else if (style.equalsIgnoreCase("e")) {
				style = "earth";
			} else if (style.equalsIgnoreCase("f")) {
				style = "fire";
			} else if (style.equalsIgnoreCase("w")) {
				style = "water";
			} else if (style.equalsIgnoreCase("c")) {
				style = "chi";
			}
			final Style targetElement = Style.getElement(style);
			if (Arrays.asList(Style.getAllElements()).contains(targetElement) && targetElement != Style.AVATAR) {
				this.add(sender, target, targetElement);

				if (target.isOnline()) {
					if (((Player)target).hasPermission("jutsu.command.choose.ignorecooldown") || ((Player)target).hasPermission("jutsu.admin.choose")) {
						return;
					}
				}

				Ninja.getOrLoadOfflineAsync(target).thenAccept(nPlayer -> {
					nPlayer.addCooldown("ChooseElement", this.cooldown, true);
				});
			} else {
				ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.invalidElement);
			}
		}
	}

	/**
	 * Adds the ability to bend the given style to the specified Player.
	 *
	 * @param sender The CommandSender who issued the command
	 * @param target The Player to add the style to
	 * @param style The style to add to the Player
	 */
	private void add(final CommandSender sender, final OfflinePlayer target, final Style style) {
		Ninja.getOrLoadOfflineAsync(target).thenAccept(nPlayer -> {
			boolean online = nPlayer instanceof Ninja;

			if (style instanceof SubElement) {
				final SubElement sub = (SubElement) style;
				nPlayer.addSubElement(sub);
				final ChatColor color = sub.getColor();
				if (!(sender instanceof Player) || !((Player) sender).equals(target)) {
					ChatUtil.sendBrandingMessage(sender, color + this.chosenOtherCFW.replace("{target}", ChatColor.DARK_AQUA + target.getName() + color).replace("{style}", sub.getName() + sub.getType().getBender()));
				} else {
					if (online) ChatUtil.sendBrandingMessage((Player) target, color + this.chosenCFW.replace("{style}", sub.getName() + sub.getType().getBender()));
				}
				nPlayer.saveSubElements();
				if (online) {
					((Ninja)nPlayer).removeUnusableAbilities();
					Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeSubElementEvent(sender, (Player) target, sub, PlayerChangeSubElementEvent.Result.CHOOSE));
				}
			} else {
				if (style == Style.AVATAR) {
					nPlayer.getElements().clear();
					for (Style e : new Style[] {Style.AIR, Style.EARTH, Style.FIRE, Style.WATER}) {
						nPlayer.addElement(e);

						if (online) {
							for (final SubElement sub : Style.getSubElements(style)) {
								if (((Ninja) nPlayer).hasSubElementPermission(sub)) {
									nPlayer.addSubElement(sub);
								}
							}
						}
					}
				} else {
					nPlayer.setElement(style);
					nPlayer.getSubElements().clear();

					if (online) {
						for (final SubElement sub : Style.getSubElements(style)) {
							if (((Ninja) nPlayer).hasSubElementPermission(sub)) {
								nPlayer.addSubElement(sub);
							}
						}
					}
				}

				final ChatColor color = style.getColor();
				if (!(sender instanceof Player) || !((Player) sender).equals(target)) {
					if (style != Style.AIR && style != Style.EARTH) {
						ChatUtil.sendBrandingMessage(sender, color + this.chosenOtherCFW.replace("{target}", ChatColor.DARK_AQUA + target.getName() + color).replace("{style}", style.getName() + style.getType().getBender()));
					} else {
						ChatUtil.sendBrandingMessage(sender, color + this.chosenOtherAE.replace("{target}", ChatColor.DARK_AQUA + target.getName() + color).replace("{style}", style.getName() + style.getType().getBender()));
					}
				} else {
					if (style != Style.AIR && style != Style.EARTH) {
						if (online) ChatUtil.sendBrandingMessage((Player) target, color + this.chosenCFW.replace("{style}", style.getName() + style.getType().getBender()));
					} else {
						if (online) ChatUtil.sendBrandingMessage((Player) target, color + this.chosenAE.replace("{style}", style.getName() + style.getType().getBender()));
					}
				}
				nPlayer.saveElements();
				nPlayer.saveSubElements();
				if (online) {
					((Ninja)nPlayer).removeUnusableAbilities();
					Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(sender, (Player) target, style, Result.CHOOSE));
				}

			}
		});
	}

	public static boolean isVowel(final char c) {
		return "AEIOUaeiou".indexOf(c) != -1;
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (args.size() >= 2 || !sender.hasPermission("jutsu.command.choose")) {
			return new ArrayList<>();
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
		} else {
			for (final Player p : Bukkit.getOnlinePlayers()) {
				l.add(p.getName());
			}
		}
		return l;
	}
}
