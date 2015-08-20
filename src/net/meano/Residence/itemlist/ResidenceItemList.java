/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.meano.Residence.itemlist;

import java.util.Map;
import net.meano.Residence.Residence;
import net.meano.Residence.permissions.PermissionGroup;
import net.meano.Residence.protection.ClaimedResidence;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 *
 * @author Administrator
 */
public class ResidenceItemList extends ItemList {
	ClaimedResidence res;

	public ResidenceItemList(ClaimedResidence parent, ListType type) {
		super(type);
		res = parent;
	}

	private ResidenceItemList() {

	}

	public void playerListChange(Player player, Material mat, boolean resadmin) {
		PermissionGroup group = Residence.getPermissionManager().getGroup(player);
		if (resadmin || (res.getPermissions().hasResidencePermission(player, true) && group.itemListAccess())) {
			if (super.toggle(mat))
				player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("ListMaterialAdd", ChatColor.GREEN + mat.toString() + ChatColor.YELLOW + "." + ChatColor.GREEN + type.toString().toLowerCase() + ChatColor.YELLOW));
			else
				player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("ListMaterialRemove", ChatColor.GREEN + mat.toString() + ChatColor.YELLOW + "." + ChatColor.GREEN + type.toString().toLowerCase() + ChatColor.YELLOW));
		} else {
			player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
		}
	}

	public static ResidenceItemList load(ClaimedResidence parent, Map<String, Object> map) {
		ResidenceItemList newlist = new ResidenceItemList();
		newlist.res = parent;
		return (ResidenceItemList) ItemList.load(map, newlist);
	}
}
