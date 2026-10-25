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

## Kioskos y "cómo llegar"
Cada tablet sabe en qué kiosko está y puede guiar al visitante hasta el lugar que consulte.

**Configurar cada kiosko (una vez, por tablet):**
1. Toca **5 veces seguidas el logo KidZania** (arriba a la izquierda, en 4 segundos) o mantenlo pulsado 2 segundos.
2. Escribe el PIN de administrador (**2468**, cámbialo en `ADMIN_PIN` dentro de `index.html`).
3. Escribe el nombre del kiosko (p. ej. "Kiosko Barra de Sushi"), pulsa **Colocar en el mapa**, cambia a la planta correcta si hace falta y toca el mapa donde está físicamente el kiosko.
4. La ubicación se guarda en la tablet y se conserva al reiniciar. **Quitar ubicación** desactiva las rutas en esa tablet.

**Sin este paso la pregunta "¿Quieres que te ayude a llegar?" no aparece** (la tablet no sabe dónde está).

**Uso:** el visitante toca un lugar (en el mapa o en la lista) y la tarjeta pregunta **"¿Quieres que te ayude a llegar?"**.
Con **Cómo llegar** se dibuja una línea azul animada desde "Estás aquí" hasta el destino, con pasos, tiempo aproximado y,
si es otra planta, el paso por escaleras o elevador ("Siguiente: Planta Baja"). **Ruta accesible (elevador)** evita escaleras.
Si el lugar tiene varias ubicaciones (baños, escaleras, salidas…) se guía a la más cercana.
Tras 2 minutos sin tocar la pantalla el kiosko vuelve solo a su pantalla inicial (`IDLE_MS`).

**Ajustes en `index.html`:** `GRAPH` (red de pasillos por planta, en píxeles del mapa) y `LINKS` (escaleras/elevador que unen plantas)
definen por dónde se camina; `WALK_PX_PER_MIN` calibra el tiempo estimado.

## Categorías de actividades (listado "Papás Juegan SFE")
Cada establecimiento se clasifica según el color que tiene en el Excel y se marca en el mapa, la lista y la tarjeta con un círculo/pin **partido en dos colores:
el de su categoría + el color de su sección** (p. ej. Estudio de Televisión: amarillo y verde azulado, como Medios de Comunicación).

| Color en el Excel | Marca en el mapa | Leyenda en la tarjeta |
|---|---|---|
| Naranja | amarillo + color de la sección, con asterisco **\*** | "\* Actividad para papá incluida en el Upgrade" |
| Blanco | amarillo + color de la sección | "Actividad para papás: elige las 3 que más te gusten" |
| Rojo | rosa + color de la sección | "Actividad exclusiva para Kals de 3 a 16 años, aplican restricciones" |
| Azul | azul cielo + color de la sección | "Actividades para todos y en todo momento" |

**Filtros y mapa:** al elegir un filtro (**Papás Juegan** = naranja + blanco, **Comer**, **Tiendas**, **Servicios**, **Escaleras y salidas**) el mapa muestra **solo los puntos de esa categoría**;
con **Todos** se ve todo. En la lista de Papás Juegan solo aparece la leyenda del Upgrade; la leyenda de las actividades "elige las 3" (blancas) se ve al tocar cada una.
Las categorías rojas (solo Kals de 3 a 16 años) y azules (para todos) no tienen botón: su leyenda sale al tocar la actividad.
Técnicamente, el mapa original trae los pines dibujados en la imagen; con un filtro activo se usa `map_*_clean.jpg` (la misma imagen sin pines, generada automáticamente: puede
verse alguna mancha leve al acercar mucho) y se dibujan solo los puntos del filtro. Si cambias la imagen original, hay que regenerar la `_clean`.
Los lugares sin color en el Excel o que no aparecen en él (Estudio de Foto, Tienda Departamental, Dulcería, Centro de Exposiciones, Fábrica de Fósforos, Oficina de Pasaportes)
se quedan con su color normal. Para cambiar la categoría de un lugar edita `ACTIVITY_CLASS` (número de pin por planta) y los textos/colores en `CLASSES`, dentro de `index.html`.

## Dónde se vende el Upgrade
El Upgrade de Papás Juegan se vende en la **KidZania Shop · Tienda interior** (Planta Baja, entre los pines 4 y 5); la tienda junto al avión es la
**KidZania Shop · Tienda exterior**. Son dos entradas separadas en la lista ("Para comprar") y se pueden elegir por separado con "Cómo llegar".
La tienda interior muestra "Adquiere aquí tu Upgrade para Papás Juegan", y cada actividad del Upgrade (con asterisco) tiene el botón
"Dónde comprarlo: Tienda interior", que cambia a Planta Baja y abre esa tienda. Datos: `POI_TYPES.kzshop_int` / `kzshop_ext`, `UPGRADE_SHOP_*` en `index.html`.

## Pantalla simplificada y guiada
- **Menos botones:** se quitaron el letrero "Piso …" (ya lo indica el selector de planta) y el aviso de zoom. Los 18 filtros por categoría pasaron a **5 filtros claros**:
  Papás Juegan, Comer, Tiendas, Servicios y Escaleras y salidas (más "Todos"). El resto se encuentra con la lista agrupada o la búsqueda.
  El botón de reinicio del mapa ahora dice **"Ver todo"**.
- **Guía de uso:** una línea arriba del mapa ("Toca un lugar del mapa o de la lista para ver qué es y cómo llegar") que desaparece cuando hay una tarjeta abierta.
- **Aviso del Upgrade de Papás Juegan:** un aviso abajo ("¿Quieres tu Upgrade de Papás Juegan? Se adquiere en la Tienda interior") con el botón
  **Cómo llegar** (o **Ver tienda** si el kiosko no está configurado). **Solo aparece cuando hay un filtro activo y la lista incluye una actividad del Upgrade**:
  en la ventana de **Papás Juegan** (ambas plantas) y, p. ej., en **Comer de Planta Baja** (por la Pizzería); no en la pantalla inicial ni en Comer de Planta Alta.
  Se oculta mientras hay otra tarjeta abierta y vuelve al cerrarla.
- **En cada actividad del Upgrade:** la tarjeta pregunta primero "¿Quieres que te ayude a llegar?" y después "¿Aún no tienes tu Upgrade? Se adquiere en la Tienda interior"
  con el botón **Llévame a la tienda**. Al terminar una ruta a una actividad del Upgrade vuelve a ofrecerlo.
- **Tienda interior:** pregunta "¿Quieres que te ayude a llegar?" y al llegar muestra "Adquiere aquí tu Upgrade para Papás Juegan".
- **Ruta:** el botón principal es **Siguiente / Listo**; "Ruta sin escaleras (elevador)" quedó como enlace discreto.

## Descarga por QR (el visitante se lleva el mapa en su celular)
En las tablets **configuradas como kiosko** (con su ubicación en el mapa) aparece la pestaña **Descarga la app**; la app que se instala un visitante no la muestra ni hace reinicio por inactividad. Muestra hasta dos códigos QR grandes (se dibujan dentro de la app, sin internet):
1. **Abrir el mapa en tu celular (versión web):** iPhone y Android, sin instalar nada. *Es la opción recomendada.*
2. **Descargar la app (APK):** solo Android; el celular puede pedir permiso para instalar desde ese origen.

Si el kiosko ya está configurado pero aún no hay enlaces, la pestaña muestra "Aún no hay enlaces de descarga configurados" con las instrucciones. Los enlaces se ponen de dos formas:
- **En cada tablet:** 5 toques al logo → PIN → campos "Enlace del QR…" (tienen prioridad sobre el código).
- **En el código (`index.html`):** `DOWNLOAD_WEB_URL` y `DOWNLOAD_APK_URL` (valen para todos los kioskos).

### Dónde subir los archivos (los QR solo apuntan a un enlace de internet)
El QR se genera sin internet, pero **el celular del visitante necesita internet (datos móviles o Wi-Fi) para abrir el enlace y descargar**.
- **Versión web:** sube el contenido de la carpeta `android/app/src/main/assets/www/` a cualquier hosting y usa la dirección resultante.
  Lo más fácil y gratis: https://app.netlify.com/drop (arrastra la carpeta `www`; te da una dirección tipo `https://nombre.netlify.app/`).
  Abierta por `http(s)` la página funciona como **versión de visitante**: sin administración, sin reinicio por inactividad y sin pestaña de QR.
  (Para forzar el modo kiosko en un navegador agrega `?kiosk`; dentro de la app de Android `file://` ya es kiosko.)
- **APK:** en Android Studio: *Build > Generate Signed App Bundle / APK > APK*, crea (y guarda a salvo) tu llave de firma, elige **release**.
  Cópiala como `MapaKidZania.apk` en la misma carpeta que subes a Netlify (enlace: `https://nombre.netlify.app/MapaKidZania.apk`) o súbela a
  GitHub: *Releases > Draft a new release* y adjunta el `.apk` (enlace: `https://github.com/Jixark/MAP/releases/download/v1.0/MapaKidZania.apk`; el repositorio es público).
  También sirve el servidor/SharePoint de tu empresa.
- Para Google Play se sube el `.aab` (cuenta de desarrollador de pago).

### Firmar la APK en Android Studio (*Build > Generate Signed App Bundle / APK > APK*)
1. **Key store path:** pulsa **Create new…**, elige una carpeta segura y un nombre (p. ej. `kidzania-mapa.jks`). Pon una contraseña y anótala.
2. **Key alias:** un nombre (p. ej. `kidzania`), con su contraseña. Validez: 25 años (por defecto). En *Certificate* basta con tu nombre y país (MX).
3. **Next → Destination folder →** variante **release**, marca las firmas **V1 y V2 → Create**. El archivo queda en `android/app/release/app-release.apk`; renómbralo `MapaKidZania.apk`.
4. **Guarda el `.jks` y las contraseñas en un lugar seguro (copia de respaldo): si se pierden no podrás publicar actualizaciones de la misma app.** No los subas al repositorio.
5. Los kioskos instalados con *Run ▶* usan otra firma (de depuración): no instales sobre ellos la APK firmada sin desinstalar antes (se perdería la ubicación guardada del kiosko).

### Sin internet para el visitante
Un archivo guardado dentro del kiosko solo se puede descargar si el celular del visitante llega al kiosko por la red (mismo Wi-Fi, sin aislamiento de clientes).
Eso requiere un pequeño servidor dentro de la app y una red Wi-Fi propia; **no está implementado**. La librería de QR (`qrcode.js`, MIT, Kazuhiko Arase) va incluida en `www/`.

## Kals de 1 a 3 años (Vecindario) y cambios en Planta Alta
- **Filtro "Kals de 1 a 3 años":** muestra en el mapa y la lista solo las actividades del folleto del Vecindario (Baja: 13, Alta: 10), con la nota del folleto
  ("Todas las actividades están sujetas a disponibilidad. Las de Puntos de Venta Interactivos tienen costo extra. Siempre con la ayuda de un adulto."). Cada una lleva la etiqueta
  "Kals de 1 a 3 años" en su tarjeta. Se edita en `INFANT` / `INFANT_POI` (número de pin por planta), `INFANT_NOTE` y `INFANT_NAME` dentro de `index.html`.
- **Vecindario (Planta Baja):** pin 29 "Vecindario" (categoría Residencial) en el centro de la zona, incluido en el filtro de 1 a 3 años (Baja: 14 actividades).
- **Planta Alta:** el pin 18 (antes "Ducto de Aire Acondicionado") ahora es **Fábrica de Apapachos**, y se agregó **Taller de Elixir** (pin 6, en el hueco de la numeración entre Adidas y Purina).
- El aviso del Upgrade ("¿Quieres tu Upgrade…?") aparece solo en el filtro **Papás Juegan**.

## Actividades de temporada (La Ciudad del Más Allá)
Filtro **Actividades de temporada**: muestra solo los lugares que participan en la dinámica de temporada, con pin/círculo en **morado oscuro** (tiene prioridad sobre las demás marcas, también en **Todos**).
**Solo dentro de este filtro** la lista y las tarjetas hablan de las **actividades**, no de los establecimientos: cada actividad es una fila ("Taller de donas", "Sensorama", "Teatro de sombras", "Obra de teatro", "Elabora tu elixir", "Taller de apapachos", "Tatuaje", "Crepa")
con "Se encuentra en: <lugar>"; al tocarla la tarjeta indica dónde está y qué otras actividades hay ahí. En el mapa hay un pin por lugar. En **Todos** y los demás filtros los lugares se ven con su nombre normal (la tarjeta del Teatro, por ejemplo, dice "Actividad: Teatro de sombras, Sensorama y Obra de teatro").
| Lugar | Planta | Actividades |
|---|---|---|
| Teatro | Baja | Teatro de sombras, Sensorama y Obra de teatro |
| Restaurante | Baja | Taller de donas |
| Taller de Elixir | Alta | Elabora tu elixir |
| Fábrica de Apapachos | Alta | Taller de apapachos |
| Tatuajes | Alta | Tatuaje |
| Café | Alta | Crepa |
Se edita en `SEASON` y `SEASON_POI` (lugares y actividades), `SEASON_NAME`, `SEASON_COLOR` y `SEASON_NOTE`, dentro de `index.html`. Para terminar la temporada, vacía esas listas (el filtro desaparece solo).
