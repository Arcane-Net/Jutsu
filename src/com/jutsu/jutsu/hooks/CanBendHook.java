package com.projectkorra.projectkorra.hooks;

import com.projectkorra.projectkorra.Ninja;
import com.projectkorra.projectkorra.ability.CoreAbility;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@FunctionalInterface
public interface CanBendHook {

    /**
     * Checks if a player can bend. This should be used to change the way the canBend method functions. By default, this
     * should return {@link Optional#empty()}. To deny jutsu, return {@code Optional.of(false)}. To allow jutsu,
     * return {@code Optional.of(true)}
     * @param nPlayer The player being checked
     * @param ability The ability being checked
     * @param isCheckingBind Whether the binds are being checked or not
     * @param isCheckingCooldown Whether the cooldowns are being checked or not
     * @return Whether to deny or grant the player the ability to use the ability
     */
    @NotNull
    Optional<Boolean> canBend(@NotNull Ninja nPlayer, @NotNull CoreAbility ability, boolean isCheckingBind, boolean isCheckingCooldown);
}
