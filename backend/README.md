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
| `DATABASE_URL` | Si | URL R2DBC de Postgres. Ejemplo: `r2dbc:postgresql://HOST:5432/reuam` |
| `DATABASE_USER` | Si | Usuario de la base de datos |
| `DATABASE_PASSWORD` | Si | Password de la base de datos |

Para desarrollo local, el proyecto usa H2 en modo compatible con Postgres:

```yaml
database:
  url: "r2dbc:h2:file:///./reuam;MODE=PostgreSQL;DATABASE_TO_UPPER=false"
  user: "root"
  password: ""
```

Para Cloud SQL PostgreSQL, configura `DATABASE_URL`, `DATABASE_USER` y `DATABASE_PASSWORD` en el runtime donde despliegues el backend.

## Firebase Authentication

La app movil debe iniciar sesion con Google Sign-In usando Firebase Authentication. Luego debe enviar el Firebase ID token en cada endpoint protegido:

```http
Authorization: Bearer <firebase_id_token>
```

Para que el backend valide tokens reales, coloca el archivo de credenciales del Admin SDK en la raiz del proyecto con este nombre:

```text
firebase-adminsdk.json
```

Si el archivo no existe, el servidor arranca, pero las rutas protegidas responden `401 Unauthorized`. Esto permite desarrollo local de rutas publicas sin exponer un modo inseguro.

## Storage De Imagenes

No se guardan imagenes binarias en Postgres. Postgres guarda solo metadata:

- `storagePath`
- `downloadUrl`
- orden de la foto
- relacion con perfil o articulo

Convenciones recomendadas en Firebase Storage:

```text
profiles/{firebaseUid}/avatar.jpg
items/{firebaseUid}/{itemId}/{photoId}.jpg
```

Flujo recomendado para fotos de perfil:

1. La app sube la imagen a Firebase Storage.
2. La app obtiene el `downloadUrl`.
3. La app llama `PATCH /api/v1/profiles/me/photo`.

Flujo recomendado para fotos de articulos:

1. Crear el articulo con `POST /api/v1/items` sin fotos, o con fotos ya subidas.
2. Subir imagenes a `items/{firebaseUid}/{itemId}/{photoId}.jpg`.
3. Actualizar metadata con `PUT /api/v1/items/{id}` enviando `photos`.

Reglas esperadas de Storage:

- Solo usuarios autenticados pueden escribir.
- El usuario solo puede escribir bajo su propio `firebaseUid`.
- Limitar `contentType` a imagenes.
- Limitar tamano maximo por archivo.

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
