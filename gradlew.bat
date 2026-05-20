@rem Gradle startup script for Windows
@echo off
setlocal
set DIRNAME=%~dp0
"%DIRNAME%\gradle\wrapper\gradle-wrapper.jar" %*
if errorlevel 1 goto fail
goto end
:fail
gradle %*
:end
