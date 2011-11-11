package jpars.test.bootstrap;

import static org.junit.Assert.*;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import jpars.test.util.ExamplePropertiesLoader;

import org.eclipse.persistence.jaxb.JAXBContext;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.jpa.rs.PersistenceContext;
import org.eclipse.persistence.jpa.rs.PersistenceFactory;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.server.ServerSession;
import org.junit.Test;

/**
 * Test bootstrapping of a dynamically created persistence context
 * @author tware
 *
 */
public class TestBootstrap {

	@Test
	public void testBootstrap() {
		Map<String, Object> properties = new HashMap<String, Object>();
		ExamplePropertiesLoader.loadProperties(properties);	
		PersistenceFactory factory = null;
		try{
		    factory = new PersistenceFactory();
			factory.bootstrapPersistenceContext("auction", new URL("file:///C:/EclipseLinkView2/incubator/JPA-RS Incubator/tests/JPA-RS Tests/src/xmldocs/auction-persistence.xml"), properties);
		} catch (Exception e){
			fail(e.toString());
		}
		
		PersistenceContext context = factory.getPersistenceContext("auction");
		
		assertTrue(context.getEmf() != null);
		assertTrue(context.getJAXBContext() != null);
		
		Session session = (ServerSession)JpaHelper.getServerSession(context.getEmf());
		assertTrue("JPA Session did not contain Auction.", session.getProject().getAliasDescriptors().containsKey("Auction"));
        assertTrue("JPA Session did not contain Bid.", session.getProject().getAliasDescriptors().containsKey("Bid"));
        assertTrue("JPA Session did not contain User.", session.getProject().getAliasDescriptors().containsKey("User"));
        
        session = (DatabaseSession)((JAXBContext)context.getJAXBContext()).getXMLContext().getSession(0);
        assertTrue("JAXB Session did not contain Auction.", session.getProject().getAliasDescriptors().containsKey("Auction"));
        assertTrue("JAXB Session did not contain Bid.", session.getProject().getAliasDescriptors().containsKey("Bid"));
        assertTrue("JAXB Session did not contain User.", session.getProject().getAliasDescriptors().containsKey("User"));
	
	}

}
