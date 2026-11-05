# Despliegue en Proxmox sin internet (Paper 26.3, Java 25)

Compila y prepara todo en un PC con internet; el Proxmox solo ejecuta el servidor.

## 1. En el PC (con internet)
1. Instala **JDK 25** (Temurin) y **Gradle 9.1+**.
2. `cd plugin && gradle build` -> `plugin/build/libs/dedsafio-core-0.1.0.jar`
3. Descarga **Paper 26.3** de https://papermc.io/downloads/paper (si 26.3 aun es experimental, activa
   "experimental builds") y renombralo `paper.jar`.
4. Descarga **Temurin JDK 25 Linux x64 (.tar.gz)** para el contenedor.
5. Primer arranque con internet (Paper descarga el jar de Mojang y librerias):
   ```bash
   mkdir ~/dedsafio-run && cd ~/dedsafio-run
   cp paper.jar . && mkdir plugins && cp dedsafio-core-0.1.0.jar plugins/
   java -jar paper.jar --nogui      # pon eula=true en eula.txt (aceptas el EULA de Mojang)
   java -jar paper.jar --nogui      # espera "Done" y escribe: stop
   ```
   Revisa el log: el plugin debe cargar sin errores.

## 2. Contenedor en Proxmox
- Sube una plantilla Debian 12 (`.tar.zst`) a *CT Templates* (no hay internet para descargarla).
- Crea el CT: 4 cores, 6 GB RAM, 20 GB disco, IP fija, unprivileged.

## 3. Copiar archivos
```bash
scp -r ~/dedsafio-run root@IP:/opt/dedsafio
scp OpenJDK25*.tar.gz root@IP:/opt/
# en el CT:
cd /opt && tar xzf OpenJDK25*.tar.gz && mv jdk-25* java25
/opt/java25/bin/java -version
useradd -r -m -d /opt/dedsafio minecraft 2>/dev/null; chown -R minecraft:minecraft /opt/dedsafio
```

## 4. server.properties (pruebas sin internet)
```properties
online-mode=false     # SOLO pruebas: sin internet no se puede autenticar con Mojang
white-list=true
enforce-whitelist=true
difficulty=hard
spawn-protection=0
view-distance=10
simulation-distance=8
max-players=100
```
**Aviso:** con `online-mode=false` los UUID son distintos de los reales. `plugins/DedsafioCore/data.yml`
guarda todo por UUID: al pasar a produccion con `online-mode=true`, **borra ese archivo**.

## 5. Servicio systemd
```bash
cp server/systemd/minecraft.* /etc/systemd/system/
systemctl daemon-reload && systemctl enable --now minecraft.socket minecraft.service
journalctl -u minecraft -f
echo "dedsafio status" > /run/minecraft.stdin
```
Actualizar el plugin: copia el `.jar` nuevo a `/opt/dedsafio/plugins/` y `systemctl restart minecraft`.

## 6. Acceso de jugadores
Los jugadores necesitan **Minecraft Java 26.3**. Desde fuera de tu red necesitas port forwarding (25565),
un VPS con tunel (frp/WireGuard) o hosting para el dia del evento.
