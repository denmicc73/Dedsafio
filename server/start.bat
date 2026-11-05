@echo off
REM Arranque manual de Paper (Windows). Requiere Java 25. Renombra el jar a paper.jar
java -Xms4G -Xmx4G -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -jar paper.jar --nogui
pause
