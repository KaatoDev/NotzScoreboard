package notzSb.commands;

import notzSb.model.entities.PlayerM;
import notzSb.model.entities.enums.Staff;
import notzSb.utils.ScoreboardU;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static notzSb.Main.*;
import static notzSb.utils.CorU.c;
import static notzSb.utils.CorU.send;
import static notzSb.utils.ScoreboardU.*;

public class ScoreboardC implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!start) return false;

        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (!p.hasPermission("notzsb.admin")) {
                send(p, "&cVocê não tem permissão para utilizar este comando!");
                return true;
            }

            switch (args.length) {
                case 1:
                    switch (args[0].toLowerCase()) {
                        case "debug":
                            if (setDebugPlayer(p))
                                send(p, "&eModo debug &aativado! &e- cuidado");
                            else send(p, "&eModo debug &cdesativado!");
                            break;
                        case "help":
                            p.sendMessage(c("\n    --&6[&fNotzScoreboard&6]&f--"));
                            p.sendMessage(c("&f/&bnotzsb &badd &f(&bcargo&f) &f(&bplayer&f)"));
                            p.sendMessage(c("&f/&bnotzsb &bremove &f(&bplayer&f)"));
                            p.sendMessage(c("&f/&bnotzsb &bsetRole &f(&bcargo&f) &7-[Altera seu próprio cargo]"));
                            p.sendMessage(c("&f/&bnotzsb &bsetRole &f(&bcargo&f) &f(&bplayer&f) &7-[Altera o cargo de outrem]"));
                            p.sendMessage(c("&f/&bnotzsb &bsetScore &f(&bcargo&f) &7-[Altera sua própria scoreboard]"));
                            p.sendMessage(c("&f/&bnotzsb &bsetScore &f(&bcargo&f) &f(&bplayer&f) &7-[Altera a scoreboard de outrem]"));
                            p.sendMessage("");
                            break;
                        case "list":
                            p.sendMessage(c("\n    --&6[&fNotzScoreboard&6]&f--"));
                            p.sendMessage(c("&6Staffs:"));
                            if (!getStaffs().isEmpty())
                                getStaffs().values().forEach(pm -> p.sendMessage(c("&e- &f" + pm.getP().getName() + " | &erole: &f" + pm.getCargo().name().toLowerCase() + " | &esb: &f" + pm.getScoreboard().name().toLowerCase())));
                            p.sendMessage(c("&6AllStaffs:"));
                            if (!getAllStaffs().isEmpty())
                                getAllStaffs().forEach(pp -> {
                                    if (getStaffs().containsKey(pp))
                                        p.sendMessage(c("&e- &f" + pp + " | &erole: &f" + getStaffs().get(pp).getCargo().name().toLowerCase() + " | &esb: &f" + getStaffs().get(pp).getScoreboard().name().toLowerCase()));
                                    else p.sendMessage(c("&e- &f" + pp + " | &cOffline"));
                                });
                            p.sendMessage("");
                            break;
                        case "reload":
                            cf.reloadConfig();
                            sf.reloadConfig();
                            ScoreboardU.loadPlayers();
                            send(p, "&eConfigurações do plugin reiniciadas.");
                            break;
                        case "reset":
                            resetPlugin();
                            start();
                            send(p, "&eDatabase resetada com sucesso!");
                            break;
                        default:
                            el(p, args);
                    }
                    break;
                case 2:
                    boolean aff = true;
                    try {
                        Staff.valueOf(args[1].toUpperCase());
                    } catch (Exception e) {
                        aff = false;
                    }

                    if (aff && args[0].equalsIgnoreCase("setrole") && (args[1].equalsIgnoreCase("player") || Arrays.stream(Staff.values()).collect(Collectors.toList()).contains(Staff.valueOf(args[1].toUpperCase()))))
                        setRole(p, args[1]);
                    else if (aff && args[0].equalsIgnoreCase("setscore") && (args[1].equalsIgnoreCase("player") || Arrays.stream(Staff.values()).collect(Collectors.toList()).contains(Staff.valueOf(args[1].toUpperCase()))))
                        setScoreboard(p, args[1]);
                    else if (args[0].equalsIgnoreCase("remove") && Bukkit.getPlayerExact(args[1]) != null) {
                        Player pp = Bukkit.getPlayerExact(args[1]);
                        if (getAllStaffs().contains(pp.getName())) {
                            if (removePlayer(pp))
                                send(p, "&ePlayer &f" + args[1] + " &eremovido com sucesso.");
                            else send(p, "&cNão foi possível remover o player &f" + args[1] + "&c.");
                        } else send(p, "&cO player &f" + args[1] + " &cnão está registrado.");
                    } else el(p, args);

                    break;
                case 3:
                    try {
                        Staff.valueOf(args[1].toUpperCase());
                    } catch (Exception e) {
                        el(p, args);
                        return true;
                    }
                    if (Bukkit.getPlayerExact(args[2]) == null)
                        send(p, "&cEste player não existe.");
                    else {
                        if (args[0].equalsIgnoreCase("setrole")
                                && Arrays.stream(Staff.values()).collect(Collectors.toList()).contains(Staff.valueOf(args[1].toUpperCase())))
                            setRole(Bukkit.getPlayerExact(args[2]), args[1]);
                        else if (args[0].equalsIgnoreCase("setscore")
                                && (args[1].equalsIgnoreCase("player") || Arrays.stream(Staff.values()).collect(Collectors.toList()).contains(Staff.valueOf(args[1].toUpperCase()))))
                            setScoreboard(Bukkit.getPlayerExact(args[2]), args[1]);
                        else if (args[0].equalsIgnoreCase("add")
                                && Arrays.stream(Staff.values()).collect(Collectors.toList()).contains(Staff.valueOf(args[1].toUpperCase()))) {
                            if (getStaffs().get(args[2]) != null)
                                send(p, "&eO player &f" + args[2] + " &ejá está registrado!");
                            else if (addPlayer(new PlayerM(Bukkit.getPlayerExact(args[2]), Staff.valueOf(args[1].toUpperCase()), Staff.valueOf(args[1].toUpperCase()))))
                                send(p, "&aPlayer &f" + args[2] + " &aadicionado com sucesso.");
                            else send(p, "&cFalha ao adicionar o player &f" + args[2] + "&c.");
                        } else el(p, args);
                    }

                    break;
                default:
                    send(p, "&eUtilize &f/&bnotzsb help");
            }
            return true;
        }
        return false;
    }

    private void el(Player p, String[] args) {
        if (args.length > 0)
            switch (args[0].toLowerCase()) {
                case "add":
                    send(p, "&eUtilize &f/&bnotzsb &badd &f(&bcargo&f) &f(&bplayer&f)");
                    break;
                case "remove":
                    send(p, "&eUtilize &f/&bnotzsb &bremove &f(&bplayer&f)");
                    break;
                case "setrole":
                    p.sendMessage("");
                    send(p, "&f/&bnotzsb &bsetRole &f(&bcargo&f) &7-[Altera seu próprio cargo]");
                    send(p, "&f/&bnotzsb &bsetRole &f(&bcargo&f) &f(&bplayer&f) &7-[Altera o cargo de outrem]");
                    p.sendMessage("");
                    break;
                case "setscore":
                    p.sendMessage("");
                    send(p, "&f/&bnotzsb &bsetScore &f(&bcargo&f) &7-[Altera sua própria scoreboard]");
                    send(p, "&f/&bnotzsb &bsetScore &f(&bcargo&f) &f(&bplayer&f) &7-[Altera a scoreboard de outrem]");
                    p.sendMessage("");
                    break;
                default:
                    send(p, "&eUtilize &f/&bnotzsb help");
            }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        switch (args.length) {
            case 1:
                return Arrays.asList("add", "help", "remove", "setRole", "setScore", "reload");
            case 2:
                if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("setrole") || args[0].equalsIgnoreCase("setscore"))
                    return Arrays.asList("player", "ajudante", "trial", "moderador", "admin", "gerente", "diretor");
                if (args[0].equalsIgnoreCase("remove")) {
                    List<String> staffs = new ArrayList<>(getStaffs().keySet());
                    if (staffs.isEmpty())
                        return Collections.singletonList("(no staff set)");
                    else return staffs;
                }
                break;
            case 3:
                if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("setrole")|| args[0].equalsIgnoreCase("setscore"))
                    return new ArrayList<>(getStaffs().keySet());
                break;
            default:
                return Collections.emptyList();
        }
        return Collections.emptyList();
    }

    private void setRole(Player p, String arg) {
        String error = prefix + c("&cNão foi possível alterar o cargo do player [&e" + p.getName() + "&c]!");

        switch (arg.toLowerCase()) {
            case "ajudante":
                getStaffs().get(p.getName()).setCargo(Staff.AJUDANTE);
                send(p, "&eCargo &fsetado para &eAjudante &fcom sucesso! (" + p.getName() + ").");
                break;
            case "trial":
                getStaffs().get(p.getName()).setCargo(Staff.TRIAL);
                send(p, "&dCargo &fsetado para &dTrial &fcom sucesso! (" + p.getName() + ").");
                break;
            case "moderador":
                getStaffs().get(p.getName()).setCargo(Staff.MODERADOR);
                send(p, "&2Scoreboard &fsetado para &2Moderador &fcom sucesso! (" + p.getName() + ").");
                break;
            case "admin":
                getStaffs().get(p.getName()).setCargo(Staff.ADMIN);
                send(p, "&cCargo &fsetado para &cAdmin &fcom sucesso! (" + p.getName() + ").");
                break;
            case "gerente":
                getStaffs().get(p.getName()).setCargo(Staff.GERENTE);
                send(p, "&4Cargo &fsetado para &4Gerente &fcom sucesso! (" + p.getName() + ").");
                break;
            case "diretor":
                getStaffs().get(p.getName()).setCargo(Staff.DIRETOR);
                send(p, "&9Cargo &fsetado para &9Diretor &fcom sucesso! (" + p.getName() + ").");
                break;
        }
    }
    private void setScoreboard(Player p, String arg) {
        String error = prefix + c("&cNão foi possível alterar a scoreboard do player [&e" + p.getName() + "&c]!");
        switch (arg.toLowerCase()) {
            case "player":
                getStaffs().get(p.getName()).setScoreboard(Staff.PLAYER);
                send(p, "&6Scoreboard &fsetada para &6Player &fcom sucesso! (" + p.getName() + ").");
                break;
            case "ajudante":
                getStaffs().get(p.getName()).setScoreboard(Staff.AJUDANTE);
                send(p, "&eScoreboard &fsetada para &eAjudante &fcom sucesso! (" + p.getName() + ").");
                break;
            case "trial":
                getStaffs().get(p.getName()).setScoreboard(Staff.TRIAL);
                send(p, "&dScoreboard &fsetada para &dTrial &fcom sucesso! (" + p.getName() + ").");
                break;
            case "moderador":
                getStaffs().get(p.getName()).setScoreboard(Staff.MODERADOR);
                send(p, "&2Scoreboard &fsetada para &2Moderador &fcom sucesso! (" + p.getName() + ").");
                break;
            case "admin":
                getStaffs().get(p.getName()).setScoreboard(Staff.ADMIN);
                send(p, "&cScoreboard &fsetada para &cAdmin &fcom sucesso! (" + p.getName() + ").");
                break;
            case "gerente":
                getStaffs().get(p.getName()).setScoreboard(Staff.GERENTE);
                send(p, "&4Scoreboard &fsetada para &4Gerente &fcom sucesso! (" + p.getName() + ").");
                break;
            case "diretor":
                getStaffs().get(p.getName()).setScoreboard(Staff.DIRETOR);
                send(p, "&9Scoreboard &fsetada para &9Diretor &fcom sucesso! (" + p.getName() + ").");
                break;
        }
    }
}