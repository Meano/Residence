/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.meano.Residence.listeners;

import net.meano.Residence.Residence;
import net.meano.Residence.protection.ClaimedResidence;
import net.meano.Residence.protection.FlagPermissions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;

/**
 *
 * @author Administrator
 */
public class ResidenceBlockListener implements Listener {

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if (Residence.isResAdminOn(player)) {
			return;
		}
		Material mat = event.getBlock().getType();
		String world = event.getBlock().getWorld().getName();
		String group = Residence.getPermissionManager().getGroupNameByPlayer(player);
		if (Residence.getItemManager().isIgnored(mat, group, world)) {
			return;
		}
		ClaimedResidence res = Residence.getResidenceManager().getByLoc(event.getBlock().getLocation());
		if (Residence.getConfigManager().enabledRentSystem()) {
			if (res != null) {
				String resname = res.getName();
				if (Residence.getConfigManager().preventRentModify() && Residence.getRentManager().isRented(resname)) {
					player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("RentedModifyDeny"));
					event.setCancelled(true);
					return;
				}
			}
		}
		FlagPermissions perms = Residence.getPermsByLocForPlayer(event.getBlock().getLocation(), player);
		String pname = player.getName();
		if (res != null) {
			if (res.getItemIgnoreList().isListed(mat)) {
				return;
			}
		}
		boolean hasdestroy = perms.playerHas(pname, player.getWorld().getName(), "destroy", perms.playerHas(pname, player.getWorld().getName(), "build", true));
		boolean hasContainer = perms.playerHas(pname, player.getWorld().getName(), "container", true);
		if (!hasdestroy || (!hasContainer && mat == Material.CHEST)) {
			event.setCancelled(true);
			player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
			return;
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		if (Residence.isResAdminOn(player)) {
			return;
		}
		Material mat = event.getBlock().getType();
		String world = event.getBlock().getWorld().getName();
		String group = Residence.getPermissionManager().getGroupNameByPlayer(player);
		if (Residence.getItemManager().isIgnored(mat, group, world)) {
			return;
		}
		ClaimedResidence res = Residence.getResidenceManager().getByLoc(event.getBlock().getLocation());
		if (Residence.getConfigManager().enabledRentSystem()) {
			if (res != null) {
				String resname = res.getName();
				if (Residence.getConfigManager().preventRentModify() && Residence.getRentManager().isRented(resname)) {
					player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("RentedModifyDeny"));
					event.setCancelled(true);
					return;
				}
			}
		}
		String pname = player.getName();
		if (res != null) {
			if (!res.getItemBlacklist().isAllowed(mat)) {
				player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ItemBlacklisted"));
				event.setCancelled(true);
				return;
			}
		}
		FlagPermissions perms = Residence.getPermsByLocForPlayer(event.getBlock().getLocation(), player);
		boolean hasplace = perms.playerHas(pname, player.getWorld().getName(), "place", perms.playerHas(pname, player.getWorld().getName(), "build", true));
		if (!hasplace) {
			event.setCancelled(true);
			player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
			return;
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockSpread(BlockSpreadEvent event) {
		Location loc = event.getBlock().getLocation();
		FlagPermissions perms = Residence.getPermsByLoc(loc);
		if (!perms.has("spread", true)) {
			event.setCancelled(true);
		}
	}

	//史莱姆方块查询
	public Block getEndSlimeBlock(Block FirstSlimeBlock, BlockFace SlimeBlockDirection) {
		Block EndSlimeBlock;
		EndSlimeBlock = FirstSlimeBlock;
		for (int i = 0; i < 10; i++) {
			FirstSlimeBlock = FirstSlimeBlock.getRelative(SlimeBlockDirection);
			if (FirstSlimeBlock.getType().equals(Material.SLIME_BLOCK)) {
				EndSlimeBlock = FirstSlimeBlock;
			}
		}
		return EndSlimeBlock.getRelative(SlimeBlockDirection);
	}
	
	//活塞拉动事件
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockPistonRetract(BlockPistonRetractEvent event) {
		FlagPermissions perms = Residence.getPermsByLoc(event.getBlock().getLocation());
		if (!perms.has("piston", true)) {
			event.setCancelled(true);
			return;
		}
		//--------------------------------------------------------------------------------------------------------------------------//
		//粘性活塞及粘液块拉动保护
		BlockFace RetractFace = event.getDirection().getOppositeFace();
		Block RetractBlock = event.getBlock().getRelative(RetractFace).getRelative(RetractFace);
		ClaimedResidence resPiston = Residence.getResidenceManager().getByLoc(event.getBlock().getLocation());
		// 若为粘性活塞
		if (event.getBlock().getType().equals(Material.PISTON_MOVING_PIECE)) {
			// 若拉动方块为史莱姆方块
			if (RetractBlock.getType().equals(Material.SLIME_BLOCK)) {
				ClaimedResidence resEndSlimeBlock = Residence.getResidenceManager().getByLoc(getEndSlimeBlock(RetractBlock, RetractFace).getLocation());
				if (resEndSlimeBlock != resPiston) {
					event.setCancelled(true);
					Bukkit.broadcast("拉动取消！", "AnimalsRestrict.SlimeBlock");
				}
			} else {
				ClaimedResidence resRetract = Residence.getResidenceManager().getByLoc(RetractBlock.getLocation());
				if (resRetract != resPiston) {
					event.setCancelled(true);
					Bukkit.broadcast("拉动取消！", "AnimalsRestrict.SlimeBlock");
				}
				// }
			}
		}
		//--------------------------------------------------------------------------------------------------------------------------//
		/*if (event.isSticky()) {
			@SuppressWarnings("deprecation")
			Location location = event.getRetractLocation();
			FlagPermissions blockperms = Residence.getPermsByLoc(location);
			if (!blockperms.has("piston", true)) {
				event.setCancelled(true);
			}
		}*/
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockPistonExtend(BlockPistonExtendEvent event) {
		FlagPermissions perms = Residence.getPermsByLoc(event.getBlock().getLocation());
		if (!perms.has("piston", true)) {
			event.setCancelled(true);
			return;
		}
		ClaimedResidence resExtend;
		ClaimedResidence resPiston = Residence.getResidenceManager().getByLoc(event.getBlock().getLocation());
		if (resPiston == null) {
			for (Block b : event.getBlocks()) {
				resExtend = Residence.getResidenceManager().getByLoc(b.getLocation());
				if (resExtend != null) {
					event.setCancelled(true);
					Bukkit.broadcast("发生从领地外推动物品进领地的事件！", "AnimalsRestrict.SlimeBlock");
					return;
				}
			}
		}
		/*for (Block block : event.getBlocks()) {
			FlagPermissions blockpermsfrom = Residence.getPermsByLoc(block.getLocation());
			Location blockto = block.getLocation();
			blockto.setX(blockto.getX() + event.getDirection().getModX());
			blockto.setY(blockto.getY() + event.getDirection().getModY());
			blockto.setZ(blockto.getZ() + event.getDirection().getModZ());
			FlagPermissions blockpermsto = Residence.getPermsByLoc(blockto);
			if (!blockpermsfrom.has("piston", true) || !blockpermsto.has("piston", true)) {
				event.setCancelled(true);
				return;
			}
		}*/
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockFromTo(BlockFromToEvent event) {
		FlagPermissions perms = Residence.getPermsByLoc(event.getToBlock().getLocation());
		boolean hasflow = perms.has("flow", true);
		Material mat = event.getBlock().getType();
		if (!hasflow) {
			event.setCancelled(true);
			return;
		}
		if (mat == Material.LAVA || mat == Material.STATIONARY_LAVA) {
			if (!perms.has("lavaflow", hasflow)) {
				event.setCancelled(true);
			}
			return;
		}
		if (mat == Material.WATER || mat == Material.STATIONARY_WATER) {
			if (!perms.has("waterflow", hasflow)) {
				event.setCancelled(true);
			}
			return;
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockBurn(BlockBurnEvent event) {
		FlagPermissions perms = Residence.getPermsByLoc(event.getBlock().getLocation());
		if (!perms.has("firespread", true)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockIgnite(BlockIgniteEvent event) {
		FlagPermissions perms = Residence.getPermsByLocForPlayer(event.getBlock().getLocation(), event.getPlayer());
		IgniteCause cause = event.getCause();
		if (cause == IgniteCause.SPREAD) {
			if (!perms.has("firespread", true)) {
				event.setCancelled(true);
			}
		} else if (cause == IgniteCause.FLINT_AND_STEEL) {
			Player player = event.getPlayer();
			if (player != null && !perms.playerHas(player.getName(), player.getWorld().getName(), "ignite", true) && !Residence.isResAdminOn(player)) {
				event.setCancelled(true);
				player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
			}
		} else {
			if (!perms.has("ignite", true)) {
				event.setCancelled(true);
			}
		}
	}
}
