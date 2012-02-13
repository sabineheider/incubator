Configuring on GlassFish

- Requires Oracle DB 11.2 or better for change notification
- Requires GlassFish 3.1.2 or better for web sockets
- I suggest using OPEE version 12
- Add 3.1.2 to your OPEE install if you are using GlassFish 3.1.2 or later
  (update site: http://download.java.net/glassfish/eclipse/indigo)

0)  Stop GlassFish and OEPE if running
1)  Move the grizzly-utils.jar down a folder from GF_HOME/glassfish/modules to 
    GF_HOME/glassfish/modules/endorsed. This works around a limitation in the 
    OEPE GlassFish classpath configuration
2)  Copy your oracle jdbc driver jar into GF_HOME/domains/<domain>/lib/ext
3)  Edit GF_HOME/glassfish/osgi.properties. (GF_HOME/glassfish/osgi/felix/conf/config.properties on 3.1.1) 
    Add oracle.jdbc.*, oracle.jdbc.dcn.* to eclipselink.bootdelegation line.
    It will look roughly like this: eclipselink.bootdelegation=oracle.sql, oracle.sql.*, oracle.jdbc.*, oracle.jdbc.dcn.*
4)  Delete domain OSGi caches. By default you'll have a domain1 created and caches
    are in GF_HOME/glassfish/domains/domain1/osgi-cache
    Start GlassFish
5)  Enable websockets with the following asadmin (located in GF_HOME/glassfish/bin)
    command: 
    
    asadmin set configs.config.server-config.network-config.protocols.protocol.http-listener-1.http.websockets-support-enabled=true
    
6) You can now compile and deploy websocket applications to GlassFish.



    