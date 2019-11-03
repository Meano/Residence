/*
package net.meano.Residence.permissions;

import java.util.List;
import net.meano.PermissionsBukkit.Group;
import net.meano.PermissionsBukkit.PermissionsPlugin;
import net.meano.Residence.Residence;
import org.bukkit.entity.Player;

public class PermissionsBukkitAdapter implements PermissionsInterface {

	PermissionsPlugin newperms;

	public PermissionsBukkitAdapter(PermissionsPlugin p) {
		newperms = p;
	}

	public String getPlayerGroup(Player player) {
		return this.getPlayerGroup(player.getName(), player.getWorld().getName());
	}

	public String getPlayerGroup(String player, String world) {
		PermissionManager pmanager = Residence.getPermissionManager();
		@SuppressWarnings("deprecation")
		List<Group> groups = newperms.getGroups(player);
		for (Group group : groups) {
			String name = group.getName().toLowerCase();
			if (pmanager.hasGroup(name)) {
				return name;
			}
		}
		if (groups.size() > 0) {
			return groups.get(0).getName().toLowerCase();
		}
		return null;
	}

}

 */