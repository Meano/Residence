/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.meano.Residence.event;

import net.meano.Residence.Residence;
import net.meano.Residence.protection.ClaimedResidence;
import org.bukkit.entity.Player;

/**
 *
 * @author Administrator
 */
public class ResidencePlayerEvent extends ResidenceEvent implements ResidencePlayerEventInterface {

	Player p;

	public ResidencePlayerEvent(String eventName, ClaimedResidence resref, Player player) {
		super(eventName, resref);
		res = resref;
		p = player;
	}

	public boolean isPlayer() {
		return p != null;
	}

	public boolean isAdmin() {
		if (isPlayer()) {
			return Residence.getPermissionManager().isResidenceAdmin(p);
		}
		return true;
	}

	public Player getPlayer() {
		return p;
	}
}
