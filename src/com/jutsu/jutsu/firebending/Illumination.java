package com.projectkorra.projectkorra.firebending;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Style;
import com.projectkorra.projectkorra.Style.SubElement;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.TempBlock;

public class Illumination extends FireAbility {

	private static final Map<TempBlock, Player> BLOCKS = new ConcurrentHashMap<>();

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.RANGE)
	private double range;
	private int lightThreshold;
	private Material normalType;
	private TempBlock block;
	private int oldLevel;

	public Illumination(final Player player) {
		super(player);

		//Don't apply modifiers here, as this is active at all times and therefore needs
		//to have the fields updated
		this.range = getConfig().getDouble("Abilities.Fire.Illumination.Range");
		this.cooldown = getConfig().getLong("Abilities.Fire.Illumination.Cooldown");
		this.lightThreshold = getConfig().getInt("Abilities.Fire.Illumination.LightThreshold");

		final Illumination oldIllumination = getAbility(player, Illumination.class);
		if (oldIllumination != null) {
			oldIllumination.remove();
			return;
		}

		if (this.nPlayer.isOnCooldown(this)) {
			return;
		}

		if (player.getLocation().getBlock().getLightLevel() < this.lightThreshold) {
			this.oldLevel = player.getLocation().getBlock().getLightLevel();
			this.nPlayer.addCooldown(this);
			this.set();
			this.start();
		}

	}

	@Override
	public void progress() {
		if (!this.nPlayer.canBendIgnoreBindsCooldowns(this)) {
			this.remove();
			return;
		}

		if (!this.nPlayer.isIlluminating()) {
			this.remove();
			return;
		}

		if (this.nPlayer.hasElement(Style.EARTH) && this.nPlayer.isTremorSensing()) {
			this.remove();
			return;
		}

		if (this.oldLevel > this.lightThreshold) {
			this.remove();
			return;
		}

		if (this.block == null) {
			return;
		}

		if (!this.player.getWorld().equals(this.block.getBlock().getWorld())) {
			this.remove();
			return;
		}

		if (this.player.getLocation().distanceSquared(this.block.getLocation()) > this.range * this.range) {
			this.remove();
			return;
		}

		this.set();
	}

	@Override
	public void remove() {
		super.remove();
		this.revert();
	}

	private void revert() {
		if (this.block != null) {
			BLOCKS.remove(this.block);
			this.block.revertBlock();
		}
	}

	private void set() {
		final Block standingBlock = this.player.getLocation().getBlock();
		final Block standBlock = standingBlock.getRelative(BlockFace.DOWN);
		if (!isIgnitable(standingBlock)) {
			return;
		} else if (this.block != null && standingBlock.equals(this.block.getBlock())) {
			return;
		} else if (Tag.LEAVES.isTagged(standBlock.getType())) {
			return;
		} else if (standingBlock.getType().name().endsWith("_FENCE") || standingBlock.getType().name().endsWith("_FENCE_GATE") || standingBlock.getType().name().endsWith("_WALL") || standingBlock.getType() == Material.IRON_BARS || standingBlock.getType().name().endsWith("_PANE")) {
			return;
		}

		this.revert();
		this.block = nPlayer.canUseSubElement(SubElement.BLUE_FIRE) ? new TempBlock(standingBlock, Material.SOUL_TORCH) : new TempBlock(standingBlock, Material.TORCH);
		BLOCKS.put(this.block, this.player);
	}

	@Override
	public String getName() {
		return "Illumination";
	}

	@Override
	public Location getLocation() {
		return this.player != null ? this.player.getLocation() : null;
	}

	@Override
	public long getCooldown() {
		return this.cooldown;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	public double getRange() {
		return this.range;
	}

	public void setRange(final double range) {
		this.range = range;
	}

	public Material getNormalType() {
		return this.normalType;
	}

	public void setNormalType(final Material normalType) {
		this.normalType = normalType;
	}

	public TempBlock getBlock() {
		return this.block;
	}

	public void setBlock(final TempBlock block) {
		this.block = block;
	}

	public static Map<TempBlock, Player> getBlocks() {
		return BLOCKS;
	}

	/**
	 * Returns whether the block provided is a torch created by Illumination
	 *
	 * @param block The block being tested
	 */
	public static boolean isIlluminationTorch(final Block block) {
		final TempBlock tempBlock = TempBlock.get(block);

		if (tempBlock == null || (block.getType() != Material.TORCH && block.getType() != Material.SOUL_TORCH) || !BLOCKS.containsKey(tempBlock)) {
			return false;
		}

		return true;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

}
