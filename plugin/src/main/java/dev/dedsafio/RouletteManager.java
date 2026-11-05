package dev.dedsafio;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * La "ruleta": cambios de dificultad que se activan (a mano o por dia)
 * y modifican el servidor. Efectos soportados:
 * DAMAGE_MULTIPLIER, MOB_HEALTH_MULTIPLIER, GAMERULE, POTION.
 */
public final class RouletteManager implements Listener {

    public record RouletteChange(String id, String name, String description,
                                 NamedTextColor color, int day, List<Map<?, ?>> effects) {
    }

    private final DedsafioPlugin plugin;
    private final Map<String, RouletteChange> catalog = new LinkedHashMap<>();
    private final Set<String> active = new LinkedHashSet<>();

    public RouletteManager(DedsafioPlugin plugin) {
        this.plugin = plugin;
    }

    // ---------- carga / guardado ----------

    public void load() {
        catalog.clear();
        ConfigurationSection root = plugin.getConfig().getConfigurationSection("changes");
        if (root != null) {
            for (String id : root.getKeys(false)) {
                ConfigurationSection s = root.getConfigurationSection(id);
                if (s == null) continue;
                NamedTextColor color = NamedTextColor.NAMES.value(s.getString("color", "red"));
                catalog.put(id, new RouletteChange(
                        id,
                        s.getString("name", id),
                        s.getString("description", ""),
                        color != null ? color : NamedTextColor.RED,
                        s.getInt("day", 0),
                        new ArrayList<>(s.getMapList("effects"))));
            }
        }
        active.clear();
        active.addAll(plugin.store().yaml().getStringList("roulette.active"));
    }

    private void persist() {
        plugin.store().yaml().set("roulette.active", new ArrayList<>(active));
        plugin.store().save();
    }

    public Set<String> ids() {
        return catalog.keySet();
    }

    public Set<String> activeIds() {
        return active;
    }

    // ---------- activacion ----------

    /** Se llama al cambiar de dia. */
    public void onDay(int day) {
        if (!plugin.getConfig().getBoolean("roulette.auto-activate-on-day", true)) return;
        for (RouletteChange c : catalog.values()) {
            if (c.day() == day && !active.contains(c.id())) {
                spinAndActivate(c.id());
            }
        }
    }

    // ---------- animacion de la ruleta ----------

    /** Giros pendientes: si caen varios cambios el mismo dia, se muestran uno detras de otro. */
    private final java.util.ArrayDeque<String> queue = new java.util.ArrayDeque<>();
    private boolean spinning;

    public void spinAndActivate(String id) {
        RouletteChange target = catalog.get(id);
        if (target == null) return;
        if (spinning) {
            if (!queue.contains(id)) queue.add(id);
            return;
        }
        spinning = true;
        int steps = Math.max(8, plugin.getConfig().getInt("roulette.animation.spin-steps", 24));
        int startDelay = Math.max(1, plugin.getConfig().getInt("roulette.animation.start-delay-ticks", 2));
        int endDelay = Math.max(startDelay, plugin.getConfig().getInt("roulette.animation.end-delay-ticks", 14));
        spinStep(target, new ArrayList<>(catalog.values()), 0, steps, startDelay, endDelay, null);
    }

    /**
     * Un "fotograma" de la ruleta. Cada paso espera mas que el anterior (frenada),
     * el tono del sonido sube y el ultimo fotograma cae siempre en el cambio elegido.
     */
    private void spinStep(RouletteChange target, List<RouletteChange> all, int step, int steps,
                          int startDelay, int endDelay, String lastId) {
        boolean lastFrame = step == steps - 1;
        double t = steps <= 1 ? 1.0 : step / (double) (steps - 1);

        RouletteChange shown = target;
        if (!lastFrame && all.size() > 1) {
            do {
                shown = all.get(ThreadLocalRandom.current().nextInt(all.size()));
            } while (shown.id().equals(lastId));
        }

        int delay = startDelay + (int) Math.round((endDelay - startDelay) * t * t);
        showAll(shown.name(), lastFrame ? "\u25B6  \u25C0" : "\u00B7  \u00B7  \u00B7",
                shown.color(), 0, delay + 2, 0);
        sound(Sound.UI_BUTTON_CLICK, (float) (0.7 + 0.9 * t));

        final String shownId = shown.id();
        if (lastFrame) {
            // pausa dramatica con el resultado en pantalla y luego revelacion
            sound(Sound.ENTITY_PLAYER_LEVELUP, 0.8f);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                activate(target);
                // pequeno margen antes de la siguiente ruleta en cola
                Bukkit.getScheduler().runTaskLater(plugin, this::finishSpin, 100L);
            }, 20L);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin,
                    () -> spinStep(target, all, step + 1, steps, startDelay, endDelay, shownId), delay);
        }
    }

    private void finishSpin() {
        spinning = false;
        String next = queue.poll();
        if (next != null && !active.contains(next)) spinAndActivate(next);
    }

    public void activate(RouletteChange c) {
        active.add(c.id());
        for (Map<?, ?> effect : c.effects()) apply(effect, true);
        persist();
        showAll(c.name(), c.description(), c.color(), 10, 80, 20);
        sound(Sound.ENTITY_WITHER_SPAWN);
        Bukkit.broadcast(Component.text("[Ruleta] ", NamedTextColor.DARK_RED)
                .append(Component.text(c.name() + " - " + c.description(), c.color())));
    }

    public void deactivate(String id) {
        RouletteChange c = catalog.get(id);
        if (c == null || !active.remove(id)) return;
        for (Map<?, ?> effect : c.effects()) apply(effect, false);
        persist();
        Bukkit.broadcast(Component.text("[Ruleta] Desactivado: " + c.name(), NamedTextColor.GREEN));
    }

    // ---------- efectos ----------

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void apply(Map<?, ?> m, boolean on) {
        String type = String.valueOf(m.get("type")).toUpperCase();
        switch (type) {
            case "GAMERULE" -> {
                GameRule rule = GameRule.getByName(String.valueOf(m.get("rule")));
                if (rule == null) {
                    plugin.getLogger().warning("GameRule desconocida: " + m.get("rule")
                            + " (puede haber cambiado de nombre en tu version de Minecraft)");
                    return;
                }
                Object value = on ? m.get("value") : m.get("revert");
                if (value == null) return;
                for (World w : Bukkit.getWorlds()) {
                    if (value instanceof Boolean b) w.setGameRule(rule, b);
                    else if (value instanceof Number n) w.setGameRule(rule, n.intValue());
                }
            }
            case "POTION" -> {
                if (!on) return;
                PotionEffectType pet = Registry.POTION_EFFECT_TYPE
                        .get(NamespacedKey.minecraft(String.valueOf(m.get("effect"))));
                if (pet == null) {
                    plugin.getLogger().warning("Efecto de pocion desconocido: " + m.get("effect"));
                    return;
                }
                int amp = m.get("amplifier") instanceof Number n ? n.intValue() : 0;
                int secs = m.get("seconds") instanceof Number n ? n.intValue() : 60;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (plugin.scores().get(p).alive) {
                        p.addPotionEffect(new PotionEffect(pet, secs * 20, amp));
                    }
                }
            }
            default -> {
                // DAMAGE_MULTIPLIER y MOB_HEALTH_MULTIPLIER son pasivos (ver listeners)
            }
        }
    }

    private double multiplier(String type) {
        double result = 1.0;
        for (String id : active) {
            RouletteChange c = catalog.get(id);
            if (c == null) continue;
            for (Map<?, ?> e : c.effects()) {
                if (type.equalsIgnoreCase(String.valueOf(e.get("type"))) && e.get("value") instanceof Number n) {
                    result *= n.doubleValue();
                }
            }
        }
        return result;
    }

    // ---------- listeners pasivos ----------

    @EventHandler(ignoreCancelled = true)
    public void onMobDamagePlayer(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Entity damager = e.getDamager();
        if (damager instanceof Projectile proj && proj.getShooter() instanceof Entity shooter) {
            damager = shooter;
        }
        if (damager instanceof Monster) {
            e.setDamage(e.getDamage() * multiplier("DAMAGE_MULTIPLIER"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMobSpawn(CreatureSpawnEvent e) {
        if (!(e.getEntity() instanceof Monster mob)) return;
        double mult = multiplier("MOB_HEALTH_MULTIPLIER");
        if (mult == 1.0) return;
        AttributeInstance hp = mob.getAttribute(Attribute.MAX_HEALTH);
        if (hp == null) return;
        hp.setBaseValue(hp.getBaseValue() * mult);
        mob.setHealth(hp.getValue());
    }

    // ---------- utilidades ----------

    private void showAll(String title, String sub, NamedTextColor color, int inTicks, int stayTicks, int outTicks) {
        Title t = Title.title(
                Component.text(title, color),
                Component.text(sub, NamedTextColor.GRAY),
                Title.Times.times(Duration.ofMillis(inTicks * 50L), Duration.ofMillis(stayTicks * 50L),
                        Duration.ofMillis(outTicks * 50L)));
        Bukkit.getOnlinePlayers().forEach(p -> p.showTitle(t));
    }

    private void sound(Sound s) {
        sound(s, 1f);
    }

    private void sound(Sound s, float pitch) {
        for (Player p : Bukkit.getOnlinePlayers()) p.playSound(p.getLocation(), s, 1f, pitch);
    }
}
