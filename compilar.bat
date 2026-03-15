@echo off
REM Script para compilar ConFiMICDB
REM Asegúrate de estar en la carpeta raíz del proyecto

echo ========================================
echo Compilando ConFiMICDB v1.0
echo ========================================
echo.

REM Verificar si Maven está instalado
mvn --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Maven no está instalado o no está en PATH
    echo Descarga Maven desde: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

REM Limpiar compilaciones anteriores
echo Limpiando compilaciones anteriores...
call mvn clean

REM Compilar el proyecto
echo.
echo Compilando el proyecto...
call mvn package -DskipTests

REM Verificar si la compilación fue exitosa
if errorlevel 1 (
    echo.
    echo ERROR: La compilación falló
    pause
    exit /b 1
)

echo.
echo ========================================
echo COMPILACION EXITOSA!
echo ========================================
echo.
echo El archivo JAR se encuentra en:
echo target\ConFiMICDB-v1.0.jar
echo.
echo Próximo paso: Usar Launch4J para crear el .exe
echo.
pause
