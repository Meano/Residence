/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.meano.Residence.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.meano.Residence.Residence;
import net.meano.Residence.chat.ChatChannel;
import net.meano.Residence.event.ResidenceChangedEvent;
import net.meano.Residence.permissions.PermissionGroup;
import net.meano.Residence.protection.ClaimedResidence;
import net.meano.Residence.protection.FlagPermissions;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;


public class ResidencePlayerListener implements Listener {

	protected Map<String, String> currentRes;
	protected Map<String, Long> lastUpdate;
	protected Map<String, Location> lastOutsideLoc;
	protected int minUpdateTime;
	protected boolean chatenabled;
	protected List<String> playerToggleChat;

	public ResidencePlayerListener() {
		currentRes = new HashMap<String, String>();
		lastUpdate = new HashMap<String, Long>();
		lastOutsideLoc = new HashMap<String, Location>();
		playerToggleChat = new ArrayList<String>();
		minUpdateTime = Residence.getConfigManager().getMinMoveUpdateInterval();
		chatenabled = Residence.getConfigManager().chatEnabled();
		for (Player player : Residence.getServ().getOnlinePlayers()) {
			lastUpdate.put(player.getName(), System.currentTimeMillis());
		}
	}

	public void reload() {
		currentRes = new HashMap<String, String>();
		lastUpdate = new HashMap<String, Long>();
		lastOutsideLoc = new HashMap<String, Location>();
		playerToggleChat = new ArrayList<String>();
		minUpdateTime = Residence.getConfigManager().getMinMoveUpdateInterval();
		chatenabled = Residence.getConfigManager().chatEnabled();
		for (Player player : Residence.getServ().getOnlinePlayers()) {
			lastUpdate.put(player.getName(), System.currentTimeMillis());
		}
	}

	//玩家退出游戏事件
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event) {
		String pname = event.getPlayer().getName();
		currentRes.remove(pname);
		lastUpdate.remove(pname);
		lastOutsideLoc.remove(pname);
		Residence.getChatManager().removeFromChannel(pname);
	}
	
	//玩家登录游戏事件
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		lastUpdate.put(player.getName(), 0L);
		if (Residence.getPermissionManager().isResidenceAdmin(player)) {
			Residence.turnResAdminOn(player);
		}
		handleNewLocation(player, player.getLocation(), false);
	}
	
	//玩家重生事件
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerSpawn(PlayerRespawnEvent event) {
		Location loc = event.getRespawnLocation();
		Boolean bed = event.isBedSpawn();
		Player player = event.getPlayer();
		ClaimedResidence res = Residence.getResidenceManager().getByLoc(loc);
		if (res == null) {
			return;
		}
		if (res.getPermissions().playerHas(player.getName(), "move", true)) {
			return;
		}
		if (bed) {
			loc = player.getWorld().getSpawnLocation();
		}
		res = Residence.getResidenceManager().getByLoc(loc);
		if (res != null) {
			if (!res.getPermissions().playerHas(player.getName(), "move", true)) {
				loc = res.getOutsideFreeLoc(loc);
			}
		}
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoSpawn"));
		event.setRespawnLocation(loc);
	}

	//是否是箱子
	private boolean isContainer(Material mat, Block block) {
		return FlagPermissions.getMaterialUseFlagList().containsKey(mat) && FlagPermissions.getMaterialUseFlagList().get(mat).equals("container") || Residence.getConfigManager().getCustomContainers().contains(block.getType().toString());
	}

	private boolean isCanUseEntity_BothClick(Material mat, Block block) {
		return mat == Material.LEVER ||
				mat == Material.STONE_BUTTON ||
				mat == Material.ACACIA_BUTTON ||
				
				mat == Material.OAK_DOOR ||
				mat == Material.SPRUCE_DOOR ||
				mat == Material.BIRCH_DOOR ||
				mat == Material.JUNGLE_DOOR ||
				mat == Material.ACACIA_DOOR ||
				mat == Material.DARK_OAK_DOOR ||

				mat == Material.OAK_FENCE_GATE ||
				mat == Material.SPRUCE_FENCE_GATE ||
				mat == Material.BIRCH_FENCE_GATE ||
				mat == Material.JUNGLE_FENCE_GATE ||
				mat == Material.ACACIA_FENCE_GATE ||
				mat == Material.DARK_OAK_FENCE_GATE ||
				
				// TODO
				mat == Material.OAK_TRAPDOOR ||
				mat == Material.SPRUCE_TRAPDOOR ||
				mat == Material.BIRCH_TRAPDOOR ||
				mat == Material.JUNGLE_TRAPDOOR ||
				mat == Material.ACACIA_TRAPDOOR ||
				mat == Material.DARK_OAK_TRAPDOOR ||
				
				mat == Material.PISTON ||
				mat == Material.PISTON_HEAD ||
				mat == Material.MOVING_PISTON ||
				mat == Material.STICKY_PISTON ||
				
				mat == Material.DRAGON_EGG || 
				Residence.getConfigManager().getCustomBothClick().contains(block.getType().toString());
	}

//	private boolean isCanUseEntity_RClickOnly(Material mat, Block block) {
//		return mat == Material.ITEM_FRAME ||
//				mat == Material.BEACON || 
//				mat == Material.FLOWER_POT ||
//				mat == Material.COMMAND_BLOCK ||
//				mat == Material.ANVIL ||
//				mat == Material.CAKE ||
//				mat == Material.NOTE_BLOCK ||
//				mat == Material.COMPARATOR ||
//				mat == Material.REPEATER  ||
//				
//				// Bed
//				mat == Material.BLACK_BED ||
//				mat == Material.BLUE_BED ||
//				mat == Material.BROWN_BED ||
//				mat == Material.CYAN_BED ||
//				mat == Material.GRAY_BED ||
//				mat == Material.GREEN_BED ||
//				mat == Material.LIGHT_BLUE_BED ||
//				mat == Material.LIGHT_GRAY_BED ||
//				mat == Material.LIME_BED ||
//				mat == Material.MAGENTA_BED ||
//				mat == Material.ORANGE_BED ||
//				mat == Material.PINK_BED ||
//				mat == Material.PURPLE_BED ||
//				mat == Material.RED_BED ||
//				mat == Material.WHITE_BED ||
//				mat == Material.YELLOW_BED ||
//				
//				mat == Material.CRAFTING_TABLE ||
//				mat == Material.BREWING_STAND ||
//				mat == Material.ENCHANTING_TABLE ||
//				Residence.getConfigManager().getCustomRightClick().contains(block.getType().toString());
//	}

	private boolean isCanUseEntity(Material mat, Block block) {
		return FlagPermissions.getMaterialUseFlagList().containsKey(mat) && !FlagPermissions.getMaterialUseFlagList().get(mat).equals("container");
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Material heldItem = player.getInventory().getItemInMainHand().getType();
		Block block = event.getClickedBlock();
		if (block == null) {
			return;
		}
		Material mat = block.getType();
		if (
			!(
				(isContainer(mat, block) || isCanUseEntity(mat, block)) && event.getAction() == Action.RIGHT_CLICK_BLOCK ||
				isCanUseEntity_BothClick(mat, block) || event.getAction() == Action.PHYSICAL
			)
		) {
			if (heldItem != Residence.getConfigManager().getSelectionTooldID() && heldItem != Residence.getConfigManager().getInfoToolID()) {
				return;
			}
		}
		FlagPermissions perms = Residence.getPermsByLocForPlayer(block.getLocation(), player);
		String world = player.getWorld().getName();
		String permgroup = Residence.getPermissionManager().getGroupNameByPlayer(player);
		boolean resadmin = Residence.isResAdminOn(player);
		if (event.getAction() == Action.PHYSICAL) {
			if (!resadmin) {
				boolean hasuse = perms.playerHas(player.getName(), world, "use", true);
				boolean haspressure = perms.playerHas(player.getName(), world, "pressure", hasuse);
				if ((!hasuse && !haspressure || !haspressure) && 
						(mat == Material.STONE_PRESSURE_PLATE || 
						mat == Material.ACACIA_PRESSURE_PLATE || 
						mat == Material.BIRCH_PRESSURE_PLATE || 
						mat == Material.DARK_OAK_PRESSURE_PLATE ||
						mat == Material.HEAVY_WEIGHTED_PRESSURE_PLATE ||
						mat == Material.JUNGLE_PRESSURE_PLATE ||
						mat == Material.LIGHT_WEIGHTED_PRESSURE_PLATE ||
						mat == Material.OAK_PRESSURE_PLATE ||
						mat == Material.SPRUCE_PRESSURE_PLATE)
					) {
					event.setCancelled(true);
					return;
				}
			}
			if (!perms.playerHas(player.getName(), world, "trample", perms.playerHas(player.getName(), world, "build", true)) && (mat == Material.COARSE_DIRT || mat == Material.SOUL_SAND)) {
				event.setCancelled(true);
				return;
			}
			return;
		}
		if (!resadmin && !Residence.getItemManager().isAllowed(heldItem, permgroup, world)) {
			player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ItemBlacklisted"));
			event.setCancelled(true);
			return;
		}
		if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (player.getInventory().getItemInMainHand().getType() == Residence.getConfigManager().getSelectionTooldID()) {
				PermissionGroup group = Residence.getPermissionManager().getGroup(player);
				if (
						player.hasPermission("residence.select") ||
						player.hasPermission("residence.create") &&
						!player.isPermissionSet("residence.select") ||
						group.canCreateResidences() &&
						!player.isPermissionSet("residence.create") &&
						!player.isPermissionSet("residence.select") ||
						resadmin
				) {
					if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
						if(Residence.getConfigManager().IsSelectChunk()) {
							Chunk chunk = block.getChunk();
							Residence.getSelectionManager().SelectChunk(player, chunk, true);
							player.sendMessage(
									ChatColor.GREEN + Residence.getLanguage().getPhrase("SelectChunk", Residence.getLanguage().getPhrase("Primary")) + 
									ChatColor.RED + "(" + chunk.getX() + "," + chunk.getZ() + ")" +
									ChatColor.GREEN + " !"
								);
							if(Residence.getSelectionManager().hasPlacedBoth(player.getName()))
								Residence.getSelectionManager().showSelectionInfo(player);
						}
						else {
							Location loc = block.getLocation();
							Residence.getSelectionManager().placeLoc1(player, loc);
							player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("SelectPoint", Residence.getLanguage().getPhrase("Primary")) + ChatColor.RED + "(" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + ")" + ChatColor.GREEN + "!");
						}
					}
					else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
						if(Residence.getConfigManager().IsSelectChunk()) {
							Chunk chunk = block.getChunk();
							Residence.getSelectionManager().SelectChunk(player, chunk, false);
							player.sendMessage(
									ChatColor.GREEN + Residence.getLanguage().getPhrase("SelectChunk", Residence.getLanguage().getPhrase("Secondary")) + 
									ChatColor.RED + "(" + chunk.getX() + "," + chunk.getZ() + ")" +
									ChatColor.GREEN + " !"
								);
							if(Residence.getSelectionManager().hasPlacedBoth(player.getName()))
								Residence.getSelectionManager().showSelectionInfo(player);
						}
						else {
							Location loc = block.getLocation();
							Residence.getSelectionManager().placeLoc2(player, loc);
							player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("SelectPoint", Residence.getLanguage().getPhrase("Secondary")) + ChatColor.RED + "(" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + ")" + ChatColor.GREEN + "!");
						}
					}
				}
			} 
			else if (player.getInventory().getItemInMainHand().getType() == Residence.getConfigManager().getInfoToolID()) {
				if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
					Location loc = block.getLocation();
					String res = Residence.getResidenceManager().getNameByLoc(loc);
					if (res != null) {
						Residence.getResidenceManager().printAreaInfo(res, player);
						event.setCancelled(true);
					}
					if (res == null) {
						event.setCancelled(true);
						player.sendMessage(Residence.getLanguage().getPhrase("NoResHere"));
					}
				}
			}
			if (!resadmin) {
				if (heldItem != null) {
					if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
//						if (player.getInventory().getItemInMainHand().getType() == Material.STRING) {
//							if (player.getInventory().getItemInMainHand().getData().getData() == 15 && block.getType() == Material.GRASS || player.getInventory().getItemInMainHand().getData().getData() == 3 && /*block.getType() == 17 &&*/ (block.getData() == 3 || block.getData() == 7 || block.getData() == 11 || block.getData() == 15)) {
//								perms = Residence.getPermsByLocForPlayer(block.getRelative(event.getBlockFace()).getLocation(), player);
//								if (!perms.playerHas(player.getName(), world, "build", true)) {
//									event.setCancelled(true);
//									return;
//								}
//							}
//						}
						if (heldItem == Material.ARMOR_STAND) {
							perms = Residence.getPermsByLocForPlayer(block.getRelative(event.getBlockFace()).getLocation(), player);
							if (!perms.playerHas(player.getName(), world, "place", perms.playerHas(player.getName(), world, "build", true))) {
								player.sendMessage(ChatColor.RED + "不要在这里放置盔甲架，" + Residence.getLanguage().getPhrase("FlagDeny", "place"));
								event.setCancelled(true);
								return;
							}
						}
					}
				}
				if (isContainer(mat, block) || isCanUseEntity(mat, block)) {
					boolean hasuse = perms.playerHas(player.getName(), world, "use", true);
					String matFlag = FlagPermissions.getMaterialUseFlagList().get(mat);
					
					if(matFlag != null) {
						if (!perms.playerHas(player.getName(), world, matFlag, hasuse)) {
							if (hasuse) {
								if(!matFlag.equals("barrel") || !perms.playerHas(player.getName(), world, "container", true)) {
									event.setCancelled(true);
									player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagDeny", matFlag));
									return;
								}
							}
							else {
								event.setCancelled(true);
								player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagDeny", "use"));
								return;
							}
						}
					}
					
//					for (Entry<Material, String> checkMat : FlagPermissions.getMaterialUseFlagList().entrySet()) {
//						if (mat == checkMat.getKey()) {
//							if (!perms.playerHas(player.getName(), world, checkMat.getValue(), hasuse)) {
//								if (hasuse || checkMat.getValue().equals("container")) {
//									event.setCancelled(true);
//									player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagDeny", checkMat.getValue()));
//									return;
//								} else {
//									event.setCancelled(true);
//									player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagDeny", "use"));
//									return;
//								}
//							}
//						}
//					}

					if (Residence.getConfigManager().getCustomContainers().contains(block.getType().toString())) {
						if (!perms.playerHas(player.getName(), world, "container", hasuse)) {
							event.setCancelled(true);
							player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagDeny", "container"));
							return;
						}
					}
					if (Residence.getConfigManager().getCustomBothClick().contains(block.getType().toString())) {
						if (!hasuse) {
							event.setCancelled(true);
							player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagDeny", "use"));
							return;
						}
					}
					if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
						if (Residence.getConfigManager().getCustomRightClick().contains(block.getType().toString())) {
							if (!hasuse) {
								event.setCancelled(true);
								player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagDeny", "use"));
								return;
							}
						}
					}
				}
			}
		}
	}
	
	//玩家与实体如挂画等交互事件
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		if (Residence.isResAdminOn(player)) {
			return;
		}
		Entity ClickedEntity = event.getRightClicked();
		FlagPermissions perms = Residence.getPermsByLocForPlayer(ClickedEntity.getLocation(), player);
		String world = player.getWorld().getName();
		if (ClickedEntity instanceof Hanging) {
			Hanging hanging = (Hanging) ClickedEntity;
			if (hanging.getType() != EntityType.ITEM_FRAME) {
				return;
			}
			//Material heldItem = player.getItemInHand().getType();
			/*String permgroup = Residence.getPermissionManager().getGroupNameByPlayer(player);
			if (!Residence.getItemManager().isAllowed(heldItem, permgroup, world)) {
				player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ItemBlacklisted"));
				event.setCancelled(true);
				return;
			}*/
			if (!perms.playerHas(player.getName(), world, "use", true)) {
				event.setCancelled(true);
				player.sendMessage(ChatColor.RED + "不要试图使用领地内的物品展示框，" + Residence.getLanguage().getPhrase("FlagDeny", "use"));
			}
		}else if(ClickedEntity instanceof Villager){
			if (!perms.playerHas(player.getName(), world, "use", true)) {
				event.setCancelled(true);
				player.sendMessage(ChatColor.RED + "不要试图与领地内的村民交易，" + Residence.getLanguage().getPhrase("FlagDeny", "use"));
			}
		}else if((ClickedEntity instanceof Horse) || (ClickedEntity instanceof Pig)){
			if (!perms.playerHas(player.getName(), world, "container", true)) {
				event.setCancelled(true);
				player.sendMessage(ChatColor.RED + "不要试图骑乘领地内的猪或者马，" + Residence.getLanguage().getPhrase("FlagDeny", "container"));
			}
		}else if(ClickedEntity instanceof Minecart){
			if(ClickedEntity instanceof RideableMinecart){
				if (!perms.playerHas(player.getName(), world, "use", true)) {
					event.setCancelled(true);
					player.sendMessage(ChatColor.RED + "不要试图登上矿车，" + Residence.getLanguage().getPhrase("FlagDeny", "use"));
				}
			}else{
				if (!perms.playerHas(player.getName(), world, "use", true)) {
					event.setCancelled(true);
					player.sendMessage(ChatColor.RED + "不要试图打开领地内的矿车容器，" + Residence.getLanguage().getPhrase("FlagDeny", "use"));
				}
			}
		}else{
			return;
		}
	}
	
	//玩家与盔甲架交互事件
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
		Player player = event.getPlayer();
		if (Residence.isResAdminOn(player)) {
			return;
		}
		//检查是否是盔甲架
		Entity ClickEntity = event.getRightClicked();
		if (!(ClickEntity instanceof ArmorStand)) {
			return;
		}
		FlagPermissions perms = Residence.getPermsByLocForPlayer(ClickEntity.getLocation(), player);
		String world = player.getWorld().getName();
		if (!perms.playerHas(player.getName(), world, "container", true)) {
			event.setCancelled(true);
			player.sendMessage(ChatColor.RED + "不要试图使用领地内的盔甲架，" + Residence.getLanguage().getPhrase("FlagDeny", "container"));
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
		Player player = event.getPlayer();
		if (Residence.isResAdminOn(player)) {
			return;
		}
		String pname = player.getName();
		ClaimedResidence res = Residence.getResidenceManager().getByLoc(event.getBlockClicked().getLocation());
		if (res != null) {
			if (Residence.getConfigManager().preventRentModify() && Residence.getConfigManager().enabledRentSystem()) {
				if (Residence.getRentManager().isRented(res.getName())) {
					player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("RentedModifyDeny"));
					event.setCancelled(true);
					return;
				}
			}
		}
		FlagPermissions perms = Residence.getPermsByLocForPlayer(event.getBlockClicked().getLocation(), player);
		if (!perms.playerHas(pname, player.getWorld().getName(), "bucket", perms.playerHas(pname, player.getWorld().getName(), "build", true))) {
			player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagDeny", "bucket"));
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerBucketFill(PlayerBucketFillEvent event) {
		Player player = event.getPlayer();
		String pname = player.getName();
		if (Residence.isResAdminOn(player)) {
			return;
		}
		ClaimedResidence res = Residence.getResidenceManager().getByLoc(event.getBlockClicked().getLocation());
		if (res != null) {
			if (Residence.getConfigManager().preventRentModify() && Residence.getConfigManager().enabledRentSystem()) {
				if (Residence.getRentManager().isRented(res.getName())) {
					player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("RentedModifyDeny"));
					event.setCancelled(true);
					return;
				}
			}
		}
		FlagPermissions perms = Residence.getPermsByLocForPlayer(event.getBlockClicked().getLocation(), player);
		boolean hasbucket = perms.playerHas(pname, player.getWorld().getName(), "bucket", perms.playerHas(pname, player.getWorld().getName(), "build", true));
		if (!hasbucket) {
			player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagDeny", "bucket"));
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Location loc = event.getTo();
		Player player = event.getPlayer();

		if (Residence.isResAdminOn(player)) {
			handleNewLocation(player, loc, false);
			return;
		}

		ClaimedResidence res = Residence.getResidenceManager().getByLoc(loc);
		if (event.getCause() == TeleportCause.ENDER_PEARL) {
			if (res != null) {
				String areaname = Residence.getResidenceManager().getNameByLoc(loc);
				if (!res.getPermissions().playerHas(player.getName(), "move", true)) {
					event.setCancelled(true);
					player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ResidenceMoveDeny", areaname));
					return;
				}
			}
		}
		if (event.getCause() == TeleportCause.PLUGIN) {
			if (res != null) {
				String areaname = Residence.getResidenceManager().getNameByLoc(loc);
				if (!res.getPermissions().playerHas(player.getName(), "tp", true) && !player.hasPermission("residence.admin.tp")) {
					event.setCancelled(true);
					player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("TeleportDeny", areaname));
					return;
				}
			}
		}
		handleNewLocation(player, loc, false);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (player == null) {
			return;
		}
		long last = lastUpdate.get(player.getName());
		long now = System.currentTimeMillis();
		if (now - last < Residence.getConfigManager().getMinMoveUpdateInterval()) {
			return;
		}
		lastUpdate.put(player.getName(), now);
		if (event.getFrom().getWorld() == event.getTo().getWorld()) {
			if (event.getFrom().distance(event.getTo()) == 0) {
				return;
			}
		}
		handleNewLocation(player, event.getTo(), true);
	}

	public void handleNewLocation(Player player, Location loc, boolean move) {
		String pname = player.getName();

		ClaimedResidence res = Residence.getResidenceManager().getByLoc(loc);
		String areaname = null;
		boolean chatchange = false;
		String subzone = null;
		if (res != null) {
			areaname = Residence.getResidenceManager().getNameByLoc(loc);
			while (res.getSubzoneByLoc(player.getLocation()) != null) {
				subzone = res.getSubzoneNameByLoc(player.getLocation());
				res = res.getSubzoneByLoc(player.getLocation());
				areaname = areaname + "." + subzone;
			}
		}
		ClaimedResidence ResOld = null;
		if (currentRes.containsKey(pname)) {
			ResOld = Residence.getResidenceManager().getByName(currentRes.get(pname));
			if (ResOld == null) {
				currentRes.remove(pname);
			}
		}
		if (res == null) {
			lastOutsideLoc.put(pname, loc);
			if (ResOld != null) {
				String leave = ResOld.getLeaveMessage();

				// New ResidenceChangeEvent
				ResidenceChangedEvent chgEvent = new ResidenceChangedEvent(ResOld, null, player);
				Residence.getServ().getPluginManager().callEvent(chgEvent);

				if (leave != null && !leave.equals("")) {
					//ActionBar.send(player, ChatColor.BLUE + ChatColor.BOLD.toString() + this.insertMessages(player, ResOld.getName(), ResOld, leave));
					player.sendTitle("", ChatColor.BLUE + ChatColor.BOLD.toString() + this.insertMessages(player, ResOld.getName(), ResOld, leave), -1, -1, -1);
				}
				currentRes.remove(pname);
				Residence.getChatManager().removeFromChannel(pname);
			}
			return;
		}
		if (move) {
			if (!res.getPermissions().playerHas(pname, "move", true) && !Residence.isResAdminOn(player) && !player.hasPermission("residence.admin.move")) {
				Location lastLoc = lastOutsideLoc.get(pname);
				if (lastLoc != null) {
					player.teleport(lastLoc);
				} else {
					player.teleport(res.getOutsideFreeLoc(loc));
				}
				player.sendTitle("", ChatColor.RED+ ChatColor.BOLD.toString() + Residence.getLanguage().getPhrase("ResidenceMoveDeny", res.getName().split("\\.")[res.getName().split("\\.").length - 1]), -1, -1, -1);
				return;
			}
		}
		lastOutsideLoc.put(pname, loc);
		if (!currentRes.containsKey(pname) || ResOld != res) {
			currentRes.put(pname, areaname);
			if (subzone == null) {
				chatchange = true;
			}

			// "from" residence for ResidenceChangedEvent
			ClaimedResidence chgFrom = null;
			if (ResOld != res && ResOld != null) {
				String leave = ResOld.getLeaveMessage();
				chgFrom = ResOld;

				if (leave != null && !leave.equals("") && ResOld != res.getParent()) {
					player.sendTitle("", ChatColor.BLUE + ChatColor.BOLD.toString() + this.insertMessages(player, ResOld.getName(), ResOld, leave), -1, -1, -1);
				}
			}
			String enterMessage = res.getEnterMessage();

			// New ResidenceChangedEvent
			ResidenceChangedEvent chgEvent = new ResidenceChangedEvent(chgFrom, res, player);
			Residence.getServ().getPluginManager().callEvent(chgEvent);

			if (enterMessage != null && !enterMessage.equals("") && !(ResOld != null && res == ResOld.getParent())) {
				player.sendTitle("", ChatColor.BLUE + ChatColor.BOLD.toString() + this.insertMessages(player, areaname, res, enterMessage), -1, -1, -1);
			}
		}
		if (chatchange && chatenabled) {
			Residence.getChatManager().setChannel(pname, areaname);
		}
	}

	public String insertMessages(Player player, String areaname, ClaimedResidence res, String message) {
		try {
			message = message.replaceAll("%player", player.getName());
			message = message.replaceAll("%owner", res.getPermissions().getOwner());
			message = message.replaceAll("%residence", areaname);
		} catch (Exception ex) {
			return "";
		}
		return message;
	}

	public void doHeals() {
		try {
			for (Player player : Residence.getServ().getOnlinePlayers()) {
				String resname = Residence.getPlayerListener().getCurrentResidenceName(player.getName());
				ClaimedResidence res = null;
				if (resname != null) {
					res = Residence.getResidenceManager().getByName(resname);
				}
				if (res != null && res.getPermissions().has("healing", false)) {
					Damageable damage = player;
					double health = damage.getHealth();
					if (health < 20 && !player.isDead()) {
						player.setHealth(health + 1);
					}
				}
			}
		} catch (Exception ex) {
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		String pname = event.getPlayer().getName();
		if (chatenabled && playerToggleChat.contains(pname)) {
			String area = currentRes.get(pname);
			if (area != null) {
				ChatChannel channel = Residence.getChatManager().getChannel(area);
				if (channel != null) {
					channel.chat(pname, event.getMessage());
				}
				event.setCancelled(true);
			}
		}
	}

	public void tooglePlayerResidenceChat(Player player) {
		String pname = player.getName();
		if (playerToggleChat.contains(pname)) {
			playerToggleChat.remove(pname);
			player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("ResidenceChat", ChatColor.RED + "OFF" + ChatColor.YELLOW + "!"));
		} else {
			playerToggleChat.add(pname);
			player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("ResidenceChat", ChatColor.RED + "ON" + ChatColor.YELLOW + "!"));
		}
	}

	public String getCurrentResidenceName(String player) {
		return currentRes.get(player);
	}
}
