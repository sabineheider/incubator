
REM Demo using different persistence.xml location with properties set in persistence.xml

javac -processor org.eclipse.persistence.internal.jpa.modelgen.CanonicalModelProcessor -classpath .;eclipselink.jar;jpa.jar -s generated -proc:only -Aeclipselink.persistencexml=./xml/persistence.xml model/*.java

pause