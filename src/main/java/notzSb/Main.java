package notzSb;

import notzSb.commands.ScoreboardC;
import notzSb.events.JoinLeaveE;
import notzSb.file.ConfigFile;
import notzSb.utils.ScoreboardU;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

import static notzSb.utils.CorU.c;
import static notzSb.utils.CorU.send;

public final class Main extends JavaPlugin {
    public static String prefix;
    public static Main plugin;
    public static ConfigFile cf, sf, stf;
    public static boolean start = false;

    @Override
    public void onEnable() {
        plugin = this;
        cf = new ConfigFile(this, "config");
        sf = new ConfigFile(this, "scoreboard");
        stf = new ConfigFile(this, "staffs");
        prefix = c(cf.getConfig().getString("prefix"));


        getServer().getScheduler().runTaskLater(this, new BukkitRunnable(){public void run() {
            start = true;
            letters();
        }}, 2*20);

        regCommands();
        regEvents();
        regTab();

        getServer().getScheduler().runTaskTimer(this, ScoreboardU.getInstance(), 0, cf.getConfig().getLong("time"));
//        TimerT t = new TimerT();
//        t.runTaskTimer(Main.plugin, 0, cf.getConfig().getLong("time") * 20);

//        getServer().getScheduler().runTaskTimer(this, Boarddd.getInstance(), 0, 20);

        Bukkit.getConsoleSender().sendMessage(prefix + c("&aPlugin iniciado"));
        ScoreboardU.getStaffs().values().forEach(p -> send(p.getP(), "&aPlugin sendo habilitado!"));
    }

    @Override
    public void onDisable() {
        ScoreboardU.getStaffs().values().forEach(p -> send(p.getP(), "&cPlugin sendo desabilitado!"));
        Bukkit.getOnlinePlayers().forEach(p -> p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard()));
    }
    public void regEvents() {
        Bukkit.getPluginManager().registerEvents(new JoinLeaveE(), this);
    }

    public void regCommands() {
        Objects.requireNonNull(getCommand("notzsb")).setExecutor(new ScoreboardC());
    }
    public void regTab() {
        Objects.requireNonNull(getCommand("notzsb")).setTabCompleter(new ScoreboardC());
    }
    private void letters() {
        Bukkit.getConsoleSender().sendMessage(prefix + c("&2Inicializado com sucesso.") +
                        c("\n&f┳┓    &6┏┓       ┓        ┓"
                        + "\n&f┃┃┏┓╋┓&6┗┓┏┏┓┏┓┏┓┣┓┏┓┏┓┏┓┏┫"
                        + "\n&f┛┗┗┛┗┗&6┗┛┗┗┛┛ ┗ ┗┛┗┛┗┻┛ ┗┻"));
    }
}
