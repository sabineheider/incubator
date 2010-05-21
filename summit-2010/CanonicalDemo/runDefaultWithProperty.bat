
REM Demo setting the prefix to $

javac -processor org.eclipse.persistence.internal.jpa.modelgen.CanonicalModelProcessor -classpath .;eclipselink.jar;service.jar;jpa.jar -s generated -proc:only -Aeclipselink.canonicalmodel.prefix=$ model/*.java

pause
