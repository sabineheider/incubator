
REM Demo with no extra settings (default)

REM javac -classpath .;eclipselink.jar;jpa.jar;qualifier.jar -proc:only -s generated model/*.java

javac -classpath .;eclipselink.jar;jpa.jar;qualifier.jar -s generated model/*.java

pause