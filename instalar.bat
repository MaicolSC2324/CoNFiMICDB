@echo off
REM Script de instalación de ConFiMICDB
REM Este script debe ejecutarse como administrador

title Instalador de ConFiMICDB v1.0

echo ========================================
echo Instalación de ConFiMICDB v1.0
echo ========================================
echo.

REM Verificar permisos de administrador
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Este script requiere permisos de administrador
    echo Por favor, ejecuta el script como administrador
    pause
    exit /b 1
)

REM Crear carpeta de instalación
set INSTALL_DIR=C:\Program Files\ConFiMICDB
echo Creando directorio de instalación: %INSTALL_DIR%
if not exist "%INSTALL_DIR%" (
    mkdir "%INSTALL_DIR%"
)

REM Copiar archivos
echo.
echo Copiando archivos...
copy "ConFiMICDB-v1.0.exe" "%INSTALL_DIR%\"
copy "application.properties" "%INSTALL_DIR%\" 2>nul || (
    echo ADVERTENCIA: No se encontró application.properties
    echo Crea este archivo manualmente en %INSTALL_DIR%
)

REM Crear acceso directo en escritorio
echo.
echo Creando acceso directo en el escritorio...

REM Crear acceso directo usando PowerShell
powershell -Command "^
$DesktopPath = [Environment]::GetFolderPath('Desktop'); ^
$ShortcutPath = Join-Path -Path $DesktopPath -ChildPath 'ConFiMICDB.lnk'; ^
$TargetPath = 'C:\Program Files\ConFiMICDB\ConFiMICDB-v1.0.exe'; ^
$WshShell = New-Object -ComObject WScript.Shell; ^
$Shortcut = $WshShell.CreateShortcut($ShortcutPath); ^
$Shortcut.TargetPath = $TargetPath; ^
$Shortcut.WorkingDirectory = 'C:\Program Files\ConFiMICDB'; ^
$Shortcut.Description = 'ConFiMICDB - Gestión de Aeronaves'; ^
$Shortcut.Save()
"

echo.
echo ========================================
echo INSTALACION COMPLETADA!
echo ========================================
echo.
echo ConFiMICDB ha sido instalado en:
echo %INSTALL_DIR%
echo.
echo Se ha creado un acceso directo en el Escritorio
echo.
echo IMPORTANTE - ANTES DE USAR:
echo ============================
echo 1. Verifica que tengas Java 21 o superior instalado
echo    Abre PowerShell y ejecuta: java -version
echo.
echo 2. Asegúrate que MySQL esté corriendo
echo.
echo 3. Edita el archivo: %INSTALL_DIR%\application.properties
echo    Con tus datos de conexión a MySQL
echo.
echo 4. La base de datos confi_db debe existir
echo.
echo.
pause
