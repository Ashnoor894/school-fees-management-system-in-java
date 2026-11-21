@echo off
setlocal DisableDelayedExpansion
cd /d "%~dp0"

echo ======================================================================
echo   GROUP 15: SCHOOL FEE MANAGEMENT SYSTEM - COMPILATION ^& PACKAGING
echo   CSE2003 Object Oriented Programming using JAVA ^| B.Tech CSE Sem 3
echo ======================================================================
echo.

if not exist bin mkdir bin

echo [Step 1] Compiling Java classes with --release 17 (universal compatibility)...
javac --release 17 -d bin src/fees/*.java src/fees/model/*.java src/fees/exception/*.java src/fees/thread/*.java src/fees/db/*.java src/fees/ui/*.java
if errorlevel 1 (
    echo.
    echo [ERROR] Compilation failed. Please check your Java JDK installation.
    echo.
    pause
    exit /b 1
)

echo [Step 2] Building executable JAR: SchoolFeeManagementSystem.jar...
jar cfe SchoolFeeManagementSystem.jar fees.Main -C bin .
if errorlevel 1 (
    echo.
    echo [ERROR] Packaging failed. Please check your Java JDK installation.
    echo.
    pause
    exit /b 1
)

echo.
echo ======================================================================
echo [SUCCESS] Build completed for Java 17+ (Class version 61.0).
echo Compatible with your Java Runtime (versions up to 69.0).
echo ======================================================================
echo.
pause
