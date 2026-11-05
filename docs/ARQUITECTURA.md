# Arquitectura propuesta

## Decisión principal: Paper + plugin Java + resource pack

| Opción | Pros | Contras |
|---|---|---|
| **Paper (plugin) + resource pack** ✅ | Los jugadores entran con Minecraft vanilla (sin mods). Fácil de desplegar y escalar. Rápido de desarrollar. | Sin bloques/entidades totalmente nuevos (se simulan con `custom_model_data`, item displays y mobs vanilla modificados). |
| Fabric/Forge/NeoForge (mod) | Entidades, bloques e items 100% custom. Es lo que usa la serie original. | Cada jugador instala mods. Más trabajo y mantenimiento por versión. |
| Híbrido (Paper + mod cliente mínimo) | Lo mejor de ambos | Más complejidad |

**Recomendación:** empieza con Paper + resource pack. Si luego necesitas entidades/bosses muy custom, añade un mod o usa Citizens/ModelEngine/MythicMobs.

## Módulos del plugin (`dev.dedsafio`)

```
DedsafioPlugin ── arranque, registro de comandos/eventos
 ├─ DataStore ........ persistencia (data.yml)
 ├─ GameManager ...... evento en marcha, día actual, temporizador
 ├─ RouletteManager .. catálogo de cambios + efectos + animación
 ├─ DeathListener .... muerte definitiva, tótems, gulag/eliminación, revivir
 ├─ ScoreManager ..... puntos, vivos/muertos, hermandades, marcador
 └─ DedsafioCommand .. /dedsafio ...
```

### Flujo de un día
```
/dedsafio day next
  → GameManager.setDay → título "DÍA N"
  → ScoreManager.awardSurvivalDay (+puntos a vivos)
  → RouletteManager.onDay(N) → gira la ruleta → activa los cambios con day == N
```

### Flujo de una muerte
```
PlayerDeathEvent (si hay tótem, Minecraft lo evita y salta EntityResurrectEvent)
  → alive=false, deaths++, score += penalty
  → killer.kills++ , +puntos
  → rayo + título global
  → Respawn: mode SPECTATOR | GULAG (tp al gulag) | ELIMINATE (quita whitelist + kick)
```

## Tipos de efecto de la ruleta (ya implementados)
| type | Parámetros | Notas |
|---|---|---|
| `DAMAGE_MULTIPLIER` | `value` | Multiplica daño de monstruos a jugadores (pasivo, se acumula). |
| `MOB_HEALTH_MULTIPLIER` | `value` | Vida máxima de monstruos al spawnear. |
| `GAMERULE` | `rule`, `value`, `revert` | Cambia gamerule en todos los mundos. Revisa el nombre en tu versión. |
| `POTION` | `effect`, `amplifier`, `seconds` | Pociones a jugadores vivos. |

Para añadir uno nuevo: método `apply()` en `RouletteManager` (efectos puntuales) o un listener (efectos pasivos).

## Plugins externos recomendados
- **LuckPerms** (permisos), **spark** (profiling), **WorldEdit/FAWE** + **WorldGuard** (construir y proteger arenas y gulag).
- **Citizens** o **MythicMobs/ModelEngine** (bosses y NPCs custom).
- **ProtocolLib / PacketEvents** (overlays avanzados, opcional).
- **Multiverse-Core** (mundos: overworld, gulag, arena de boss).
- **Discord/Twitch** bridge si quieres integrar el chat.

## Infraestructura
- Desarrollo: `docker compose up` (carpeta `server/`).
- Producción: VPS/dedicado con Java 25, ≥ 8 GB RAM para ~40-60 jugadores (ajusta con spark); 100 jugadores requiere más y optimizar (view-distance, entidades).
- Backups automáticos del mundo cada X horas; `data.yml` incluido.
- Whitelist obligatoria; anti-grief y límites de entidades.
- Para muchos jugadores concurrentes valora un proxy (Velocity) solo si necesitas varios servidores; empieza sin él.

## Qué NO está hecho todavía
Gulag con minijuegos automáticos, bosses y mobs custom, misiones y DEDciclopedia, economía/cajeros/correo, overlay de instrucciones con resource pack, panel web de estadísticas. Ver `ROADMAP.md`.
