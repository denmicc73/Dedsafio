package dev.dedsafio;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;

/** Estado global del evento: en marcha o no, y dia actual. */
public final class GameManager {

    private final DedsafioPlugin plugin;
    private boolean running;
    private int day;
    private BukkitTask timer;

    public GameManager(DedsafioPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        running = plugin.store().yaml().getBoolean("game.running", false);
        day = plugin.store().yaml().getInt("game.day", 0);
    }

    private void persist() {
        plugin.store().yaml().set("game.running", running);
        plugin.store().yaml().set("game.day", day);
        plugin.store().save();
    }

    public boolean isRunning() {
        return running;
    }

    public int day() {
        return day;
    }

    public void start() {
        running = true;
        day = 1;
        persist();
        announceDay();
        plugin.roulette().onDay(day);
        resumeTimer();
    }

    public void stop() {
        running = false;
        persist();
        cancelTimer();
        Bukkit.broadcast(Component.text("El evento ha terminado.", NamedTextColor.YELLOW));
    }

    public void nextDay() {
        setDay(day + 1);
        plugin.scores().awardSurvivalDay();
    }

    public void setDay(int newDay) {
        day = newDay;
        persist();
        announceDay();
        plugin.roulette().onDay(day);
    }

    public void resumeTimer() {
        cancelTimer();
        int minutes = plugin.getConfig().getInt("game.auto-advance-minutes", 0);
        if (!running || minutes <= 0) return;
        long ticks = minutes * 60L * 20L;
        timer = Bukkit.getScheduler().runTaskTimer(plugin, this::nextDay, ticks, ticks);
    }

    private void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void announceDay() {
        Title t = Title.title(
                Component.text("DIA " + day, NamedTextColor.RED),
                Component.text("Sobrevive.", NamedTextColor.GRAY),
                Title.Times.times(Duration.ofMillis(300), Duration.ofSeconds(3), Duration.ofMillis(800)));
        Bukkit.getOnlinePlayers().forEach(p -> p.showTitle(t));
    }
}
