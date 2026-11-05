package dev.dedsafio;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Puntuacion, estado vivo/muerto, hermandades y marcador lateral. */
public final class ScoreManager {

    public static final class Stats {
        public String name = "?";
        public int score;
        public int kills;
        public int deaths;
        public int totems;
        public int days;
        public boolean alive = true;
        public boolean pendingRevive;
        public String brotherhood;
    }

    private final DedsafioPlugin plugin;
    private final Map<UUID, Stats> stats = new LinkedHashMap<>();
    private final Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();

    public ScoreManager(DedsafioPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        stats.clear();
        ConfigurationSection root = plugin.store().yaml().getConfigurationSection("players");
        if (root == null) return;
        for (String key : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(key);
            if (s == null) continue;
            Stats st = new Stats();
            st.name = s.getString("name", "?");
            st.score = s.getInt("score");
            st.kills = s.getInt("kills");
            st.deaths = s.getInt("deaths");
            st.totems = s.getInt("totems");
            st.days = s.getInt("days");
            st.alive = s.getBoolean("alive", true);
            st.pendingRevive = s.getBoolean("pendingRevive", false);
            st.brotherhood = s.getString("brotherhood");
            stats.put(UUID.fromString(key), st);
        }
    }

    public void save() {
        YamlConfiguration y = plugin.store().yaml();
        y.set("players", null);
        for (Map.Entry<UUID, Stats> e : stats.entrySet()) {
            String p = "players." + e.getKey();
            Stats s = e.getValue();
            y.set(p + ".name", s.name);
            y.set(p + ".score", s.score);
            y.set(p + ".kills", s.kills);
            y.set(p + ".deaths", s.deaths);
            y.set(p + ".totems", s.totems);
            y.set(p + ".days", s.days);
            y.set(p + ".alive", s.alive);
            y.set(p + ".pendingRevive", s.pendingRevive);
            y.set(p + ".brotherhood", s.brotherhood);
        }
        plugin.store().save();
    }

    public Stats get(UUID id, String name) {
        Stats s = stats.computeIfAbsent(id, k -> new Stats());
        if (name != null) s.name = name;
        return s;
    }

    public Stats get(Player p) {
        return get(p.getUniqueId(), p.getName());
    }

    public Stats find(String name) {
        for (Stats s : stats.values()) {
            if (s.name.equalsIgnoreCase(name)) return s;
        }
        return null;
    }

    public UUID findId(String name) {
        for (Map.Entry<UUID, Stats> e : stats.entrySet()) {
            if (e.getValue().name.equalsIgnoreCase(name)) return e.getKey();
        }
        return null;
    }

    /** Suma puntos de supervivencia a todos los jugadores vivos. */
    public void awardSurvivalDay() {
        int pts = plugin.getConfig().getInt("game.survival-day-points", 10);
        for (Stats s : stats.values()) {
            if (s.alive) {
                s.days++;
                s.score += pts;
            }
        }
        save();
        refresh();
    }

    public void attach(Player p) {
        get(p);
        p.setScoreboard(board);
        refresh();
    }

    /** Entradas que ahora mismo aparecen en el marcador lateral. */
    private final java.util.Set<String> shown = new java.util.HashSet<>();

    public void refresh() {
        Objective obj = board.getObjective("dedsafio");
        if (obj == null) {
            obj = board.registerNewObjective("dedsafio", Criteria.DUMMY,
                    Component.text("DEDSAFIO", NamedTextColor.RED, TextDecoration.BOLD));
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        // Top 10 vivos: solo se tocan las entradas que cambian (sin borrar el objetivo entero)
        java.util.Set<String> top = new java.util.HashSet<>();
        for (Stats s : stats.values().stream()
                .filter(s -> s.alive)
                .sorted(Comparator.comparingInt((Stats s) -> s.score).reversed())
                .limit(10)
                .toList()) {
            top.add(s.name);
            obj.getScore(s.name).setScore(s.score);
        }
        for (String old : new java.util.ArrayList<>(shown)) {
            if (!top.contains(old)) board.resetScores(old);
        }
        shown.clear();
        shown.addAll(top);

        // Hermandades: crea el equipo si falta y mueve al jugador si cambio de hermandad
        for (Stats s : stats.values()) {
            Team current = board.getEntryTeam(s.name);
            if (s.brotherhood == null) {
                if (current != null) current.removeEntry(s.name);
                continue;
            }
            if (current != null && current.getName().equals(s.brotherhood)) continue;
            if (current != null) current.removeEntry(s.name);
            Team t = board.getTeam(s.brotherhood);
            if (t == null) {
                t = board.registerNewTeam(s.brotherhood);
                t.prefix(Component.text("[" + s.brotherhood + "] ", NamedTextColor.AQUA));
            }
            t.addEntry(s.name);
        }
    }
}
