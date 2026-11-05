# Roadmap de desarrollo

## Fase 0 — Preparación (1-2 días)
- [ ] Instalar JDK 25, Gradle 9.1+ (o usar el wrapper), IntelliJ IDEA, Docker.
- [ ] Definir con tus propias palabras el "diseño del evento": nº de jugadores, duración (días), hermandades, reglas de muerte.
- [ ] Ver VODs de DEDsafio 2/3/4 y anotar: ruleta por día, gulags, jefes, normas. Rellenar una hoja con esos datos.

## Fase 0.5 — Migración a Paper 26.3 (hecha en código, sin compilar)
- [x] `build.gradle.kts`: `paper-api:26.3.build.+`, Java 25 (requiere Gradle 9.1+).
- [x] `plugin.yml` `api-version: '26.3'`, docker-compose (`java25`, `VERSION 26.3`).
- [x] Corregidos: `e.getEntity()` en muerte, revivir a jugadores desconectados (`pendingRevive`),
      marcador sin parpadeo, cambio de hermandad.
- [x] Guía de despliegue offline en `docs/PROXMOX.md` + servicio systemd en `server/systemd/`.
- [ ] Verificar `api-version`, `GameRule.getByName` y `pack_format` del resource pack en 26.3.

## Fase 1 — Núcleo (incluido en este proyecto)
- [ ] Compilar el plugin: `cd plugin && gradle build`
- [ ] Arrancar servidor dev: `cd server && docker compose up -d`
- [ ] Probar: `/dedsafio start`, `day next`, `ruleta random`, morir con y sin tótem, `revive`.
- [ ] Ajustar `config.yml` con tu ruleta real.

## Fase 2 — Mundo y gulag
- [ ] Mundo principal (mapa propio o generado) + mundo del gulag (Multiverse).
- [ ] Spawn del gulag (`/dedsafio gulag setspawn`).
- [ ] Minijuego de gulag nº1 (p. ej. "Golpea al topo": entidades que aparecen y se golpean, con marcador).
- [ ] Combate 1v1 en arena con ganador → `revive`.

## Fase 3 — Ruleta avanzada
- [ ] Nuevos tipos de efecto: spawn de mobs especiales, eventos súbitos (lluvia de meteoros, noche), cambio de dimensión.
- [ ] GUI de administración con la lista de cambios por día (inventario clicable).
- [ ] Sonidos y overlay con resource pack.

## Fase 4 — RPG y contenido
- [ ] Items custom (armas/armaduras) con `custom_model_data` + recetas.
- [ ] Mobs y dungeons (MythicMobs o código propio).
- [ ] Jefe semanal y jefe final (REviil-like, con tu propio villano).
- [ ] Menú de misiones diarias + "enciclopedia".

## Fase 5 — Economía y social
- [ ] Monedas, cajeros (NPCs), correo entre jugadores, waypoints.
- [ ] Hermandades con chat propio y puntuación conjunta.

## Fase 6 — Producción
- [ ] VPS, backups, whitelist, anti-cheat/anti-grief.
- [ ] Prueba de carga con bots (p. ej. mineflayer) para ver TPS con N jugadores.
- [ ] Web de estadísticas (leer `data.yml` o pasar a SQLite/MySQL).
- [ ] Ensayo general con amigos antes del evento.

## Deuda técnica conocida
- `data.yml` se reescribe entero al guardar: pasar a SQLite si hay muchos jugadores.
- Nombres de GameRule: pueden cambiar entre versiones de Minecraft.
- El código no se ha compilado ni probado en servidor real (ver README).
