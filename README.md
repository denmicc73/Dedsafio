# Servidor estilo DEDsafio — proyecto base

Proyecto para construir tu propio servidor de Minecraft inspirado en **DEDsafio** (ElDed / Eufonia Studio).

## Contenido
```
INSTRUCCIONES_PROYECTO.md   → texto para pegar en las instrucciones de un Proyecto de Claude
docs/INVESTIGACION.md       → qué es Dedsafio, mecánicas, mods existentes, fuentes
docs/ARQUITECTURA.md        → decisiones técnicas y módulos
docs/ROADMAP.md             → fases con checklist
plugin/                     → plugin Paper (Java 25 + Gradle 9.1+) con el núcleo
server/                     → docker-compose, scripts de arranque, server.properties
resourcepack/               → esqueleto de resource pack (pon tus propios assets)
```

## Qué hace ya el plugin (DedsafioCore)
- Evento con días (`/dedsafio start`, `day next`, avance automático opcional).
- **Ruleta** de cambios de dificultad con animación, configurable en `config.yml`.
- **Muerte definitiva**: penalización de puntos, modos `SPECTATOR`, `GULAG` o `ELIMINATE`; cuenta de tótems; `revive`.
- **Puntuación** + marcador lateral + **hermandades**.
- Anuncio de instrucciones en pantalla.

## Arranque rápido
Requisitos: JDK 25, Gradle 9.1+ (o genera el wrapper con `gradle wrapper`), Docker.

```bash
# 1. Compilar el plugin
cd plugin
gradle build                      # genera build/libs/dedsafio-core-0.1.0.jar

# 2. Servidor de desarrollo
cd ../server
# edita docker-compose.yml: OPS y WHITELIST con tu nombre de Minecraft
docker compose up -d
docker compose logs -f mc

# 3. Entra en localhost:25565 y prueba
/dedsafio start
/dedsafio ruleta random
/dedsafio day next
/dedsafio status
```

Sin Docker: descarga Paper desde https://papermc.io/downloads/paper, renómbralo `paper.jar`, copia el `.jar` del plugin a `plugins/` y ejecuta `server/start.sh` (o `start.bat`).

## Comandos
| Comando | Acción |
|---|---|
| `/dedsafio start` / `stop` | Inicia / termina el evento |
| `/dedsafio day <n\|next>` | Cambia de día (suma puntos y dispara la ruleta del día) |
| `/dedsafio ruleta <list\|random\|off id\|id>` | Gestiona la ruleta |
| `/dedsafio revive <jugador>` | Revive a un jugador |
| `/dedsafio gulag setspawn` | Guarda la ubicación del gulag |
| `/dedsafio score <jugador> <puntos>` | Suma/resta puntos |
| `/dedsafio hermandad <nombre> <jugador>` | Asigna hermandad |
| `/dedsafio instrucciones <texto>` | Muestra instrucciones en pantalla a todos |
| `/dedsafio reload` | Recarga config |

## Aviso honesto
- El código **no se ha compilado ni probado** en un servidor real (el entorno donde lo generé no tiene acceso a los repositorios de Paper). Es posible que haya que corregir algún detalle de API tras el primer `gradle build`; pega el error y se arregla.
- La ruleta de `config.yml` son **ejemplos inventados**, no las reglas reales de la serie.
- Verifica versiones: `paper-api` en `build.gradle.kts`, `VERSION` en docker-compose y `pack_format` del resource pack.
- Usa assets y nombre originales; no copies material de Eufonia.

## Siguiente paso
Abre `docs/ROADMAP.md` y sigue la Fase 1. Para trabajar con Claude, pega `INSTRUCCIONES_PROYECTO.md` en un Proyecto y sube esta carpeta como conocimiento.

## Versión objetivo
Paper **26.3** (Minecraft Java 26.3), **Java 25**, Gradle **9.1+**. El `pack_format` de `resourcepack/pack.mcmeta`
sigue siendo el de 1.21.4: compruébalo para 26.3 antes de publicar el pack. Despliegue en Proxmox: `docs/PROXMOX.md`.
