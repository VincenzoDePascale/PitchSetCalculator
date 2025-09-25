@echo off
echo Compilazione in corso...
if not exist bin mkdir bin
javac -d bin src/*.java
if errorlevel 1 goto error_compilazione

echo Avvio del programma...
java -cp bin Main
goto end

:error_compilazione
echo Errore durante la compilazione. Controlla il codice sorgente.
pause

:end
echo.
echo Operazione completata.
pause