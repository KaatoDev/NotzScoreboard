package notzSb.model.services;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.List;

import static notzSb.Main.cf;
import static notzSb.Main.sf;
import static notzSb.utils.CorU.c;

public class SB_Diretor implements ScoreboardS {
    private final Player p;
    private final List<String> sc;
    private final String stf;

    public SB_Diretor(Player player) {
        stf = "Diretor";
        p = player;
        sc = getScoreLines();
    }

    public void setScoreboard() {
        if (p.getScoreboard() != null && p.getScoreboard().getObjective(stf) != null)
            update2();
        else p.setScoreboard(create());
        //p.setScoreboard(update());
    }

    private Scoreboard create() {
        Scoreboard score = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective ob = score.registerNewObjective(stf, "yummy");
        ob.setDisplaySlot(DisplaySlot.SIDEBAR);
        ob.setDisplayName(c(cf.getConfig().getString("title")));

        int i = sc.size()-1;
        int count=0;
        for (String s : sc) {
            if (s.contains("{") && s.contains("}")) {
                Team team1 = score.registerNewTeam("team" + count++);

                String pre = s.substring(0, s.indexOf("{"));
                String suf = placeholder(p, s.substring(s.indexOf("{")));

                team1.addEntry(c(pre));
                team1.setSuffix(c(suf));

                ob.getScore(c(pre)).setScore(i);
            } else ob.getScore(c(s)).setScore(i);
            i--;
        }
        return score;
    }

    private Scoreboard update() {
        Scoreboard score = p.getScoreboard();
        int count=0;

        for (String s : sc) {
            if (s.contains("{") && s.contains("}")) {
                Team team1 = score.getTeam("team" + count++);

                String suf = placeholder(p, s.substring(s.indexOf("{")));

                team1.setSuffix(c(suf));

            }
        }
        return score;
    }

    private void update2() {
        int count=0;

        for (String s : sc)
            if (s.contains("{") && s.contains("}"))
                p.getScoreboard().getTeam("team" + count++).setSuffix(c(placeholder(p, s.substring(s.indexOf("{")))));
    }

    public List<String> getScoreLines() {
        List<String> sc = sf.getConfig().getStringList("staff.scoreboard");
        sc.addAll(sf.getConfig().getStringList("staff_sb.diretor"));
        sc = PlaceholderAPI.setPlaceholders(p, sc);

        final String[] y = {" "};
        sc.replaceAll(x -> {
            if (x.equals("")) {
                String z = y[0];
                y[0] += " ";
                return z;
            } else {
                return x;
            }
        });

        return sc;
        //return sc.stream().map(s -> c(placeholder(p, s))).collect(Collectors.toList());
    }
}
