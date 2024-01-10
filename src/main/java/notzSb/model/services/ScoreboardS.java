package notzSb.model.services;

import me.clip.placeholderapi.PlaceholderAPI;
import notzSb.model.entities.PlayerM;
import notzSb.model.entities.enums.Staff;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static notzSb.Main.cf;
import static notzSb.model.entities.enums.Staff.*;
import static notzSb.utils.CorU.c;
import static notzSb.utils.ScoreboardU.*;

public interface ScoreboardS {

    void setScoreboard();
    List<String> getScoreLines();

    default Objective createObjective() {
        Scoreboard sc = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective ob = sc.registerNewObjective("Main", "yummy");

        ob.setDisplaySlot(DisplaySlot.SIDEBAR);
        ob.setDisplayName(c(cf.getConfig().getString("title")));
        return ob;
    }

    default boolean isStaffOnline(Staff staff) {
        switch (staff) {
            case MODERADOR:
                return !getAdmins().isEmpty() || !getGerentes().isEmpty();
            case ADMIN:
                return !getGerentes().isEmpty() || !getDiretores().isEmpty();
            case GERENTE:
            case DIRETOR:
                return !getStaffs().isEmpty();
            default:
                return !getAjudantes().isEmpty() || !getTrials().isEmpty();
        }
    }
    default int qttStaffOnline(Staff staff) {
        if (getStaffs().isEmpty()) return 0;

        switch (staff) {
            case AJUDANTE:
            default:
                return getAjudantes().size();
            case TRIAL:
                return getTrials().size();
            case MODERADOR:
                return getModeradores().size();
            case ADMIN:
                return getAdmins().size();
            case GERENTE:
                return getGerentes().size();
            case DIRETOR:
                return getDiretores().size();
        }
    }
    default int qttStaffOnline2(Staff staff) {
        if (getStaffs().isEmpty()) return 0;

        switch (staff) {
            case MODERADOR:
                return getAdmins().size() + getGerentes().size();
            case ADMIN:
                return getGerentes().size() + getDiretores().size();
            case GERENTE:
            case DIRETOR:
                return getStaffs().size();
            default:
                return getAjudantes().size() + getTrials().size();
        }
    }
    default List<Player> staffOnline(Staff staff) {
        if (getStaffs().isEmpty() || qttStaffOnline(staff) < 1)
            return Collections.emptyList();
        else return getStaff(staff).stream().map(PlayerM::getP).collect(Collectors.toList());
    }
    default List<Player> eachStaffOnline(Staff staff) {
        List<Player> ps = new ArrayList<>();

        switch (staff) {
            case AJUDANTE:
            default:
                ps.addAll(staffOnline(AJUDANTE));
                ps.addAll(staffOnline(TRIAL));
                break;
            case TRIAL:
                ps.addAll(staffOnline(AJUDANTE));
                ps.addAll(staffOnline(TRIAL));
                ps.addAll(staffOnline(MODERADOR));
                break;
            case MODERADOR:
                ps.addAll(staffOnline(ADMIN));
                ps.addAll(staffOnline(GERENTE));
                break;
            case ADMIN:
                ps.addAll(staffOnline(GERENTE));
                ps.addAll(staffOnline(DIRETOR));
                break;
            case DIRETOR:
            case GERENTE:
                ps.addAll(staffOnline(AJUDANTE));
                ps.addAll(staffOnline(TRIAL));
                ps.addAll(staffOnline(MODERADOR));
                ps.addAll(staffOnline(ADMIN));
                ps.addAll(staffOnline(GERENTE));
                ps.addAll(staffOnline(DIRETOR));
                break;
        }
        return ps;
    }
    default String placeholder(Player p, String placeholder) {

        if (placeholder.contains("{") && placeholder.contains("}") && placeholder.indexOf("{") < placeholder.indexOf("}")) {
            String ph = placeholder.substring(placeholder.indexOf("{"), placeholder.indexOf("}") + 1);

            if (getStaffs().containsKey(p.getName()))
                placeholder = placeholder.replace(ph, placeholderStaff(getStaffs().get(p.getName()), ph));
            else placeholder = placeholder.replace(ph, placeholderPlayer(p, ph));
        }
        return placeholder;
    }
    default String placeholderPlayer(Player p, String placeholder) {
        Staff sf = AJUDANTE;

        switch (placeholder) {
            case "{player_name}":
                placeholder = placeholder.replace(placeholder, p.getName());
                break;
            case "{player_displayname}":
                placeholder = placeholder.replace(placeholder, p.getDisplayName());
                break;
            case "{rank}":
                placeholder = placeholder.replace(placeholder, "rank");
                break;
            case "{status_rankup}":
                placeholder = placeholder.replace(placeholder, "status_rankup");
                break;
            case "{cash}":
                placeholder = placeholder.replace(placeholder, "criar cash");
                break;
            case "{staff}":
                if (!eachStaffOnline(sf).isEmpty()) {
                    placeholder = placeholder.replace(placeholder, eachStaffOnline(sf).get(new Random().nextInt(eachStaffOnline(sf).size())).getDisplayName());
                } else placeholder = placeholder.replace(placeholder, "Offline");
        }
        return placeholder;
    }

    default String placeholderStaff(PlayerM pm, String placeholder) {
        Player p = pm.getP();

        Staff sf = pm.getScoreboard();

        switch (placeholder) {
            case "{player_name}":
            case "{player_displayname}":
            case "{rank}":
            case "{status_rankup}":
            case "{cash}":
            case "{staff}":
                placeholder = placeholderPlayer(p, placeholder);
                break;
            case "{staff_ajd}":
                if (!staffOnline(AJUDANTE).isEmpty())
                    placeholder = placeholder.replace(placeholder, staffOnline(AJUDANTE).get(new Random().nextInt(staffOnline(AJUDANTE).size())).getDisplayName());
                else placeholder = placeholder.replace(placeholder, "Offline");
                break;
            case "{staff_trial}":
                if (!staffOnline(TRIAL).isEmpty())
                    placeholder = placeholder.replace(placeholder, getTrials().get(new Random().nextInt(getTrials().size())).getP().getDisplayName());
                else placeholder = placeholder.replace(placeholder, "Offline");
                break;
            case "{staff_mod}":
                if (!staffOnline(MODERADOR).isEmpty())
                    placeholder = placeholder.replace(placeholder, getModeradores().get(new Random().nextInt(getModeradores().size())).getP().getDisplayName());
                else placeholder = placeholder.replace(placeholder, "Offline");
                break;
            case "{staff_admin}":
                if (!staffOnline(ADMIN).isEmpty())
                    placeholder = placeholder.replace(placeholder, getAdmins().get(new Random().nextInt(getAdmins().size())).getP().getDisplayName());
                else placeholder = placeholder.replace(placeholder, "Offline");
                break;
            case "{staff_list}":
                placeholder = placeholder.replace(placeholder, String.valueOf(qttStaffOnline2(sf)));
                break;
            case "{player_list}":
                placeholder = placeholder.replace(placeholder, String.valueOf(getPlayers().size()));
                break;
            case "{ajd_list}":
                placeholder = placeholder.replace(placeholder, String.valueOf(getAjudantes().size()));
                break;
            case "{trial_list}":
                placeholder = placeholder.replace(placeholder, String.valueOf(getTrials().size()));
                break;
            case "{mod_list}":
                placeholder = placeholder.replace(placeholder, String.valueOf(getModeradores().size()));
                break;
            case "{adm_list}":
                placeholder = placeholder.replace(placeholder, String.valueOf(getAdmins().size()));
                break;
            case "{gerente_list}":
                placeholder = placeholder.replace(placeholder, String.valueOf(getGerentes().size()));
                break;
            case "{tps}":
                placeholder = placeholder.replace(placeholder, PlaceholderAPI.setPlaceholders(p, "%server_tps_1%") + "&2 ms");
                break;
            case "{ping}":
                placeholder = placeholder.replace(placeholder, PlaceholderAPI.setPlaceholders(p, "%player_ping%") + "&2 ms");
                //placeholder = placeholder.replace(placeholder, String.valueOf(getPlayerPing(p)));
        }
        return placeholder;
    }
}
