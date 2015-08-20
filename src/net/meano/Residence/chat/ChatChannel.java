/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.meano.Residence.chat;

import java.util.ArrayList;
import java.util.List;
import net.meano.Residence.Residence;
import net.meano.Residence.event.ResidenceChatEvent;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class ChatChannel {

	protected String name;
	protected List<String> members;

	public ChatChannel(String channelName) {
		name = channelName;
		members = new ArrayList<String>();
	}

	public void chat(String sourcePlayer, String message) {
		Server serv = Residence.getServ();
		ChatColor color = Residence.getConfigManager().getChatColor();
		ResidenceChatEvent cevent = new ResidenceChatEvent(Residence.getResidenceManager().getByName(name), serv.getPlayer(sourcePlayer), message, color);
		Residence.getServ().getPluginManager().callEvent(cevent);
		if (cevent.isCancelled())
			return;
		for (String member : members) {
			Player player = serv.getPlayer(member);
			if (player != null)
				player.sendMessage(cevent.getColor() + sourcePlayer + ": " + cevent.getChatMessage());
		}
		System.out.println("ResidentialChat[" + name + "] - " + sourcePlayer + ": " + cevent.getChatMessage());
	}

	public void join(String player) {
		if (!members.contains(player))
			members.add(player);
	}

	public void leave(String player) {
		members.remove(player);
	}

	public boolean hasMember(String player) {
		return members.contains(player);
	}

	public int memberCount() {
		return members.size();
	}
}
