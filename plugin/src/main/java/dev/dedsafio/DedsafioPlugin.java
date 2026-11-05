package dev.dedsafio;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class DedsafioPlugin extends JavaPlugin implements Listener {

    private DataStore store;
    private ScoreManager scores;
    private GameManager game;
    private RouletteManager roulette;
    private DeathListener deaths;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        store = new DataStore(this);
        scores = new ScoreManager(this);
        roulette = new RouletteManager(this);
        game = new GameManager(this);
        deaths = new DeathListener(this);

        scores.load();
        roulette.load();
        game.load();

        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(roulette, this);
        Bukkit.getPluginManager().registerEvents(deaths, this);

        DedsafioCommand cmd = new DedsafioCommand(this);
        getCommand("dedsafio").setExecutor(cmd);
        getCommand("dedsafio").setTabCompleter(cmd);

        for (Player p : Bukkit.getOnlinePlayers()) scores.attach(p);
        game.resumeTimer();
        Bukkit.getScheduler().runTaskTimer(this, scores::refresh, 20L, 100L);

        getLogger().info("DedsafioCore activado.");
    }

    @Override
    public void onDisable() {
        if (scores != null) scores.save();
        if (store != null) store.save();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        scores.attach(p);
        ScoreManager.Stats st = scores.get(p);
        if (game.isRunning() && !st.alive) {
            Bukkit.getScheduler().runTask(this, () -> deaths.applyDeadState(p));
        } else if (st.pendingRevive) {
            Bukkit.getScheduler().runTask(this, () -> deaths.applyRevivedState(p));
        }
    }

    public DataStore store() { return store; }
    public ScoreManager scores() { return scores; }
    public GameManager game() { return game; }
    public RouletteManager roulette() { return roulette; }
    public DeathListener deaths() { return deaths; }
}
