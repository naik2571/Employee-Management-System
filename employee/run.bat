@echo off
echo =========================================
echo Employee Management System (MySQL + PDF)
echo =========================================

cd ..

echo Compiling...
if not exist bin mkdir bin
javac -d bin -cp "employee;employee\lib\itextpdf.jar;employee\lib\mysql-connector-java.jar;employee\lib\jfreechart.jar;employee\lib\flatlaf.jar;employee\lib\javax.mail.jar;employee\lib\activation.jar" --release 8 employee\*.java

echo Starting Application...
java -cp "bin;employee\lib\itextpdf.jar;employee\lib\mysql-connector-java.jar;employee\lib\jfreechart.jar;employee\lib\flatlaf.jar;employee\lib\javax.mail.jar;employee\lib\activation.jar" employee.LoginGUI

echo.
echo Cleaning up compiled files...
if exist bin rmdir /s /q bin

pause
