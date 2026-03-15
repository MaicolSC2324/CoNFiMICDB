# GUÍA PARA CREAR EJECUTABLE DE ConFiMICDB v1.0

## PASO 1: COMPILAR EL PROYECTO

### En tu computadora de desarrollo:

1. Abre **PowerShell** o **Cmd** como administrador
2. Navega a la carpeta del proyecto:
   ```
   cd C:\Users\USUARIO\OneDrive\Escritorio\Programas\ConFiMICDB
   ```

3. Ejecuta el comando Maven para compilar y empaquetar:
   ```
   mvn clean package -DskipTests
   ```

   Espera a que termine (puede tomar 2-5 minutos)

4. Si todo es correcto, verás el mensaje: `BUILD SUCCESS`

5. El archivo JAR se creará en: `target\ConFiMICDB-v1.0.jar`

## PASO 2: CREAR EL EJECUTABLE .EXE CON LAUNCH4J

### Descargar Launch4J:
1. Ve a: https://sourceforge.net/projects/launch4j/files/
2. Descarga la versión más reciente (ej: `launch4j-3.50-win32.zip`)
3. Descomprime en: `C:\Program Files\launch4j\`

### Crear la configuración .xml para Launch4J:

Crea un archivo llamado `ConFiMICDB.xml` en la carpeta del proyecto:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<launch4jConfig>
  <dontWrapJar>false</dontWrapJar>
  <headerType>gui</headerType>
  <jar>target\ConFiMICDB-v1.0.jar</jar>
  <outfile>ConFiMICDB-v1.0.exe</outfile>
  <errTitle>ConFiMICDB - Error</errTitle>
  <cmdLine></cmdLine>
  <chdir>.</chdir>
  <priority>normal</priority>
  <downloadUrl>https://www.java.com/download</downloadUrl>
  <supportUrl></supportUrl>
  <stayAlive>false</stayAlive>
  <restartOnCrash>false</restartOnCrash>
  <manifest></manifest>
  <icon></icon>
  <classPath>
    <cp>%EXEDIR%</cp>
  </classPath>
  <jre>
    <path>java</path>
    <bundledJre64Bit>false</bundledJre64Bit>
    <bundledJreAsFallback>false</bundledJreAsFallback>
    <minVersion>21</minVersion>
    <maxVersion></maxVersion>
    <jdkPreference>preferJre</jdkPreference>
    <runtimeBits>64/32</runtimeBits>
  </jre>
  <messages>
    <startupErr>Ha ocurrido un error al iniciar la aplicación</startupErr>
    <bundledJreErr>No se encontró Java. Se requiere Java 21 o superior</bundledJreErr>
    <jreVersionErr>Esta aplicación requiere Java 21 o superior</jreVersionErr>
    <launcherErr>Error crítico en la aplicación</launcherErr>
    <instanceAlreadyExistsMsg>La aplicación ya está en ejecución</instanceAlreadyExistsMsg>
  </messages>
  <variables></variables>
  <splash>
    <file></file>
    <waitForWindow>true</waitForWindow>
    <timeout>60</timeout>
    <timeoutErr>false</timeoutErr>
  </splash>
</launch4jConfig>
```

### Generar el .EXE:

1. Abre Launch4J (ejecutable de la carpeta de instalación)
2. Ve a: `File` → `Open` → Selecciona `ConFiMICDB.xml`
3. Click en el botón ▶ (Build)
4. Se generará el archivo `ConFiMICDB-v1.0.exe` en la carpeta del proyecto

## PASO 3: REQUISITOS PARA INSTALAR EN OTROS EQUIPOS

### En el equipo donde se instalará la aplicación:

1. **Java 21 o superior** (obligatorio)
   - Descargar desde: https://www.oracle.com/java/technologies/downloads/#java21
   - O JDK open source: https://adoptium.net/

2. **MySQL Server 8.0 o superior**
   - Descargar desde: https://dev.mysql.com/downloads/mysql/
   - Configurar con un usuario y contraseña conocidos

3. **Base de Datos**
   - Ejecutar el script SQL (confi_db.sql) para crear las tablas

## PASO 4: CONFIGURAR LA APLICACIÓN EN OTRO EQUIPO

### Archivo de configuración (application.properties):

Antes de ejecutar el .exe en otro equipo, crea/edita el archivo:
`application.properties` en la misma carpeta que el .exe

Contenido:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/confi_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=TU_CONTRASEÑA_MYSQL
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
server.port=8080
logging.level.root=ERROR
```

## PASO 5: CREAR CARPETA DE INSTALACIÓN

### En el equipo destino:

1. Crea una carpeta: `C:\Program Files\ConFiMICDB\`
2. Copia los archivos:
   - `ConFiMICDB-v1.0.exe`
   - `application.properties` (con la configuración correcta)
3. Ejecuta el .exe como administrador la primera vez

## ALTERNATIVA: CREAR INSTALADOR CON INNO SETUP

Para crear un instalador profesional (.msi o instalador automático):

1. Descarga Inno Setup: https://www.innosetup.com/
2. Crea un script de instalación
3. Empaqueta el .exe y las dependencias

## TROUBLESHOOTING

### Si sale "No se encontró Java":
- Instala Java 21 en el equipo
- Verifica que `java.exe` esté en `%PATH%`

### Si no se conecta a MySQL:
- Verifica que MySQL esté corriendo
- Checa el usuario y contraseña en `application.properties`
- Asegúrate que la base de datos `confi_db` existe

### Si sale error de permisos:
- Ejecuta como administrador
- Comprueba permisos en la carpeta de instalación

---

**Versión**: 1.0  
**Fecha**: 2026-03-15
