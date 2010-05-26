
REM Demo setting the prefix to $

javac -classpath .;eclipselink.jar;jpa.jar;qualifier.jar -s generated -proc:only -Aeclipselink.canonicalmodel.prefix=$ model/*.java

pause
