@echo off
setlocal
cd /d "%~dp0"

echo [1/2] Checking build compatibility...
if not exist "SchoolFeeManagementSystem.jar" (
    echo Compiling Java source files for Java 17+...
    if not exist bin mkdir bin
    javac --release 17 -d bin src/fees/*.java src/fees/model/*.java src/fees/exception/*.java src/fees/thread/*.java src/fees/db/*.java src/fees/ui/*.java
    if errorlevel 1 (
        echo Compilation failed. Please make sure JDK is installed.
        pause
        exit /b 1
    )
    jar cfe SchoolFeeManagementSystem.jar fees.Main -C bin .
)

echo [2/2] Launching School Fee Management System...
start javaw -jar SchoolFeeManagementSystem.jar
exit
