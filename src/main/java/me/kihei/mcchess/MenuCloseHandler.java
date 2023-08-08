package me.kihei.mcchess;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;


public class MenuCloseHandler implements Listener {

    private MCChess mcc;

    public MenuCloseHandler(MCChess mcc) {
        this.mcc = mcc;
    }

    @EventHandler
    public void onMenuClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();

        Bukkit.getScheduler().runTaskLater(mcc, new Runnable() {
            @Override
            public void run() {
                if (e.getView().getTitle().equalsIgnoreCase("Select a promotion piece:")) {
                    if (!MCChess.promoteDone) {
                        p.openInventory(MCChess.gui);
                    } else {
                        MCChess.promoteDone = false;
                    }
                }
            }
        }, 1L);
    }
}
