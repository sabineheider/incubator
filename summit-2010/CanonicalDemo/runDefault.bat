
REM Demo with no extra settings (default)

javac -processor org.eclipse.persistence.internal.jpa.modelgen.CanonicalModelProcessor -classpath .;eclipselink.jar;jpa.jar -proc:only -s generated model/*.java

pause