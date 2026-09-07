@rem Standard Gradle wrapper launcher script (Windows).
@rem If Android Studio reports the wrapper jar is missing, open this project
@rem in Android Studio and accept its prompt to regenerate the Gradle wrapper.

@echo off
set DIRNAME=%~dp0
set APP_HOME=%DIRNAME%

if defined JAVA_HOME (
    set JAVA_EXE=%JAVA_HOME%\bin\java.exe
) else (
    set JAVA_EXE=java.exe
)

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

"%JAVA_EXE%" -Xmx64m -Xms64m -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
