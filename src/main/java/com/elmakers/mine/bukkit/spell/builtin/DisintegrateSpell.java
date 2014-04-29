package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BlockSpell;
import com.elmakers.mine.bukkit.utility.Target;

public class DisintegrateSpell extends BlockSpell
{
	private final static int             DEFAULT_PLAYER_DAMAGE = 1;
	private final static int             DEFAULT_ENTITY_DAMAGE = 100;

	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Target target = getTarget();
		
		int playerDamage = parameters.getInt("player_damage", DEFAULT_PLAYER_DAMAGE);
		int entityDamage = parameters.getInt("entity_damage", DEFAULT_ENTITY_DAMAGE);

		if (target.hasEntity())
		{
			Entity targetEntity = target.getEntity();
			if (targetEntity instanceof LivingEntity)
			{
				LivingEntity li = (LivingEntity)targetEntity;
				if (li instanceof Player)
				{
					li.damage(mage.getDamageMultiplier() * playerDamage, getPlayer());
				}
				else
				{
					li.damage(mage.getDamageMultiplier() * entityDamage, getPlayer());
					if (li.isDead()) {
						registerRemoved(li);
						registerForUndo();
					}
				}
				return SpellResult.CAST;
			}
		}

		if (!target.hasTarget())
		{
			return SpellResult.NO_TARGET;
		}
		
		Block targetBlock = target.getBlock();
		if (!hasBuildPermission(targetBlock)) 
		{
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		if (mage.isIndestructible(targetBlock)) 
		{
			return SpellResult.NO_TARGET;
		}

		registerForUndo(targetBlock);
		
		if (isUnderwater())
		{
			targetBlock.setType(Material.STATIONARY_WATER);
		}
		else
		{
			targetBlock.setType(Material.AIR);
		}
		
		registerForUndo();
		return SpellResult.CAST;
	}
}
