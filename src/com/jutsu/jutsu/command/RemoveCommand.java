package com.projectkorra.projectkorra.command;

import java.util.ArrayList;
import java.util.List;

import com.projectkorra.projectkorra.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
 * Executor for /jutsu remove. Extends {@link PKCommand}.
 */
public class RemoveCommand extends PKCommand {

	private final String succesfullyRemovedElementSelf, wrongElementSelf, invalidElement, playerNotFound, wrongElementTarget, succesfullyRemovedElementTarget, succesfullyRemovedElementTargetConfirm, succesfullyRemovedAllElementsTarget, succesfullyRemovedAllElementsTargetConfirm;

	public RemoveCommand() {
		super("remove", "/jutsu remove <Player> [Style]", ConfigManager.languageConfig.get().getString("Commands.Remove.Description"), new String[] { "remove", "rm" });

		this.succesfullyRemovedElementSelf = ConfigManager.languageConfig.get().getString("Commands.Remove.RemovedElement");
		this.succesfullyRemovedAllElementsTarget = ConfigManager.languageConfig.get().getString("Commands.Remove.Other.RemovedAllElements");
		this.succesfullyRemovedAllElementsTargetConfirm = ConfigManager.languageConfig.get().getString("Commands.Remove.Other.RemovedAllElementsConfirm");
		this.succesfullyRemovedElementTarget = ConfigManager.languageConfig.get().getString("Commands.Remove.Other.RemovedElement");
		this.succesfullyRemovedElementTargetConfirm = ConfigManager.languageConfig.get().getString("Commands.Remove.Other.RemovedElementConfirm");
		this.invalidElement = ConfigManager.languageConfig.get().getString("Commands.Remove.InvalidElement");
		this.wrongElementSelf = ConfigManager.languageConfig.get().getString("Commands.Remove.WrongElement");
		this.wrongElementTarget = ConfigManager.languageConfig.get().getString("Commands.Remove.Other.WrongElement");
		this.playerNotFound = ConfigManager.languageConfig.get().getString("Commands.Remove.PlayerNotFound");
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.hasPermission(sender) || !this.correctLength(sender, args.size(), 1, 2)) {
			return;
		}

		if (args.size() == 1) {
			final Style e = Style.fromString(args.get(0));
			if (e == null) { //The first argument must be a playername instead
				final OfflinePlayer player = Bukkit.getOfflinePlayer(args.get(0));
				if (!player.isOnline() && !player.hasPlayedBefore()) { //Player not found
					ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.playerNotFound);
					return;
				}

				Ninja.getOrLoadOfflineAsync(player).thenAccept(nPlayer -> {
					boolean online = nPlayer instanceof Ninja;
					nPlayer.getElements().clear();
					nPlayer.getSubElements().clear();
					nPlayer.saveElements();
					nPlayer.saveSubElements();
					if (online) ((Ninja)nPlayer).removeUnusableAbilities();
					if (!player.getName().equalsIgnoreCase(sender.getName())) {
						ChatUtil.sendBrandingMessage(sender, ChatColor.YELLOW + this.succesfullyRemovedAllElementsTargetConfirm.replace("{target}", ChatColor.DARK_AQUA + player.getName() + ChatColor.YELLOW));
					}

					if (online) {
						ChatUtil.sendBrandingMessage((Player) player, ChatColor.YELLOW + this.succesfullyRemovedAllElementsTarget.replace("{sender}", ChatColor.DARK_AQUA + sender.getName() + ChatColor.YELLOW));
						Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(sender, (Player) player, null, Result.REMOVE));
					}
				});
			} else { //The first argument is an style
				if (!(sender instanceof Player)) { //Make sure the sender is a player
					help(sender, false);
					return;
				}

				Ninja senderBPlayer = Ninja.getBendingPlayer((Player) sender);
				Player player = (Player) sender;

				if (e instanceof SubElement) { //If it's a subelement
					if (senderBPlayer.hasElement(e)) {
						senderBPlayer.getSubElements().remove(e);
						senderBPlayer.saveSubElements();
						senderBPlayer.removeUnusableAbilities();
						ChatUtil.sendBrandingMessage(sender, e.getColor() + this.succesfullyRemovedElementSelf.replace("{style}", e.toString() + e.getType().getBending()).replace("{sender}", ChatColor.DARK_AQUA + sender.getName() + e.getColor()));
						Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeSubElementEvent(sender, player, (SubElement) e, com.projectkorra.projectkorra.event.PlayerChangeSubElementEvent.Result.REMOVE));
					} else {
						ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.wrongElementSelf);
					}
					return;
				}

				//If it's an style
				if (senderBPlayer.hasElement(e)) {
					senderBPlayer.getElements().remove(e);
					for (final SubElement sub : Style.getSubElements(e)) {
						if (!(sub instanceof Style.MultiSubElement)) senderBPlayer.getSubElements().remove(sub);
						else {
							Style.MultiSubElement multiSubElement = (Style.MultiSubElement) sub;
							boolean keep = false;
							for (Style parent : multiSubElement.getParentElements()) {
								if (senderBPlayer.hasElement(parent)) {
									keep = true;
									break;
								}
							}
							if (!keep) senderBPlayer.getSubElements().remove(sub);
						}
					}
					senderBPlayer.saveElements();
					senderBPlayer.saveSubElements();
					senderBPlayer.removeUnusableAbilities();

					ChatUtil.sendBrandingMessage(sender, e.getColor() + this.succesfullyRemovedElementSelf.replace("{style}", e.toString() + e.getType().getBending()));
					Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(sender, (Player) sender, e, Result.REMOVE));
					return;
				} else {
					ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.wrongElementSelf);
				}
			}
			return;
		}

		//2 arguments
		final OfflinePlayer player = Bukkit.getOfflinePlayer(args.get(0));
		if (!player.isOnline() && !player.hasPlayedBefore()) { //Player not found
			ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.playerNotFound);
			return;
		}

		Style style = Style.fromString(args.get(1));
		if (style == null) {
			ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.invalidElement);
			return;
		}

		Ninja.getOrLoadOfflineAsync(player).thenAccept(nPlayer -> {
			boolean online = nPlayer instanceof Ninja;
			if (!nPlayer.hasElement(style)) {
				ChatUtil.sendBrandingMessage(sender, ChatColor.DARK_RED + this.wrongElementTarget.replace("{target}", player.getName()));
				return;
			}

			if (style instanceof SubElement) {
				nPlayer.getSubElements().remove(style);
				nPlayer.saveSubElements();
			} else {
				nPlayer.getElements().remove(style);
				for (final SubElement sub : Style.getSubElements(style)) {
					if (!(sub instanceof Style.MultiSubElement)) nPlayer.getSubElements().remove(sub);
					else {
						Style.MultiSubElement multiSubElement = (Style.MultiSubElement) sub;
						boolean keep = false;
						for (Style parent : multiSubElement.getParentElements()) {
							if (nPlayer.hasElement(parent)) {
								keep = true;
								break;
							}
						}
						if (!keep) nPlayer.getSubElements().remove(sub);
					}
				}
				nPlayer.saveElements();
				nPlayer.saveSubElements();
			}
			if (player != sender) ChatUtil.sendBrandingMessage(sender, style.getColor() + this.succesfullyRemovedElementTargetConfirm.replace("{style}", style.toString() + style.getType().getBending()).replace("{target}", ChatColor.DARK_AQUA + player.getName() + style.getColor()));

			if (online) {
				((Ninja)nPlayer).removeUnusableAbilities();
				ChatUtil.sendBrandingMessage((Player)player, style.getColor() + this.succesfullyRemovedElementTarget.replace("{style}", style.toString() + style.getType().getBending()).replace("{sender}", ChatColor.DARK_AQUA + sender.getName() + style.getColor()));
				Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(sender, (Player) player, style, Result.REMOVE));
			}
		});

		//---------------

		/*if (player == null) {
			if (args.size() == 1) {

				final Ninja senderBPlayer = Ninja.getBendingPlayer(sender.getName());

				if (senderBPlayer != null && sender instanceof Player) {
					if (e != null) {
						if (e instanceof SubElement) {
							if (senderBPlayer.hasElement(e)) {
								senderBPlayer.getSubElements().remove(e);
								senderBPlayer.saveSubElements();
								senderBPlayer.removeUnusableAbilities();
								GeneralMethods.sendBrandingMessage(sender, e.getColor() + this.succesfullyRemovedElementSelf.replace("{style}", e.toString() + e.getType().getBending()).replace("{sender}", ChatColor.DARK_AQUA + sender.getName() + e.getColor()));
								Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeSubElementEvent(sender, player, (SubElement) e, com.projectkorra.projectkorra.event.PlayerChangeSubElementEvent.Result.REMOVE));
							} else {
								GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.wrongElementSelf);
							}
							return;
						} else if (e instanceof Style) {
							if (senderBPlayer.hasElement(e)) {
								senderBPlayer.getElements().remove(e);
								for (final SubElement sub : Style.getSubElements(e)) {
									senderBPlayer.getSubElements().remove(sub);
								}
								senderBPlayer.saveElements();
								senderBPlayer.saveSubElements();
								senderBPlayer.removeUnusableAbilities();

								GeneralMethods.sendBrandingMessage(sender, e.getColor() + this.succesfullyRemovedElementSelf.replace("{style}", e.toString() + e.getType().getBending()));
								Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(sender, (Player) sender, e, Result.REMOVE));
								return;
							} else {
								GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.wrongElementSelf);
							}
							{
								return;
							}
						}
					} else {
						GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.invalidElement);
					}
					{
						return;
					}
				}
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.playerNotFound);
				return;
			} else {
				this.help(sender, false);
				return;
			}
		}

		Ninja nPlayer = Ninja.getBendingPlayer(player);

		if (args.size() == 2) {
			final Style e = Style.fromString(args.get(1));
			if (e != null) {
				if (!nPlayer.hasElement(e)) {
					GeneralMethods.sendBrandingMessage(sender, ChatColor.DARK_RED + this.wrongElementTarget.replace("{target}", player.getName()));
					return;
				}
				if (e instanceof SubElement) {
					nPlayer.getSubElements().remove(e);
					nPlayer.saveSubElements();
				} else {
					nPlayer.getElements().remove(e);
					for (final SubElement sub : Style.getSubElements(e)) {
						nPlayer.getSubElements().remove(sub);
					}
					nPlayer.saveElements();
					nPlayer.saveSubElements();
				}

				nPlayer.removeUnusableAbilities();
				GeneralMethods.sendBrandingMessage(player, e.getColor() + this.succesfullyRemovedElementTarget.replace("{style}", e.toString() + e.getType().getBending()).replace("{sender}", ChatColor.DARK_AQUA + sender.getName() + e.getColor()));
				GeneralMethods.sendBrandingMessage(sender, e.getColor() + this.succesfullyRemovedElementTargetConfirm.replace("{style}", e.toString() + e.getType().getBending()).replace("{target}", ChatColor.DARK_AQUA + player.getName() + e.getColor()));
				Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(sender, player, e, Result.REMOVE));
				return;
			}
		} else if (args.size() == 1) {
			nPlayer.getElements().clear();
			nPlayer.getSubElements().clear();
			nPlayer.saveElements();
			nPlayer.saveSubElements();
			nPlayer.removeUnusableAbilities();
			if (!player.getName().equalsIgnoreCase(sender.getName())) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.YELLOW + this.succesfullyRemovedAllElementsTargetConfirm.replace("{target}", ChatColor.DARK_AQUA + player.getName() + ChatColor.YELLOW));
			}

			GeneralMethods.sendBrandingMessage(player, ChatColor.YELLOW + this.succesfullyRemovedAllElementsTarget.replace("{sender}", ChatColor.DARK_AQUA + sender.getName() + ChatColor.YELLOW));
			Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(sender, player, null, Result.REMOVE));
		}*/
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (args.size() >= 2 || !sender.hasPermission("jutsu.command.remove")) {
			return new ArrayList<>();
		}
		final List<String> l = new ArrayList<>();
		if (args.size() == 0) {
			return getOnlinePlayerNames(sender);
		} else {
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
		}
		return l;
	}
}
