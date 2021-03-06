package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.TargetingSpell;
import com.elmakers.mine.bukkit.utility.Target;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Deprecated
public class HealSpell extends TargetingSpell 
{
    private final static PotionEffectType[] _negativeEffects =
            {PotionEffectType.BLINDNESS, PotionEffectType.CONFUSION, PotionEffectType.HARM,
             PotionEffectType.HUNGER, PotionEffectType.POISON, PotionEffectType.SLOW,
             PotionEffectType.SLOW_DIGGING, PotionEffectType.WEAKNESS, PotionEffectType.WITHER};
    protected final static Set<PotionEffectType> negativeEffects = new HashSet<PotionEffectType>(Arrays.asList(_negativeEffects));

    @Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Target target = getTarget();
		Entity targetEntity = target.getEntity();
        if (targetEntity == null || !(targetEntity instanceof LivingEntity)) {
            return SpellResult.NO_TARGET;
        }
        LivingEntity li = (LivingEntity)targetEntity;

        double health = li.getHealth();
        if (parameters.contains("amount")) {
            health = health + parameters.getDouble("amount");
        } else if (parameters.contains("percentage")) {
            health = health + li.getMaxHealth() * parameters.getDouble("percentage");
        } else {
            health = li.getMaxHealth();
        }

        li.setHealth(Math.min(health, li.getMaxHealth()));
        if (targetEntity instanceof Player && parameters.getBoolean("feed", false)) {
            Player p = (Player)targetEntity;
            p.setExhaustion(0);
            p.setFoodLevel(20);
        }
        if (parameters.getBoolean("cure", false)) {
            Collection<PotionEffect> effects = li.getActivePotionEffects();
            for (PotionEffect effect : effects) {
                if (negativeEffects.contains(effect.getType())) {
                    li.removePotionEffect(effect.getType());
                }
            }
        }
        return SpellResult.CAST;
	}
}
