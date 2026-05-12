@ECHO OFF
SETLOCAL

set "BASE_DIR=%~dp0"
set "WRAPPER_DIR=%BASE_DIR%.mvn\wrapper"
set "MAVEN_VERSION=3.9.9"
set "MAVEN_HOME=%WRAPPER_DIR%\apache-maven-%MAVEN_VERSION%"
set "MAVEN_CMD=%MAVEN_HOME%\bin\mvn.cmd"
set "MAVEN_ZIP=%WRAPPER_DIR%\apache-maven-%MAVEN_VERSION%-bin.zip"
set "MAVEN_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip"

IF EXIST "%MAVEN_CMD%" GOTO run_maven

IF NOT EXIST "%WRAPPER_DIR%" mkdir "%WRAPPER_DIR%"

ECHO Downloading Apache Maven %MAVEN_VERSION%...
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "Invoke-WebRequest -Uri '%MAVEN_URL%' -OutFile '%MAVEN_ZIP%'"
IF ERRORLEVEL 1 (
  ECHO Failed to download Maven distribution.
  EXIT /B 1
)

ECHO Extracting Maven...
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "Expand-Archive -Force -Path '%MAVEN_ZIP%' -DestinationPath '%WRAPPER_DIR%'"
IF ERRORLEVEL 1 (
  ECHO Failed to extract Maven distribution.
  EXIT /B 1
)

:run_maven
CALL "%MAVEN_CMD%" %*
EXIT /B %ERRORLEVEL%
