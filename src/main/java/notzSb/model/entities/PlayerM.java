package notzSb.model.entities;

import notzSb.model.entities.enums.Staff;
import notzSb.utils.DM;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import static notzSb.Main.stf;
import static notzSb.utils.ScoreboardU.updateCargo;

public class PlayerM extends DM implements Serializable {
    private static final long serialVersionUID = 1L;
    private final UUID uuid;
    private Staff cargo, scoreboard;

    public PlayerM(Player player, Staff cargo, Staff scoreboard) {
        this.uuid = player.getUniqueId();
        this.cargo = cargo;
        this.scoreboard = scoreboard;
    }

    public Player getP() {
        return Bukkit.getPlayer(uuid);
    }

    public Staff getCargo() {
        return cargo;
    }

    public Staff getScoreboard() {
        return scoreboard;
    }

    public void setCargo(Staff cargo) {
        alterCargo(getP(), cargo);
        alterScoreboard(getP(), cargo);

        updateCargo(getP(), this.cargo, cargo);

        this.cargo = cargo;
        scoreboard = cargo;
    }

    public void setScoreboard(Staff scoreboard) {
        this.scoreboard = scoreboard;

        alterScoreboard(getP(), scoreboard);
        stf.getConfig().set(getP().getName() + ".scoreboard", scoreboard.name());
        stf.saveConfig();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerM)) return false;
        PlayerM playerM = (PlayerM) o;
        return Objects.equals(uuid, playerM.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
