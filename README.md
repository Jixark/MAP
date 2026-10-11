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

## Sistema "Papás Juegan"
Las actividades del sistema Papás Juegan se marcan con un círculo/pin **partido en amarillo (como el brazalete) y el color de su sección** (p. ej. Estudio de Televisión: amarillo y verde azulado, como Medios de Comunicación) en el mapa, la lista y la tarjeta, y tienen su propio chip de filtro **Papás Juegan**.
La lista está en `PAPAS_JUEGAN` (números de pin por planta) dentro de `index.html`; para agregar o quitar una actividad basta editar esa lista.
Actualmente: Baja → Hospital (Cirugía), Universidad, Restaurante, Pizzería, Supermercado, Fábrica de Pastelillos (Gansito), Autobús Turístico (Turi).
Alta → Purina (Veterinaria), Adidas, Barra de Jugos, Estudio de Arte (Vinci), Estudio de Diseño, Heladería, Disco Lounge, Escuela Culinaria,
Fábrica de Dulces, Fábrica de Cuadernos, Fábrica de Mantequilla, Hamburguesería (McDonald's), Estudio de Televisión (Foro), Barra de Sushi.

**Upgrade de Papás Juegan (*)**: Cuadernos, Gansito (Fábrica de Pastelillos), Escuela Culinaria, Veterinaria (Purina), Adidas y Pizzería
son las actividades incluidas en el **Upgrade de Papás Juegan**. Llevan el mismo círculo partido que el resto del sistema, más un **asterisco**
y, en la tarjeta, el mensaje en amarillo con letras negras "Actividad incluida dentro de tu Upgrade de Papás Juegan".
Se editan en `PJ_EXTRA` / `PJ_EXTRA_POI` (lista) y `PJ_EXTRA_TEXT` (mensaje) dentro de `index.html`.

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
En el kiosko aparece una pestaña **Descarga la app** con hasta dos códigos QR grandes (se dibujan dentro de la app, sin internet):
1. **Abrir el mapa (web):** enlace a la versión web del mapa. Funciona en iPhone y Android, sin instalar nada. *Es la opción recomendada.*
2. **Descargar la app (APK):** enlace al archivo `.apk`. Solo Android; el celular puede pedir permiso para instalar desde ese origen.

La pestaña **solo aparece si hay al menos un enlace**. Se configuran de dos formas:
- En el código (`index.html`): `DOWNLOAD_WEB_URL` y `DOWNLOAD_APK_URL` (valen para todos los kioskos).
- En cada tablet: 5 toques al logo → PIN → campos "Enlace del QR…" (tienen prioridad sobre el código).

**Versión web (para el QR 1):** sube el contenido de `android/app/src/main/assets/www/` tal cual a cualquier hosting (GitHub Pages, Netlify, tu servidor).
Abierta por `http(s)` la página funciona como **versión de visitante**: sin reinicio por inactividad, sin administración y sin pestaña de QR.
Dentro de la app de Android (`file://`) funciona como kiosko. Para forzar el modo kiosko en un navegador agrega `?kiosk` a la dirección.

**APK (para el QR 2):** en Android Studio: *Build > Generate Signed App Bundle / APK > APK*, crea (y guarda a salvo) tu llave de firma, elige **release**.
Sube el `.apk` a un enlace directo (tu hosting o GitHub Releases) y pega ese enlace. Para Google Play se sube el `.aab` (cuenta de desarrollador de pago).
La librería de QR (`qrcode.js`, MIT, Kazuhiko Arase) va incluida en `www/`.
