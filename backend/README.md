# ReUAM Backend

REST API para ReUAM, una aplicacion academica para reutilizar, intercambiar, donar, prestar o vender simbolicamente articulos dentro de la comunidad UAM.

## Ejecutar Localmente

El proyecto usa Ktor, Kotlin, Exposed R2DBC y kotlinx.serialization.

```bash
sh ./gradlew test
sh ./gradlew run
```

Si prefieres usar `./gradlew` directamente:

```bash
chmod +x gradlew
./gradlew run
```

El servidor escucha por defecto en:

```text
http://localhost:8080
```

## Variables De Entorno

El backend lee primero variables de entorno y, si no existen, usa los valores de `src/main/resources/application.yaml`.

| Variable | Requerida en produccion | Descripcion |
| --- | --- | --- |
| `PGHOST` | Si, si usas Railway PostgreSQL | Host del servicio PostgreSQL |
| `PGPORT` | Si, si usas Railway PostgreSQL | Puerto del servicio PostgreSQL |
| `PGDATABASE` | Si, si usas Railway PostgreSQL | Nombre de la base de datos |
| `PGUSER` | Si, si usas Railway PostgreSQL | Usuario de la base de datos |
| `PGPASSWORD` | Si, si usas Railway PostgreSQL | Password de la base de datos |
| `DATABASE_URL` | Alternativa | URL R2DBC o URL PostgreSQL. Ejemplo: `r2dbc:postgresql://HOST:5432/reuam` o `postgresql://USER:PASSWORD@HOST:5432/DB` |
| `DATABASE_USER` | Alternativa | Usuario de la base de datos cuando usas `DATABASE_URL` sin credenciales |
| `DATABASE_PASSWORD` | Alternativa | Password de la base de datos cuando usas `DATABASE_URL` sin credenciales |
| `LOCAL_STORAGE_DIR` | No | Carpeta local donde se guardan/leen imagenes. Por defecto: `./storage` |
| `LOCAL_STORAGE_PUBLIC_BASE_URL` | No | URL publica que Android usa para leer imagenes locales. Por defecto: `http://10.0.2.2:8080` |
| `FIREBASE_ADMIN_JSON` | Si | Contenido completo del JSON del Firebase Admin SDK. En local tambien puedes usar `firebase-adminsdk.json`. |

Para desarrollo local, el proyecto usa H2 en modo compatible con Postgres:

```yaml
database:
  url: "r2dbc:h2:file:///./reuam;MODE=PostgreSQL;DATABASE_TO_UPPER=false"
  user: "root"
  password: ""
```

Para Cloud SQL PostgreSQL o Railway PostgreSQL, configura `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER` y `PGPASSWORD` en el runtime donde despliegues el backend. Si usas `DATABASE_URL` como `postgresql://USER:PASSWORD@HOST:PORT/DB`, el backend tambien la convierte automaticamente a R2DBC.

## Deploy En Railway

El repo incluye `railway.toml` en la raiz. Railway ejecuta el backend desde la carpeta `backend`, construye con:

```bash
./gradlew installDist
```

Y arranca con el puerto dinamico de Railway:

```bash
./build/install/reuam-app/bin/reuam-app -host=0.0.0.0 -port=$PORT
```

Variables recomendadas para Railway con un servicio PostgreSQL conectado:

```text
PGHOST=${{Postgres.PGHOST}}
PGPORT=${{Postgres.PGPORT}}
PGDATABASE=${{Postgres.PGDATABASE}}
PGUSER=${{Postgres.PGUSER}}
PGPASSWORD=${{Postgres.PGPASSWORD}}
LOCAL_STORAGE_DIR=/data/storage
FIREBASE_ADMIN_JSON={...contenido del JSON de Firebase Admin SDK...}
```

El backend prioriza esas variables `PG*`. Si prefieres una sola variable, tambien puedes usar:

```text
DATABASE_URL=${{Postgres.DATABASE_URL}}
```

El backend acepta URLs `postgresql://...`, `postgres://...`, `jdbc:postgresql://...` y `r2dbc:postgresql://...`.

Si usas H2 temporalmente con un Railway Volume en `/data`, usa una ruta absoluta:

```text
DATABASE_URL=r2dbc:h2:file:/data/reuam;MODE=PostgreSQL;DATABASE_TO_UPPER=false
DATABASE_USER=root
DATABASE_PASSWORD=
```

Para imagenes, agrega un Railway Volume montado en:

```text
/data
```

Con ese volume, `LOCAL_STORAGE_DIR=/data/storage` hace que las fotos sobrevivan redeploys. Si no defines `LOCAL_STORAGE_PUBLIC_BASE_URL`, el backend usa automaticamente:

```text
https://${RAILWAY_PUBLIC_DOMAIN}
```

Luego instala Android debug apuntando al dominio publico:

```powershell
.\gradlew.bat :mobile-app:installDebug -PREUAM_BASE_URL=https://TU-DOMINIO.up.railway.app/api/v1/
```

## Firebase Authentication

La app movil debe iniciar sesion con Google Sign-In usando Firebase Authentication. Luego debe enviar el Firebase ID token en cada endpoint protegido:

```http
Authorization: Bearer <firebase_id_token>
```

Para que el backend valide tokens reales, coloca el archivo de credenciales del Admin SDK en la raiz del proyecto con este nombre:

```text
firebase-adminsdk.json
```

En Railway no subas ese archivo al repo. Usa `FIREBASE_ADMIN_JSON` con el contenido completo del JSON. Si no existe ni el archivo local ni la variable, el servidor arranca, pero las rutas protegidas responden `401 Unauthorized`. Esto permite desarrollo local de rutas publicas sin exponer un modo inseguro.

## Almacenamiento Local De Imagenes

No se guardan imagenes binarias en Postgres. Postgres guarda solo metadata:

- `storagePath`
- `downloadUrl`
- orden de la foto
- relacion con perfil o articulo

El backend sirve archivos desde una carpeta local. Por defecto, esa carpeta es:

```text
backend/storage
```

Y se publica en:

```text
http://10.0.2.2:8080/storage
```

`10.0.2.2` es el alias que usa el emulador de Android para entrar al `localhost` de la computadora. Si usas un celular fisico, cambia `LOCAL_STORAGE_PUBLIC_BASE_URL` por la IP local de tu computadora, por ejemplo `http://192.168.1.50:8080`.

Convenciones recomendadas para rutas locales:

```text
profiles/{firebaseUid}/avatar.jpg
items/{firebaseUid}/{itemId}/{photoId}.jpg
```

Flujo recomendado para fotos de perfil:

1. Guarda la imagen dentro de `backend/storage/profiles/{firebaseUid}/avatar.jpg`.
2. La app llama `PATCH /api/v1/profiles/me/photo` con `photoUrl: "profiles/{firebaseUid}/avatar.jpg"`.
3. El backend convierte esa ruta en `http://10.0.2.2:8080/storage/profiles/{firebaseUid}/avatar.jpg`.

Flujo recomendado para fotos de articulos:

1. Crear el articulo con `POST /api/v1/items` sin fotos, o con fotos ya subidas.
2. Guarda las imagenes dentro de `backend/storage/items/{firebaseUid}/{itemId}/`.
3. Actualizar metadata con `PUT /api/v1/items/{id}` enviando `photos`, por ejemplo:

```json
{
  "photos": [
    {
      "storagePath": "items/UID/ITEM_ID/foto1.jpg",
      "sortOrder": 0
    }
  ]
}
```

Si `downloadUrl` viene vacio, el backend lo completa usando `storagePath`. Si mandas un `downloadUrl` `http://` o `https://`, se conserva para no romper datos viejos.

## Convenciones De API

Base path:

```text
/api/v1
```

IDs:

- Los IDs del dominio son UUIDs.
- `firebaseUid` solo identifica al usuario en Firebase Auth.
- `UserProfile.id` es un UUID interno de ReUAM.

Headers recomendados:

```http
Content-Type: application/json
Accept: application/json
Authorization: Bearer <firebase_id_token>
```

Endpoints publicos principales:

- `GET /`
- `GET /api/v1/health`
- `GET /api/v1/categories`
- `GET /api/v1/items`
- `GET /api/v1/items/{id}`

Endpoints protegidos principales:

- `GET /api/v1/profiles/me`
- `PUT /api/v1/profiles/me`
- `PATCH /api/v1/profiles/me/photo`
- `POST /api/v1/items`
- `POST /api/v1/uploads/item-photo`
- `PUT /api/v1/items/{id}`
- `DELETE /api/v1/items/{id}`
- `POST /api/v1/exchange-requests`
- `GET /api/v1/exchange-requests/sent`
- `GET /api/v1/exchange-requests/received`

## Errores

Todos los errores de negocio y validacion usan el mismo formato:

```json
{
  "code": "validation_error",
  "detail": "There was an error validating your request",
  "field_errors": {
    "title": "Field is required"
  }
}
```

Codigos comunes:

| Code | Uso |
| --- | --- |
| `validation_error` | Campos invalidos o faltantes |
| `bad_request` | Request mal formado o regla invalida |
| `unauthorized` | Falta autenticacion |
| `forbidden` | Usuario autenticado sin permisos |
| `not_found` | Recurso no encontrado |
| `conflict` | Estado incompatible, por ejemplo solicitar tu propio articulo |
| `internal_server_error` | Error no controlado |

## OpenAPI

La documentacion OpenAPI se sirve en:

```text
http://localhost:8080/openapi
```

Tambien existe el archivo base:

```text
src/main/resources/documentation.yaml
```

Las rutas tienen metadata OpenAPI en codigo usando `describe { }`, dentro de:

```text
src/main/kotlin/routes/ReuamRoutes.kt
```

Ese esquema puede usarse para generar modelos y clientes Retrofit en la app Android.
