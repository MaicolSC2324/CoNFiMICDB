@echo off
REM Script para verificar requisitos del sistema para ConFiMICDB

title Verificador de Requisitos - ConFiMICDB

echo ========================================
echo Verificador de Requisitos
echo ConFiMICDB v1.0
echo ========================================
echo.

echo Verificando requisitos del sistema...
echo.

REM Verificar Java
echo [1/3] Verificando Java 21+...
java -version >nul 2>&1
if errorlevel 1 (
    echo    ERROR: Java NO está instalado
    echo    Descarga Java desde: https://www.oracle.com/java/technologies/downloads/
    echo    Requiere Java 21 o superior
) else (
    for /f "tokens=*" %%i in ('java -version 2^>^&1') do (
        echo    OK: %%i
        goto java_ok
    )
    :java_ok
)
echo.

REM Verificar Maven (opcional)
echo [2/3] Verificando Maven (opcional)...
mvn --version >nul 2>&1
if errorlevel 1 (
    echo    ADVERTENCIA: Maven NO está instalado
    echo    Necesario si vas a compilar el proyecto
    echo    Descarga desde: https://maven.apache.org/download.cgi
) else (
    echo    OK: Maven está instalado
)
echo.

REM Verificar MySQL
echo [3/3] Verificando MySQL...
mysql --version >nul 2>&1
if errorlevel 1 (
    echo    ADVERTENCIA: MySQL Client NO está instalado
    echo    Requiere MySQL Server 8.0 o superior
    echo    Descarga desde: https://dev.mysql.com/downloads/
    echo.
    echo    Puedes conectarte a MySQL de otras formas:
    echo    - A través de phpMyAdmin
    echo    - A través de MySQL Workbench
) else (
    for /f "tokens=*" %%i in ('mysql --version') do (
        echo    OK: %%i
    )
)
echo.

echo ========================================
echo Resumen:
echo ========================================
echo.
echo REQUISITOS OBLIGATORIOS:
echo   [?] Java 21 o superior
echo.
echo REQUISITOS RECOMENDADOS:
echo   [?] MySQL Server 8.0 o superior
echo.
echo REQUISITOS OPCIONALES (para desarrollo):
echo   [?] Maven 3.8+
echo.
pause
