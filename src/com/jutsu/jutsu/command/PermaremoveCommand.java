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
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent.Result;

/**
 * Executor for /jutsu permaremove. Extends {@link PKCommand}.
 */
public class PermaremoveCommand extends PKCommand {

	private final String invalidPlayer;
	private final String restored;
	private final String restoredConfirm;
	private final String removed;
	private final String removedConfirm;

	public PermaremoveCommand() {
		super("permaremove", "/jutsu permaremove <Player>", ConfigManager.languageConfig.get().getString("Commands.PermaRemove.Description"), new String[] { "permaremove", "premove", "permremove", "pr" });

		this.invalidPlayer = ConfigManager.languageConfig.get().getString("Commands.PermaRemove.InvalidPlayer");
		this.restored = ConfigManager.languageConfig.get().getString("Commands.PermaRemove.Restored");
		this.restoredConfirm = ConfigManager.languageConfig.get().getString("Commands.PermaRemove.RestoredConfirm");
		this.removed = ConfigManager.languageConfig.get().getString("Commands.PermaRemove.Removed");
		this.removedConfirm = ConfigManager.languageConfig.get().getString("Commands.PermaRemove.RemovedConfirm");
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.hasPermission(sender) || !this.correctLength(sender, args.size(), 0, 1)) {
			return;
		}
		if (args.size() == 1) {
			this.permaremove(sender, args.get(0));
		} else if (args.size() == 0 && this.isPlayer(sender)) {
			this.permaremove(sender, sender.getName());
		}
	}

	/**
	 * Permanently removes a player's jutsu, or restores it if it had already
	 * been permaremoved.
	 *
	 * @param sender The CommandSender who issued the permaremove command
	 * @param target The Player who's jutsu should be permaremoved
	 */
	private void permaremove(final CommandSender sender, final String target) {
		final OfflinePlayer player = Bukkit.getOfflinePlayer(target);
		if (!player.isOnline() && !player.hasPlayedBefore()) {
			ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.invalidPlayer);
			return;
		}

		Ninja.getOrLoadOfflineAsync(player).thenAccept(nPlayer -> {
			boolean online = nPlayer instanceof Ninja;
			if (nPlayer.isPermaRemoved()) {
				nPlayer.setPermaRemoved(false);
				if (online) ChatUtil.sendBrandingMessage((Player) player, ChatColor.GREEN + this.restored);
				if (!(sender instanceof Player) || !sender.getName().equalsIgnoreCase(target)) {
					ChatUtil.sendBrandingMessage(sender, ChatColor.GREEN + this.restoredConfirm.replace("{target}", ChatColor.DARK_AQUA + player.getName() + ChatColor.GREEN));
				}
			} else {
				nPlayer.getElements().clear();
				nPlayer.saveElements();
				nPlayer.setPermaRemoved(true);
				if (online) {
					((Ninja)nPlayer).removeUnusableAbilities();
					ChatUtil.sendBrandingMessage((Player) player, ChatColor.RED + this.removed);
					Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(sender, (Player) player, null, Result.PERMAREMOVE));
				}

				if (!(sender instanceof Player) || !sender.getName().equalsIgnoreCase(target)) {
					ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.removedConfirm.replace("{target}", ChatColor.DARK_AQUA + player.getName() + ChatColor.RED));
				}

			}
		});


	}

	/**
	 * Checks if the sender has the permission 'jutsu.admin.permaremove'. If
	 * not, it tells them they don't have permission to use the command.
	 *
	 * @return True if they have the permission, false otherwise
	 */
	@Override
	public boolean hasPermission(final CommandSender sender) {
		if (!sender.hasPermission("jutsu.admin.permaremove")) {
			ChatUtil.sendBrandingMessage(sender, super.noPermissionMessage);
			return false;
		}
		return true;
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (args.size() >= 1 || !sender.hasPermission("jutsu.command.permaremove")) {
			return new ArrayList<String>();
		}
		return getOnlinePlayerNames(sender);
	}
}
