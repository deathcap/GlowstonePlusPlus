package net.glowstone.command.minecraft;

import net.glowstone.command.CommandTarget;
import net.glowstone.command.CommandUtils;
import net.glowstone.constants.ItemIds;
import net.glowstone.util.InventoryUtil;
import net.glowstone.util.lang.I;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.VanillaCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ClearCommand extends VanillaCommand {
    public ClearCommand() {
        super("clear", I.tr("command.minecraft.clear.description"), I.tr("command.minecraft.clear.usage.1"), Collections.emptyList());
        setPermission("minecraft.command.clear");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!testPermission(sender)) {
            return false;
        }
        if (args.length == 0) {
            if ((sender instanceof Player)) {
                Player player = (Player) sender;
                return clearAll(sender, player, null, -1, -1);
            } else {
                sender.sendMessage(I.tr(sender, "command.generic.usage", I.tr(sender, "command.minecraft.clear.usage.2")));
                return false;
            }
        }
        String name = args[0];
        List<Player> players = new ArrayList<>();
        if (name.startsWith("@") && name.length() >= 2 && CommandUtils.isPhysical(sender)) {
            Location location = sender instanceof Entity ? ((Entity) sender).getLocation() : ((BlockCommandSender) sender).getBlock().getLocation();
            CommandTarget target = new CommandTarget(sender, name);
            Entity[] matched = target.getMatched(location);
            for (Entity entity : matched) {
                if (entity instanceof Player) {
                    players.add((Player) entity);
                }
            }
        } else {
            Player player = Bukkit.getPlayerExact(name);
            if (player == null) {
                sender.sendMessage(I.tr(sender, "command.generic.player.missing", name));
                return false;
            } else {
                players.add(player);
            }
        }
        if (players.size() == 0) {
            sender.sendMessage(I.tr(sender, "command.generic.player.missing", name));
            return false;
        }
        if (args.length >= 2) {
            String itemName = args[1];
            if (!itemName.startsWith("minecraft:")) {
                itemName = "minecraft:" + itemName;
            }
            Material type = ItemIds.getItem(itemName);
            if (type == null) {
                sender.sendMessage(I.tr(sender, "command.minecraft.clear.item.missing", itemName));
                return false;
            }
            if (args.length >= 3) {
                String dataString = args[2];
                int data;
                try {
                    data = Integer.valueOf(dataString);
                } catch (NumberFormatException ex) {
                    sender.sendMessage(I.tr(sender, "command.generic.nan", dataString));
                    return false;
                }
                if (data < -1) {
                    sender.sendMessage(I.tr(sender, "command.minecraft.clear.item.toosmall", data));
                    return false;
                }
                if (args.length >= 4) {
                    String amountString = args[3];
                    int amount;
                    try {
                        amount = Integer.valueOf(amountString);
                    } catch (NumberFormatException ex) {
                        sender.sendMessage(I.tr(sender, "command.generic.nan", amountString));
                        return false;
                    }
                    if (amount < -1) {
                        sender.sendMessage(I.tr(sender, "command.minecraft.command.clear.item.toosmall", amount));
                        return false;
                    }
                    if (args.length >= 5) {
                        sender.sendMessage(ChatColor.RED + "Sorry, item data-tags are not supported yet.");
                        return false;
                    } else {
                        boolean success = true;
                        for (Player player : players) {
                            if (!clearAll(sender, player, type, data, amount)) {
                                success = false;
                            }
                        }
                        return success;
                    }
                } else {
                    boolean success = true;
                    for (Player player : players) {
                        if (!clearAll(sender, player, type, data, -1)) {
                            success = false;
                        }
                    }
                    return success;
                }
            } else {
                boolean success = true;
                for (Player player : players) {
                    if (!clearAll(sender, player, type, -1, -1)) {
                        success = false;
                    }
                }
                return success;
            }
        } else {
            boolean success = true;
            for (Player player : players) {
                if (!clearAll(sender, player, null, -1, -1)) {
                    success = false;
                }
            }
            return success;
        }
    }

    private int countAllItems(Inventory inventory, Material material, int data, int maxCount) {
        if (material == null) {
            return Arrays.stream(inventory.getContents()).filter(stack -> !InventoryUtil.isEmpty(stack)).mapToInt(ItemStack::getAmount).sum();
        }
        int count = 0;
        for (ItemStack stack : inventory.getContents()) {
            if (stack.getType() == material && (data == -1 || data == stack.getData().getData()) && (maxCount == -1 || maxCount == 0 || count < maxCount)) {
                if (maxCount == -1 || maxCount == 0) {
                    count += stack.getAmount();
                } else {
                    for (int i = 0; i < stack.getAmount(); i++) {
                        if (count < maxCount) {
                            count++;
                        } else {
                            return count;
                        }
                    }
                }
            }
        }
        return count;
    }

    private boolean clearAll(CommandSender sender, Player player, Material material, int data, int maxCount) {
        int count = countAllItems(player.getInventory(), material, data, maxCount);
        if (count == 0 && maxCount != 0) {
            sender.sendMessage(I.tr(sender, "command.minecraft.clear.failed", player.getName()));
            return false;
        } else {
            if (material == null) {
                player.getInventory().clear();
            } else {
                int remaining = maxCount;
                for (ItemStack stack : player.getInventory().getContents()) {
                    if (stack.getType() == material && (data == -1 || data == stack.getData().getData())) {
                        // matches type and data
                        if (maxCount == -1) {
                            player.getInventory().remove(stack);
                        } else if (maxCount == 0) {
                            sender.sendMessage(I.tr(sender, "command.minecraft.clear.match", player.getName(), count));
                            return true;
                        } else {
                            for (int i = 0; i < stack.getAmount(); i++) {
                                if (remaining > 0) {
                                    stack.setAmount(stack.getAmount() - 1);
                                    remaining--;
                                }
                            }
                        }
                    }
                }
            }
            sender.sendMessage(I.tr(sender, "command.minecraft.clear.done", player.getName(), count));
            return true;
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        if (args.length == 1) {
            return super.tabComplete(sender, alias, args);
        }
        if (args.length == 2) {
            String start = args[1];
            if (!"minecraft:".startsWith(start)) {
                int colon = start.indexOf(':');
                start = "minecraft:" + start.substring(colon == -1 ? 0 : (colon + 1));
            }
            return (List) StringUtil.copyPartialMatches(start, ItemIds.getIds(), new ArrayList(ItemIds.getIds().size()));
        }
        return Collections.emptyList();
    }
}
