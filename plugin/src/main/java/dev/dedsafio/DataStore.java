package dev.dedsafio;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/** Persistencia simple en data.yml (estado del evento, puntuaciones, ruleta activa). */
public final class DataStore {

    private final DedsafioPlugin plugin;
    private final File file;
    private final YamlConfiguration yaml;

    public DataStore(DedsafioPlugin plugin) {
        this.plugin = plugin;
        plugin.getDataFolder().mkdirs();
        this.file = new File(plugin.getDataFolder(), "data.yml");
        this.yaml = YamlConfiguration.loadConfiguration(file);
    }

    public YamlConfiguration yaml() {
        return yaml;
    }

    public void save() {
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("No se pudo guardar data.yml: " + e.getMessage());
        }
    }
}
