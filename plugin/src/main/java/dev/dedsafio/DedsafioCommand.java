package dev.dedsafio;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class DedsafioCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBS = List.of(
            "start", "stop", "day", "status", "ruleta", "revive", "gulag",
            "score", "hermandad", "instrucciones", "reload");

    private final DedsafioPlugin plugin;

    public DedsafioCommand(DedsafioPlugin plugin) {
        this.plugin = plugin;
    }

    private void msg(CommandSender s, String text) {
        s.sendMessage(Component.text(text, NamedTextColor.YELLOW));
    }

    private void err(CommandSender s, String text) {
        s.sendMessage(Component.text(text, NamedTextColor.RED));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] a) {
        if (a.length == 0) {
            msg(sender, "Uso: /dedsafio " + String.join("|", SUBS));
            return true;
        }
        switch (a[0].toLowerCase()) {
            case "start" -> {
                plugin.game().start();
                msg(sender, "Evento iniciado.");
            }
            case "stop" -> plugin.game().stop();
            case "day" -> {
                if (a.length < 2) {
                    err(sender, "/dedsafio day <numero|next>");
                } else if (a[1].equalsIgnoreCase("next")) {
                    plugin.game().nextDay();
                } else {
                    try {
                        plugin.game().setDay(Integer.parseInt(a[1]));
                    } catch (NumberFormatException e) {
                        err(sender, "Numero invalido.");
                    }
                }
            }
            case "status" -> {
                msg(sender, "En marcha: " + plugin.game().isRunning() + " | Dia: " + plugin.game().day());
                msg(sender, "Ruleta activa: " + plugin.roulette().activeIds());
                plugin.scores().refresh();
            }
            case "ruleta" -> ruleta(sender, a);
            case "revive" -> {
                if (a.length < 2) {
                    err(sender, "/dedsafio revive <jugador>");
                    return true;
                }
                UUID id = plugin.scores().findId(a[1]);
                if (id == null) {
                    err(sender, "Jugador desconocido.");
                    return true;
                }
                plugin.deaths().revive(id, a[1]);
                msg(sender, a[1] + " revivido.");
            }
            case "gulag" -> {
                if (a.length >= 2 && a[1].equalsIgnoreCase("setspawn") && sender instanceof Player p) {
                    plugin.getConfig().set("gulag.spawn", p.getLocation());
                    plugin.saveConfig();
                    msg(sender, "Spawn del gulag guardado.");
                } else {
                    err(sender, "/dedsafio gulag setspawn (solo jugadores)");
                }
            }
            case "score" -> {
                if (a.length < 3) {
                    err(sender, "/dedsafio score <jugador> <puntos>");
                    return true;
                }
                ScoreManager.Stats s = plugin.scores().find(a[1]);
                if (s == null) {
                    err(sender, "Jugador desconocido.");
                    return true;
                }
                try {
                    s.score += Integer.parseInt(a[2]);
                    plugin.scores().save();
                    plugin.scores().refresh();
                    msg(sender, a[1] + " ahora tiene " + s.score + " puntos.");
                } catch (NumberFormatException e) {
                    err(sender, "Numero invalido.");
                }
            }
            case "hermandad" -> {
                if (a.length < 3) {
                    err(sender, "/dedsafio hermandad <nombre> <jugador>");
                    return true;
                }
                ScoreManager.Stats s = plugin.scores().find(a[2]);
                if (s == null) {
                    err(sender, "Jugador desconocido.");
                    return true;
                }
                s.brotherhood = a[1];
                plugin.scores().save();
                plugin.scores().refresh();
                msg(sender, a[2] + " pertenece ahora a " + a[1] + ".");
            }
            case "instrucciones" -> {
                if (a.length < 2) {
                    err(sender, "/dedsafio instrucciones <texto>");
                    return true;
                }
                String text = String.join(" ", Arrays.copyOfRange(a, 1, a.length));
                Title t = Title.title(
                        Component.text("INSTRUCCIONES", NamedTextColor.GOLD),
                        Component.text(text, NamedTextColor.WHITE),
                        Title.Times.times(Duration.ofMillis(300), Duration.ofSeconds(8), Duration.ofMillis(500)));
                Bukkit.getOnlinePlayers().forEach(p -> p.showTitle(t));
            }
            case "reload" -> {
                plugin.reloadConfig();
                plugin.roulette().load();
                plugin.game().resumeTimer();
                msg(sender, "Configuracion recargada.");
            }
            default -> err(sender, "Subcomando desconocido.");
        }
        return true;
    }

    private void ruleta(CommandSender sender, String[] a) {
        if (a.length < 2) {
            err(sender, "/dedsafio ruleta <list|random|off|id>");
            return;
        }
        switch (a[1].toLowerCase()) {
            case "list" -> msg(sender, "Cambios: " + plugin.roulette().ids());
            case "random" -> {
                List<String> pool = new ArrayList<>(plugin.roulette().ids());
                pool.removeAll(plugin.roulette().activeIds());
                if (pool.isEmpty()) {
                    err(sender, "No quedan cambios por activar.");
                    return;
                }
                plugin.roulette().spinAndActivate(pool.get((int) (Math.random() * pool.size())));
            }
            case "off" -> {
                if (a.length < 3) err(sender, "/dedsafio ruleta off <id>");
                else plugin.roulette().deactivate(a[2]);
            }
            default -> {
                if (plugin.roulette().ids().contains(a[1])) plugin.roulette().spinAndActivate(a[1]);
                else err(sender, "Cambio desconocido. Usa /dedsafio ruleta list");
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] a) {
        List<String> out = new ArrayList<>();
        if (a.length == 1) {
            out.addAll(SUBS);
        } else if (a.length == 2) {
            switch (a[0].toLowerCase()) {
                case "day" -> out.add("next");
                case "gulag" -> out.add("setspawn");
                case "ruleta" -> {
                    out.addAll(List.of("list", "random", "off"));
                    out.addAll(plugin.roulette().ids());
                }
                case "revive", "score" -> Bukkit.getOnlinePlayers().forEach(p -> out.add(p.getName()));
                default -> {
                }
            }
        } else if (a.length == 3) {
            if (a[0].equalsIgnoreCase("ruleta") && a[1].equalsIgnoreCase("off")) {
                out.addAll(plugin.roulette().activeIds());
            } else if (a[0].equalsIgnoreCase("hermandad")) {
                Bukkit.getOnlinePlayers().forEach(p -> out.add(p.getName()));
            }
        }
        String last = a[a.length - 1].toLowerCase();
        out.removeIf(s -> !s.toLowerCase().startsWith(last));
        return out;
    }
}
