// OtherBlocks - a Bukkit plugin
// Copyright (C) 2011 Robert Sargant
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.sargant.bukkit.otherblocks;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.*;
import org.bukkit.material.Colorable;

import com.sargant.bukkit.common.*;

public class OtherBlocksEntityListener extends EntityListener
{	
	private OtherBlocks parent;
	
	public OtherBlocksEntityListener(OtherBlocks instance)
	{
		parent = instance;
	}
	
	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		
		if(!(event instanceof EntityDamageByEntityEvent)) {
			parent.damagerList.remove(event.getEntity());
			return;
		}
		
		EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
		
		if(!(e.getDamager() instanceof Player)) {
			parent.damagerList.remove(event.getEntity());
			return;
		}
		
		Player damager = (Player) e.getDamager();
		
		OtherBlocksDamager d = new OtherBlocksDamager();
		
		d.tool = damager.getItemInHand().getType();
		
		parent.damagerList.put(event.getEntity(), d);
		return;
	}
	
	@Override
	public void onEntityDeath(EntityDeathEvent event)
	{
		// At the moment, we only track creatures killed by humans
		if(!parent.damagerList.containsKey(event.getEntity())) {
			return;
		}
		
		Material weapon = parent.damagerList.get(event.getEntity()).tool;
		Entity victim = event.getEntity();
		CreatureType victimType = CommonEntity.getCreatureType(victim);
		
		parent.damagerList.remove(event.getEntity());
		
		for(OtherBlocksContainer obc : parent.transformList) {
			
			// Check world matches
			if(!obc.worlds.contains(null) && !obc.worlds.contains(event.getEntity().getWorld().getName())) continue;
			
			// Check held item matches
			if(!OtherBlocks.containsValidToolString(weapon.toString(), obc.tool)) continue;

			// Check target matches
			if(!OtherBlocks.isCreature(obc.original) || CreatureType.valueOf(OtherBlocks.creatureName(obc.original)) != victimType) {
				continue;
			}
			
			// Check if creature is Colorable and if we have a color to match
			if((obc.originalData != null) && (victim instanceof Colorable)) {
				if(((Colorable) victim).getColor().getData() < obc.originalData
				        || ((Colorable) victim).getColor().getData() > obc.originalDataRangeMax)
				    continue;
			}

			// Check probability is great than the RNG
			if(parent.rng.nextDouble() > (obc.chance.doubleValue()/100)) continue;
			
			setQuantity(obc);

			event.getDrops().clear();
			Location location = victim.getLocation();
			
			OtherBlocks.performDrop(location, obc);
		}
	}
	
	private void setQuantity(OtherBlocksContainer obc) {
        if(obc.min_quantity == null) obc.quantity = 1;
        else if(obc.max_quantity == null) obc.quantity = obc.min_quantity;
        else obc.quantity = obc.min_quantity + parent.rng.nextInt(obc.max_quantity - obc.min_quantity + 1);
    }
}
