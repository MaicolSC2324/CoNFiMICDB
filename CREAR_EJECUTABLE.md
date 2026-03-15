# RESUMEN: CÓMO CREAR EJECUTABLE DE CONFIMICDB V1.0

## 📋 RESUMEN EJECUTIVO

Para convertir tu aplicación ConFiMICDB en un ejecutable (.exe) instalable, necesitas seguir 5 pasos principales:

1. **Compilar el proyecto** con Maven
2. **Crear el .exe** con Launch4J
3. **Preparar archivos de instalación**
4. **Configurar en equipos destino**
5. **Distribuir la aplicación**

---

## 🔧 PASO 1: COMPILAR EL PROYECTO

### Requisitos previos:
- Java 21 JDK (no JRE) instalado
- Maven 3.8 o superior
- Carpeta del proyecto CloneFiMICDB

### Pasos:

1. **Abre PowerShell como Administrador**

2. **Navega a la carpeta del proyecto:**
   ```powershell
   cd "C:\Users\USUARIO\OneDrive\Escritorio\Programas\ConFiMICDB"
   ```

3. **Ejecuta la compilación:**
   ```powershell
   mvn clean package -DskipTests
   ```
   
   O simplemente:
   ```
   compilar.bat
   ```

4. **Espera a que finalice** (2-5 minutos)

5. **Si ves `BUILD SUCCESS`**, el JAR está listo en:
   ```
   target\ConFiMICDB-v1.0.jar
   ```

**NOTA:** Si falla, verifica:
- [ ] Java 21 está instalado: `java -version`
- [ ] Maven está en PATH: `mvn --version`
- [ ] Tienes todos los archivos del proyecto

---

## 🎁 PASO 2: CREAR EL .EXE CON LAUNCH4J

### Descargar Launch4J:

1. Ve a: https://sourceforge.net/projects/launch4j/files/
2. Descarga: `launch4j-3.50-win32.zip` (o versión más reciente)
3. Descomprime en: `C:\Program Files\launch4j\`
4. Ejecuta: `launch4j.exe`

### Generar el ejecutable:

1. En Launch4J abierto, ve a: **File** → **Open**
2. Selecciona: `ConFiMICDB.xml` (está en la raíz del proyecto)
3. Click en el botón ▶ (Build)
4. Espera 30 segundos
5. Se creará: `ConFiMICDB-v1.0.exe` en la raíz del proyecto

✅ **¡El .exe está listo!**

---

## 📦 PASO 3: PREPARAR ARCHIVOS DE INSTALACIÓN

### Copia los siguientes archivos a una carpeta de distribución:

```
Carpeta_Distribucion\
├── ConFiMICDB-v1.0.exe
├── application.properties
├── instalar.bat
├── verificar_requisitos.bat
├── README.txt
└── REQUISITOS.txt
```

**Contenido de README.txt:**
```
INSTALACIÓN DE CONFIMICDB V1.0

1. Ejecuta: verificar_requisitos.bat
2. Verifica que tengas:
   - Java 21 o superior
   - MySQL 8.0 o superior

3. Edita: application.properties
   - Cambia el usuario/contraseña de MySQL
   - Verifica el puerto de MySQL (por defecto 3306)

4. Ejecuta: instalar.bat como administrador

5. Se creará un acceso directo en el Escritorio
```

---

## 🖥️ PASO 4: CONFIGURAR EN EQUIPOS DESTINO

### En el equipo donde se instalará:

#### A) Instalar Java 21:
- Descarga desde: https://www.oracle.com/java/technologies/downloads/#java21
- Selecciona **Windows x64**
- Instala en: `C:\Program Files\Java\jdk-21...`
- Verifica: Abre PowerShell y ejecuta `java -version`

#### B) Instalar MySQL 8.0:
- Descarga desde: https://dev.mysql.com/downloads/mysql/
- Instala con usuario `root` y contraseña segura
- Nota: El host (dirección IP), usuario y contraseña

#### C) Crear la base de datos:
- Abre MySQL Workbench o phpMyAdmin
- Crea la base de datos `confi_db`
- Ejecuta el script SQL con las tablas

#### D) Configurar application.properties:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/confi_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=TU_CONTRASEÑA
```

#### E) Ejecutar la instalación:
1. Copia la carpeta de distribución
2. Abre `verificar_requisitos.bat`
3. Ejecuta `instalar.bat` como **Administrador**
4. Se creará un acceso directo en el Escritorio

---

## 🚀 PASO 5: DISTRIBUIR LA APLICACIÓN

### Opción A: Carpeta Comprimida (Simple)
```
zip -r ConFiMICDB-v1.0.zip Carpeta_Distribucion\
```
Envía el .zip a los usuarios

### Opción B: Crear Instalador Profesional (Avanzado)

Si quieres crear un instalador automático (.msi):

1. Descarga **Inno Setup**: https://www.innosetup.com/
2. Crea un script de instalación
3. Compila para generar el instalador

---

## ⚠️ REQUISITOS PARA USUARIOS FINALES

| Requisito | Mínimo | Recomendado | Descarga |
|-----------|--------|-----------|----------|
| Java | 21 JRE | 21 JDK | https://www.oracle.com/java/ |
| MySQL | 8.0 | 8.4 | https://dev.mysql.com/ |
| RAM | 4 GB | 8 GB | - |
| Almacenamiento | 2 GB | 5 GB | - |
| SO | Windows 10 | Windows 11 | - |

---

## 🔍 SOLUCIÓN DE PROBLEMAS

### Error: "Java no encontrado"
**Solución:**
- Instala Java 21 desde https://www.oracle.com/java/
- Añade Java a las variables de entorno PATH
- Reinicia la aplicación

### Error: "No se puede conectar a MySQL"
**Solución:**
- Verifica que MySQL esté corriendo
- Edita `application.properties` con datos correctos
- Verifica usuario/contraseña/puerto

### Error: "Base de datos no existe"
**Solución:**
- Abre MySQL Workbench o phpMyAdmin
- Crea la base de datos `confi_db`
- Ejecuta el script SQL de tablas

### Error: "Puerto 8080 en uso"
**Solución:**
En `application.properties`, cambia:
```properties
server.port=8081
```

---

## 📝 ARCHIVOS CREADOS EN EL PROYECTO

Los siguientes archivos fueron creados para facilitar el proceso:

1. **BUILD_EJECUTABLE.md** - Guía detallada (este archivo)
2. **compilar.bat** - Script automático de compilación
3. **ConFiMICDB.xml** - Configuración para Launch4J
4. **instalar.bat** - Script de instalación para usuarios
5. **verificar_requisitos.bat** - Verifica requisitos del sistema
6. **application.properties.example** - Plantilla de configuración

---

## ✅ CHECKLIST FINAL

Antes de distribuir:

- [ ] El .exe ejecuta sin errores
- [ ] Se conecta correctamente a MySQL
- [ ] Todas las funciones operan correctamente
- [ ] Se creó carpeta de distribución
- [ ] Incluye application.properties configurado
- [ ] Incluye scripts de instalación
- [ ] Probaste en otro equipo (sin desarrollo instalado)
- [ ] Documentación clara para usuarios

---

## 📞 SOPORTE

Si encuentras problemas:

1. Ejecuta `verificar_requisitos.bat`
2. Revisa los logs en la consola
3. Verifica `application.properties`
4. Asegúrate que MySQL esté corriendo
5. Intenta con `java -jar ConFiMICDB-v1.0.jar` en PowerShell

---

**Versión:** 1.0  
**Fecha:** 2026-03-15  
**Aplicación:** ConFiMICDB - Gestión de Confiabilidad de Aeronaves
