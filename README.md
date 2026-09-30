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

## Kioscos con Android antiguo (p. ej. Android 7.1.2)
El WebView de esos equipos es de 2017-2018 y no entiende CSS moderno. Síntomas: mapa en blanco, textos pegados/encimados,
botones de zoom arriba en vez de abajo. `index.html` ya no usa `gap` en flexbox ni `inset`, deja `env(safe-area-inset-*)`
solo como mejora opcional, usa eventos táctiles si el WebView no tiene Pointer Events, y muestra en pantalla (franja roja
inferior) cualquier error de JavaScript para poder diagnosticarlo sin depurador.

## Sección "Conoce más de nuestra gran ciudad"
Botón **Conoce más** junto a **Mapa** en la cabecera. Muestra el tríptico (`2026_Santa_Fe_editado.pdf`) **completo**, sin recortes:
pestañas **Pág. 1** (Planta Baja, simbología, términos, B·KidZanian) y **Pág. 2** (Planta Alta, Guardianes, fiestas,
KidZania en el mundo, contacto y socios). Se ve la página entera al abrir; acerca con pellizco o con los botones + / − y
arrastra para moverte. El botón Atrás de Android regresa al mapa y solo después sale de la app.

Las páginas están en `android/app/src/main/assets/www/info/` (`triptico_1.jpg`, `triptico_2.jpg`, 2200 px de ancho).
Se usan imágenes y no el PDF directamente porque los WebView antiguos (Android 7) no pueden mostrar PDFs.
Para actualizar el folleto, reemplaza esos dos `.jpg` (mismo nombre).

## Mapas
`map_baja.jpg` y `map_alta.jpg` incluyen ahora el logo "KidZania Santa Fe" completo (antes se cortaba abajo). Los botones de
zoom del mapa están a media altura del borde derecho para no tapar el logo.

## Servicios, dónde comer/comprar y circulación en el mapa
Además de los lugares numerados, el mapa interactivo marca en ambas plantas los íconos de la leyenda del tríptico:
**Servicios**, **Para comer**, **Para comprar** y **Circulación** (elevador, escaleras, salidas de emergencia).
- Aparecen como pines redondos con su ícono, aparte de los pines numerados, y también en la lista lateral (con chips de filtro y búsqueda).
- Al elegir un servicio de la lista se resaltan **todas** sus ubicaciones en esa planta (p. ej. "Escaleras ×5") y el mapa se ajusta para mostrarlas;
  al tocar un pin se muestra solo ese y cuántas ubicaciones hay.
- Datos: `DATA[planta].pois` (coordenadas en % de la imagen) y `POI_TYPES` (nombre/categoría) en `index.html`; íconos en `www/ico/`.
- Aún no incluidos (no se pidieron): Espectáculos y Ruta de la Independencia, que también tienen íconos en el mapa.
