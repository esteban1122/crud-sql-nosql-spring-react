# CRUD de productos con MySQL y MongoDB

Aplicación full stack para gestionar productos mediante dos alternativas de persistencia intercambiables:

- **SQL / MySQL:** persistencia relacional implementada con `JdbcTemplate`.
- **NoSQL / MongoDB:** persistencia documental implementada con `MongoTemplate`.

El backend está construido con Spring Boot y mantiene una arquitectura hexagonal: ambos adaptadores implementan el mismo contrato CRUD (`ProductRepository`). La alternativa MongoDB incorpora, además, cuatro consultas filtradas resueltas con pipelines de `aggregate`. El frontend React permite elegir la persistencia y realizar las operaciones desde una sola interfaz.

## Tecnologías

- Java 21 y Spring Boot 4
- MySQL 8 (compatible con XAMPP)
- MongoDB Community Server
- React 19 y Vite
- Node.js 20 o superior

## Estructura del repositorio

```text
Spring_boot_crud/     Backend Spring Boot
  dbs/                Scripts de inicialización de MySQL y MongoDB
  src/main/java/      Dominio, servicios, puertos, controladores y adaptadores
react_crud/           Frontend React
```

## Requisitos previos

Antes de iniciar, debe estar disponible lo siguiente:

1. **JDK 21**. Compruebe la instalación con `java -version`.
2. **MySQL** iniciado y accesible en el puerto `3306`.
3. **MongoDB** iniciado y accesible en `127.0.0.1:27017`.
4. **Node.js 20 o superior**, con npm disponible.

## 1. Inicializar MySQL

Ejecute el script [`Spring_boot_crud/dbs/productoscruddb.sql`](Spring_boot_crud/dbs/productoscruddb.sql) en MySQL Workbench, conectado al servidor MySQL.

El script crea la base de datos `productoscruddb`, la tabla `products` y tres registros iniciales. También puede ejecutarse desde una terminal con el cliente de MySQL:

```powershell
cmd /c "mysql -u root -p < Spring_boot_crud\dbs\productoscruddb.sql"
```

## 2. Inicializar MongoDB

Desde la raíz del repositorio, ejecute:

```powershell
mongosh --file .\Spring_boot_crud\dbs\productoscrudmongo.js
```

El script crea la base `productoscrudmongo`, la colección `products` y los mismos tres registros de ejemplo. Es seguro ejecutarlo más de una vez: solo inserta los datos iniciales si la colección está vacía.

## 3. Configurar la conexión (si es necesario)

Sin variables adicionales, el backend utiliza esta configuración:

| Variable | Valor predeterminado |
| --- | --- |
| `DB_HOST` | `localhost` |
| `DB_PORT` | `3306` |
| `DB_NAME` | `productoscruddb` |
| `DB_USER` | `root` |
| `DB_PASSWORD` | vacío |
| `MONGO_URI` | `mongodb://127.0.0.1:27017/productoscrudmongo` |

Si su instalación usa otro puerto, usuario o contraseña, defina las variables **en la misma terminal** antes de iniciar el backend. Ejemplo:

```powershell
$env:DB_PORT = "3307"
$env:DB_USER = "root"
$env:DB_PASSWORD = "mi_contrasena"
$env:MONGO_URI = "mongodb://127.0.0.1:27017/productoscrudmongo"
```

No incluya contraseñas reales en archivos versionados ni en el repositorio remoto.

## 4. Ejecutar el backend

Abra una terminal en la raíz del repositorio y ejecute:

```powershell
cd .\Spring_boot_crud
.\mvnw.cmd spring-boot:run
```

El backend quedará disponible en:

```text
http://127.0.0.1:8081
```

Como comprobación, abra en otra terminal:

```powershell
Invoke-RestMethod http://127.0.0.1:8081/api/sql/productos
Invoke-RestMethod http://127.0.0.1:8081/api/mongo/productos
```

Ambos comandos deben devolver los productos iniciales de su respectiva base de datos.

## 5. Ejecutar el frontend

Abra una segunda terminal desde la raíz del repositorio y ejecute:

```powershell
cd .\react_crud
npm install
npm run dev
```

Abra en el navegador la dirección que Vite muestre en la terminal; normalmente es:

```text
http://127.0.0.1:5173
```

En la aplicación, use el menú superior para cambiar entre **SQL · MySQL** y **NoSQL · MongoDB**. Cada opción permite crear, listar, editar y eliminar productos de su propia base de datos.

## API REST

### CRUD SQL / MySQL

| Operación | Método y ruta |
| --- | --- |
| Listar | `GET /api/sql/productos` |
| Consultar por id | `GET /api/sql/productos/{id}` |
| Crear | `POST /api/sql/productos` |
| Actualizar | `PUT /api/sql/productos/{id}` |
| Eliminar | `DELETE /api/sql/productos/{id}` |

### CRUD NoSQL / MongoDB

| Operación | Método y ruta |
| --- | --- |
| Listar | `GET /api/mongo/productos` |
| Consultar por id | `GET /api/mongo/productos/{id}` |
| Crear | `POST /api/mongo/productos` |
| Actualizar | `PUT /api/mongo/productos/{id}` |
| Eliminar | `DELETE /api/mongo/productos/{id}` |

Para crear o actualizar, use un cuerpo JSON con esta estructura:

```json
{
  "name": "Producto de ejemplo",
  "price": 75000,
  "description": "Descripción del producto"
}
```

## Filtros MongoDB con `aggregate`

La persistencia MongoDB incorpora cuatro métodos de listado filtrado. Cada respuesta contiene dos propiedades: `productos`, con el resultado, y `pipeline`, con el código del pipeline aplicado. El frontend presenta ambos elementos al ejecutar un filtro.

| Filtro | Ruta |
| --- | --- |
| Nombre contiene texto | `GET /api/mongo/productos/filtros/nombre?texto=mouse` |
| Descripción contiene texto | `GET /api/mongo/productos/filtros/descripcion?texto=usb` |
| Precio mayor o igual | `GET /api/mongo/productos/filtros/precio-minimo?minimo=50000` |
| Rango de precios | `GET /api/mongo/productos/filtros/rango-precio?minimo=40000&maximo=100000` |

