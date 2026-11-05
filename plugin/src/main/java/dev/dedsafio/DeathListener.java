package dev.dedsafio;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.time.Duration;
import java.util.UUID;

/**
 * Muerte definitiva. Si el jugador tiene un totem, Minecraft lo salva y
 * PlayerDeathEvent no se dispara; solo contamos el uso del totem.
 */
public final class DeathListener implements Listener {

    private final DedsafioPlugin plugin;

    public DeathListener(DedsafioPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTotem(EntityResurrectEvent e) {
        if (!plugin.game().isRunning() || !(e.getEntity() instanceof Player p)) return;
        plugin.scores().get(p).totems++;
        plugin.scores().save();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent e) {
        if (!plugin.game().isRunning()) return;
        Player p = e.getEntity();
        ScoreManager.Stats s = plugin.scores().get(p);
        if (!s.alive) return;

        s.alive = false;
        s.deaths++;
        s.score += plugin.getConfig().getInt("death.penalty", -500);

        Player killer = p.getKiller();
        if (killer != null) {
            ScoreManager.Stats ks = plugin.scores().get(killer);
            ks.kills++;
            ks.score += plugin.getConfig().getInt("death.kill-points", 50);
        }

        if (plugin.getConfig().getBoolean("death.lightning", true)) {
            p.getWorld().strikeLightningEffect(p.getLocation());
        }

        Title t = Title.title(
                Component.text("\u2620 " + p.getName(), NamedTextColor.DARK_RED),
                Component.text("ha caido el dia " + plugin.game().day(), NamedTextColor.GRAY),
                Title.Times.times(Duration.ofMillis(300), Duration.ofSeconds(4), Duration.ofSeconds(1)));
        Bukkit.getOnlinePlayers().forEach(o -> o.showTitle(t));

        plugin.scores().save();
        plugin.scores().refresh();
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (!plugin.game().isRunning()) return;
        Player p = e.getPlayer();
        ScoreManager.Stats s = plugin.scores().get(p);
        if (s.alive) return;

        String mode = plugin.getConfig().getString("death.mode", "SPECTATOR").toUpperCase();
        if (mode.equals("GULAG")) {
            Location gulag = plugin.getConfig().getLocation("gulag.spawn");
            if (gulag != null) e.setRespawnLocation(gulag);
        }
        // Cambiar de modo en el mismo tick del respawn no es fiable: lo hacemos 1 tick despues
        Bukkit.getScheduler().runTask(plugin, () -> applyDeadState(p));
    }

    /** Aplica el estado de "muerto" segun la config. Tambien se usa al reconectar. */
    public void applyDeadState(Player p) {
        String mode = plugin.getConfig().getString("death.mode", "SPECTATOR").toUpperCase();
        switch (mode) {
            case "GULAG" -> p.setGameMode(GameMode.ADVENTURE);
            case "ELIMINATE" -> {
                p.setWhitelisted(false);
                p.kick(Component.text("Has sido eliminado de Dedsafio. Gracias por jugar.", NamedTextColor.RED));
            }
            default -> p.setGameMode(GameMode.SPECTATOR);
        }
    }

    /** Revive a un jugador (por ejemplo, tras ganar el gulag). */
    public void revive(UUID id, String name) {
        ScoreManager.Stats s = plugin.scores().get(id, name);
        s.alive = true;
        plugin.scores().save();
        plugin.scores().refresh();

        org.bukkit.OfflinePlayer off = Bukkit.getOfflinePlayer(id);
        off.setWhitelisted(true);

        Player p = Bukkit.getPlayer(id);
        if (p != null) {
            applyRevivedState(p);
        } else {
            // Desconectado: se aplica al volver a entrar (ver DedsafioPlugin.onJoin)
            s.pendingRevive = true;
            plugin.scores().save();
        }
    }

    /** Devuelve a un jugador revivido a modo supervivencia en el spawn. */
    public void applyRevivedState(Player p) {
        ScoreManager.Stats s = plugin.scores().get(p);
        s.pendingRevive = false;
        Location loc = p.getRespawnLocation();
        if (loc == null) loc = Bukkit.getWorlds().get(0).getSpawnLocation();
        p.setGameMode(GameMode.SURVIVAL);
        p.teleport(loc);
        p.sendMessage(Component.text("Has vuelto a la vida.", NamedTextColor.GREEN));
        plugin.scores().save();
    }
}
