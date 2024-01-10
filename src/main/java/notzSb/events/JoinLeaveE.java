package notzSb.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import static notzSb.utils.ScoreboardU.checkEntry;
import static notzSb.utils.ScoreboardU.checkLeave;

public class JoinLeaveE implements Listener {
    @EventHandler
    public void onJoinEvent(PlayerJoinEvent e) {
        checkEntry(e.getPlayer());
    }

    @EventHandler
    public void onLeaveEvent(PlayerQuitEvent e) {
        checkLeave(e.getPlayer());
    }
}
