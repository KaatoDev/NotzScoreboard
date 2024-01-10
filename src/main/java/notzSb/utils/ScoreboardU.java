package notzSb.utils;

import notzSb.model.entities.PlayerM;
import notzSb.model.entities.enums.Staff;
import notzSb.model.services.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static notzSb.Main.stf;
import static notzSb.utils.CorU.c;

public class ScoreboardU extends DM implements Runnable {
    private static boolean change;
    private static HashMap<String, PlayerM> staffs;
    private static HashMap<Staff, List<PlayerM>> cargos;
    private static List<String> allStaffs;
    private static List<Player> players = new ArrayList<>(), debug = new ArrayList<>();
    private final static ScoreboardU instance = new ScoreboardU();

    public static HashMap<String, PlayerM> getStaffs() {
        return staffs;
    }
    public static List<String> getAllStaffs() {
        return allStaffs;
    }
    public static List<Player> getPlayers() {
        return players;
    }

    @Override
    public void run() {
        if (!players.isEmpty())
            players.forEach(p -> new SB_Player(p).setScoreboard());

        if (!staffs.isEmpty())
            staffs.values().forEach(ScoreboardU::setScore);

        if (!debug.isEmpty()) {
            debug.forEach(pp -> {
                pp.sendMessage(c("&5----------"));
                pp.sendMessage(c("&6Staffs: "));
                staffs.keySet().forEach(pp::sendMessage);
                pp.sendMessage(c("\n&6AllStaffs: "));
                allStaffs.forEach(pp::sendMessage);
                pp.sendMessage(c("\n&6Players: "));
                players.forEach(p -> pp.sendMessage(p.getName()));
                pp.sendMessage(c("&5----------"));
            });
        }
    }

    public static void loadPlayers() {
        cargos.replace(Staff.AJUDANTE, new ArrayList<>());
        cargos.replace(Staff.TRIAL, new ArrayList<>());
        cargos.replace(Staff.MODERADOR, new ArrayList<>());
        cargos.replace(Staff.ADMIN, new ArrayList<>());
        cargos.replace(Staff.GERENTE, new ArrayList<>());
        cargos.replace(Staff.DIRETOR, new ArrayList<>());

        Bukkit.getOnlinePlayers().stream().filter(p -> allStaffs.contains(p.getName())).forEach(p -> staffs.put(p.getName(), find(p)));

        staffs.values().forEach(pm -> cargos.get(pm.getCargo()).add(pm));

        players = Bukkit.getOnlinePlayers().stream().filter(p -> p != null && !staffs.containsKey(p.getName())).collect(Collectors.toList());
    }

    public static void checkEntry(Player p) {
        if (allStaffs.contains(p.getName())) {
            staffs.put(p.getName(), find(p));
            cargos.get(staffs.get(p.getName()).getCargo()).add(staffs.get(p.getName()));

            if (staffs.get(p.getName()).getCargo() == Staff.AJUDANTE && getAjudantes().size() == 1)
                change = true;

        } else
            players.add(p);
    }
    public static void checkLeave(Player p) {
        if (players.contains(p))
            players.remove(p);
        else {
            cargos.get(staffs.get(p.getName()).getCargo()).remove(staffs.get(p.getName()));

            if (staffs.get(p.getName()).getCargo() == Staff.AJUDANTE && getAjudantes().isEmpty())
                change = true;

            staffs.remove(p.getName());
        }
    }

    public static void start() {
        staffs = new HashMap<>();
        cargos = new HashMap<>();

        cargos.put(Staff.AJUDANTE, new ArrayList<>());
        cargos.put(Staff.TRIAL, new ArrayList<>());
        cargos.put(Staff.MODERADOR, new ArrayList<>());
        cargos.put(Staff.ADMIN, new ArrayList<>());
        cargos.put(Staff.GERENTE, new ArrayList<>());
        cargos.put(Staff.DIRETOR, new ArrayList<>());

        initialize();
        change = true;

        HashMap<String, String[]> cc = getDbPlayers();
        allStaffs = new ArrayList<>(cc.keySet());

        allStaffs.forEach(p -> {
            stf.getConfig().set(p + ".cargo", cc.get(p)[0]);
            stf.getConfig().set(p + ".scoreboard", cc.get(p)[1]);
//            stf.saveConfig();
        });

        stf.getConfig().set("staffs", allStaffs);
        stf.saveConfig();

        Bukkit.getOnlinePlayers().stream().filter(p -> allStaffs.contains(p.getName())).forEach(ScoreboardU::checkEntry);

        players = Bukkit.getOnlinePlayers().stream().filter(p -> p != null && !allStaffs.contains(p.getName())).collect(Collectors.toList());
    }

    public static void updateCargo(Player p, Staff old, Staff cargo) {
        cargos.get(old).remove(staffs.get(p.getName()));
        cargos.get(cargo).add(staffs.get(p.getName()));

        stf.getConfig().set(p.getName() + ".cargo", cargo.name());
        stf.getConfig().set(p.getName() + ".scoreboard", cargo.name());
        stf.saveConfig();
    }

    public static boolean removePlayer(Player p) {
        if (remove(p)) {
            checkLeave(p);

            allStaffs.remove(p.getName());

            stf.getConfig().set(p.getName(), "");

            stf.getConfig().set("staffs", allStaffs);
            stf.saveConfig();

            checkEntry(p);
            return true;
        } else return false;
    }

    public static boolean addPlayer(PlayerM pm) {
        if (add(pm)) {
            checkLeave(pm.getP());

            allStaffs.add(pm.getP().getName());

            stf.getConfig().set(pm.getP().getName() + ".cargo", pm.getCargo().name());
            stf.getConfig().set(pm.getP().getName() + ".scoreboard", pm.getScoreboard().name());

            stf.getConfig().set("staffs", allStaffs);
            stf.saveConfig();

            players.remove(pm.getP());

            checkEntry(pm.getP());

            return true;
        } return false;
    }

    public static boolean setDebugPlayer(Player p) {
        if (debug.contains(p)) {
            debug.remove(p);
            return false;
        } else {
            debug.add(p);
            return true;
        }
    }

    public static List<PlayerM> getAjudantes() {
        return cargos.get(Staff.AJUDANTE);
    }
    public static List<PlayerM> getTrials() {
        return cargos.get(Staff.TRIAL);
    }
    public static List<PlayerM> getModeradores() {
        return cargos.get(Staff.MODERADOR);
    }
    public static List<PlayerM> getAdmins() {
        return cargos.get(Staff.ADMIN);
    }
    public static List<PlayerM> getGerentes() {
        return cargos.get(Staff.GERENTE);
    }
    public static List<PlayerM> getDiretores() {
        return cargos.get(Staff.DIRETOR);
    }

    public static List<PlayerM> getStaff(Staff staff) {
        switch (staff) {
            case AJUDANTE:
                return getAjudantes();
            case TRIAL:
                return getTrials();
            case MODERADOR:
                return getModeradores();
            case ADMIN:
                return getAdmins();
            case GERENTE:
                return getGerentes();
            case DIRETOR:
                return getDiretores();
            default: return Collections.emptyList();
        }
    }

    private static void setScore(PlayerM pm) {
        switch (pm.getScoreboard()) {
            case AJUDANTE:
                new SB_Ajudante(pm.getP()).setScoreboard();
                return;
            case TRIAL:
                new SB_Trial(pm.getP()).setScoreboard();
                return;
            case MODERADOR:
                new SB_Moderador(pm.getP()).setScoreboard();
                return;
            case ADMIN:
                new SB_Admin(pm.getP()).setScoreboard();
                return;
            case GERENTE:
                new SB_Gerente(pm.getP()).setScoreboard();
                return;
            case DIRETOR:
                new SB_Diretor(pm.getP()).setScoreboard();
                return;
            case PLAYER:
                new SB_Player(pm.getP()).setScoreboard();
        }
    }

    public static boolean getChange() {
        return change;
    }
    public static void setChange(boolean chang) {
        change = chang;
    }

    public static void resetPlugin() {
        resetDatabase();
    }

    public static ScoreboardU getInstance() {
        start();
        return instance;
    }
}
