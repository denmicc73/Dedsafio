# Investigación: DEDsafio (ElDed + Eufonia Studio)

> Fecha de la investigación: 5 nov 2026. Fuentes públicas (prensa, CurseForge, Modrinth, sitio de Eufonia, webs de fans).
> Lo marcado como **[sin confirmar]** son cosas que NO pude verificar y debes comprobar viendo los directos/VODs.

## 1. Qué es

- Serie de Minecraft en español creada por **ElDed** y desarrollada por **Eufonia Studio**.
- Decenas de streamers/creadores participan a la vez en un servidor RPG-survival con **muerte definitiva**, eventos puntuables y un villano final.
- Ediciones conocidas:
  | Edición | Año | Datos |
  |---|---|---|
  | DEDsafio 1 | 2021 | +60 jugadores. Villano: **REviil**. Eventos semanales con boss fight, ruleta, Gulag. |
  | DEDsafio 2 | 2022 | 84 creadores, 29-30 días, apoyada por Twitch Rivals. Gulags temáticos, nuevas dungeons y jefes. |
  | DEDsafio 3 | 2024 | Gulag "Golpea al Topo". |
  | DEDsafio 4 | reciente | Monedas, cajeros, correo, waypoints (según mods de fans). |
  | Dedsafio Bingo / Mitologías | 2021+ | Series hermanas (Mitologías = precuela RPG de Dedsafio Minecraft). |
- Eufonia tiene su propio **launcher** (Eufonia Client) que da acceso a eventos y mantiene los mods actualizados.

## 2. Mecánicas identificadas (lo que tu servidor debe imitar)

| Sistema | Descripción | Confianza |
|---|---|---|
| **Muerte definitiva** | Morir = eliminado de la serie (fan stats: la muerte "es definitiva"). Existe un sistema de muerte con animación en pantalla, sonido y baneo automático. | Alta |
| **Tótems de inmortalidad** | Salvan de una muerte segura. En D4 el tótem vanilla se muestra como "NutriTotem". Se cuentan usados/obtenidos. | Alta |
| **Ruleta de dificultad** | Evento aleatorio diario que sube la dificultad: mobs nuevos/más fuertes, cambios de entorno, eventos súbitos, avisos. Impulsada por el villano REviil. | Alta |
| **Eventos semanales / boss** | Cada semana un evento importante; al final los supervivientes se enfrentan al jefe final (REviil, Nethertitan en D2...). | Alta |
| **Gulag** | Los caídos pueden pelear en el Gulag para volver. Cada edición lo cambia: boxeo y espacio (D2), "Golpea al Topo" (D3). | Alta |
| **Revivir** | Los muertos pueden ser revividos (por Gulag o por logro colectivo; en D2 resucitan a Eon al final). | Media |
| **Desafíos diarios** | Misiones por día y menú de misiones, notificaciones, crafteos custom, "DEDciclopedia". | Media-Alta |
| **RPG** | Roles, árboles de habilidades, armas poderosas (espadas ancestral, bélica, celestial), sets de armadura (Dungeon, Tierra, Dimensional), criaturas mágicas, dungeons. | Alta |
| **Hermandades (equipos)** | Los jugadores se agrupan; se compara score, supervivencia, kills, tótems y bajas por hermandad. | Media (fan stats) |
| **Puntuación** | Puntos por supervivencia (días, tótems), combate (kills), exploración y eventos. **Morir resta 500 puntos** (según web de fans). | Media |
| **Instrucciones en pantalla** | Overlay de instrucciones estilo "juego" (también en Squid Craft Games), sincronizado para todos. | Alta |
| **Economía (D4)** | Monedas, cajeros (ATM), correo entre jugadores, waypoints, "palito mágico" (teleporta al respawn). | Media |
| **Fusión / ruleta de colores (D4)** | Ruleta con colores (roja, verde, naranja, rosada) y objetivos REVILL. | Media |

**[sin confirmar]**: valores exactos de daño/vida, lista completa de la ruleta por día, normas del gulag, recetas custom, balance económico. Esos datos hay que sacarlos viendo VODs, wiki de fans o preguntando a la comunidad.

## 3. Ecosistema de mods/plugins ya existente (para estudiar o reutilizar)

| Proyecto | Plataforma | Qué hace |
|---|---|---|
| Dedsafio Utils | Modrinth, Paper/Spigot 1.19-1.21.3 | Utilidades para recrear Dedsafio; sistema de muerte con JSON (`deathMessage`, `inventoryDrop`...). |
| Setsumeisho | Modrinth, Paper/Fabric/Forge 1.20.1-1.21.1 | Overlay de instrucciones de Eufonia (Squid Craft Games, Dedsafio). |
| Eufonia Instructions (fan) | Modrinth | Overlays cinematográficos sincronizados para todos los jugadores. |
| DEDSAFIO 4 Mechanics | Fabric 1.20.1 | Eventos con temporizador, cambios de dificultad, ruletas visuales, monedas, cajeros, correo, waypoints. |
| Dedsafio 2 Recreation | Forge (MCreator) | Items, bloques, bosses de D2. En beta. |
| Dedsafio 2 (Bedrock addon) | CurseForge | Armaduras, espadas, entidades y dungeons de D2. |
| Topos Gulag Dedsafio3 | Forge 1.20.1 | Minijuego "Golpea al Topo"; `/gulag add`. |
| Hubsafio | NeoForge 1.21.1-1.21.2 | Varias mecánicas de la serie. |
| D3Dsafio Missions Menu / RandomSafio | CurseForge | Menú de misiones, notificaciones, crafteos, dificultad, DEDciclopedia. |
| Pedsafio, Ulmesu Challenge | Servidores/modpacks | Servidores de fans inspirados en Dedsafio. |

## 4. Consideraciones legales
- Dedsafio y Eufonia son marcas/obra de sus creadores. Un servidor de fans **sin ánimo de lucro** es habitual, pero:
  - No copies texturas, modelos, música ni logos (muchos mods de fans ya reconocen que no tienen autoría clara de sus assets).
  - Usa nombre propio ("... inspirado en Dedsafio") y assets originales.
  - Si monetizas (tienda, rangos), revisa el EULA de Mojang y la política de uso comercial.
- Revisa la licencia de cada mod/plugin antes de reutilizar su código (algunos son ARR).

## 5. Fuentes
- Eufonia Studio, página de ElDed: https://eufonia.studio/collabs/elded
- Millenium: https://www.millenium.gg/noticias/49705.html (fin de D2)
- MGG: https://www.millenium.gg/noticias/49098.html (D2: horario, ruleta, eventos)
- dedsafiostats (fans): https://www.dedsafiostats.com/faq
- CurseForge: https://www.curseforge.com/minecraft/mc-mods/dedsafio-2-recreation
- CurseForge Bedrock: https://www.curseforge.com/minecraft-bedrock/addons/dedsafio-2
- Modrinth Dedsafio Utils (vía boxtoplay): https://www.boxtoplay.com/en/minecraft-hosting/minecraft-server/modrinth-plugins/dedsafio-53991
- Setsumeisho: https://www.mado-hosting.com/minecraft-mods/setsumeisho
- DEDSAFIO 4 Mechanics: https://www.modpackindex.com/mod/210709/dedsafio4-mechanics
- Topos Gulag: https://www.modpackindex.com/mod/116617/topos-gulag-dedsafio3
- Eufonia Client: https://flathub.org/apps/studio.eufonia.EufoniaClient
