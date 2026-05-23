@echo off
clean
setlocal

:: 1. Set Environment Variables (Use Java bundled with Android Studio)
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo ========================================
echo  DrugManage: Build and Host Tool
echo ========================================

:: 2. Clean build and create APK
echo [1/3] Building APK...
call gradlew.bat assembleDebug

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [!] Build Failed. Check the error messages above.
    pause
    exit /b %ERRORLEVEL%
)

:: 3. Show Local IP Address for Smartphone Connection
echo.
echo [2/3] Detecting Local IP Address...
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4"') do (
    set "MY_IP=%%a"
    goto :ip_found
)
:ip_found

:: Remove spaces
set "MY_IP=%MY_IP: =%"
echo.
echo ----------------------------------------
echo  Download URL: http://%MY_IP%:8000/app-debug.apk
echo ----------------------------------------
echo.

:: 4. Start Server
echo [3/3] Starting Python server...
cd app\build\outputs\apk\debug
python -m http.server 8000

endlocal
