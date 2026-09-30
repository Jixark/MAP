# Mapa KidZania Santa Fe (Android)

Proyecto Android Studio en `android/`. Abre **esa carpeta** (`android`) como proyecto.
El mapa es un WebView que carga `app/src/main/assets/www/index.html` (funciona sin red).

## Error `ExecutionException cannot be cast to RuntimeException` / `FileAlreadyExistsException ...\.gradle\tmp\...`

No es un error del código de la app: es una carpeta temporal corrupta de Gradle en tu PC
(suele quedar así tras un corte de red o un cierre forzado durante la descarga/sincronización).

1. Cierra Android Studio.
2. En PowerShell:
   ```powershell
   Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force
   Remove-Item -Recurse -Force "$env:USERPROFILE\.gradle\tmp"
   ```
3. Si persiste, borra también la caché del wrapper y del proyecto:
   ```powershell
   Remove-Item -Recurse -Force "$env:USERPROFILE\.gradle\wrapper\dists\gradle-8.14.3-bin"
   Remove-Item -Recurse -Force "<ruta del proyecto>\android\.gradle"
   ```
4. Abre Android Studio → *File > Sync Project with Gradle Files* (necesita internet la primera vez).
5. Si tu carpeta de usuario está en una unidad de red/OneDrive/con antivirus agresivo, define
   `GRADLE_USER_HOME` en una ruta local corta (p. ej. `C:\gradle-home`) y reinicia el IDE.

Este repo ya sube `networkTimeout` del wrapper de 10 s a 120 s para evitar descargas cortadas en redes lentas.

## Cambios en el mapa
- `map_alta.jpg` traía una franja de otra página del PDF en el borde derecho; se recortó a 1250 px de ancho y se recalcularon las posiciones (%) de los pines de Planta Alta.

- `index.html` (la interfaz del mapa) tenía tres fallos que hacían que la app no se viera/usara bien:
  - Al abrir, el panel de la lista se estiraba por el ancho de los chips de categoría y el mapa quedaba con ancho 0 (pantalla sin mapa).
  - Al tocar un pin no se abría su tarjeta (la captura del puntero le robaba el clic).
  - Al elegir un lugar de la lista el mapa se centraba mal y el pin quedaba fuera de pantalla.
  - Además los pines se dibujaban apilados y desplazados respecto a los del mapa; ahora quedan centrados sobre ellos.

## Cómo correrla
**En tu tableta (Samsung SM-T510):** activa *Opciones de desarrollador > Depuración USB*, conéctala por USB, acepta el aviso
en la tableta, elige el dispositivo en la barra superior de Android Studio y pulsa **Run ▶** (`app`).

**En el emulador de la laptop:** *Tools > Device Manager > Create Device* (p. ej. Pixel Tablet, imagen API 34) y **Run ▶**.
