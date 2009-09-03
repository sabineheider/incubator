package testing.example.util;

import static junit.framework.Assert.*;

import java.util.Iterator;

import javax.persistence.PersistenceContext;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.extension.MappingCollector;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.sessions.server.Server;
import org.junit.Test;

import testing.EclipseLinkJPATest;

@PersistenceContext(unitName = "employee")
public class MappingCollectorTests extends EclipseLinkJPATest {

    @Test
    public void empty() throws Exception {
        MappingCollector.Criteria criteria = new MappingCollector.Criteria() {
            public boolean collect(DatabaseMapping mapping) {
                return false;
            }

            public boolean collect(ClassDescriptor descriptor) {
                return false;
            }
        };

        MappingCollector collector = new MappingCollector(JpaHelper.getServerSession(getEMF()), criteria);

        assertSame(criteria, collector.getCriteria());
        assertNotNull(collector);
        assertTrue(collector.getMappings().isEmpty());
    }

    @Test
    public void allClassesNoMappings() throws Exception {
        MappingCollector.Criteria criteria = new MappingCollector.Criteria() {
            public boolean collect(DatabaseMapping mapping) {
                return false;
            }

            public boolean collect(ClassDescriptor descriptor) {
                return true;
            }
        };

        Server session = JpaHelper.getServerSession(getEMF());
        MappingCollector collector = new MappingCollector(session, criteria);

        assertSame(criteria, collector.getCriteria());
        assertNotNull(collector);
        assertEquals(session.getDescriptors().size(), collector.getMappings().size());

        for (Iterator<?> i = session.getDescriptors().values().iterator(); i.hasNext();) {
            ClassDescriptor descriptor = (ClassDescriptor) i.next();

            assertTrue(collector.getMappings(session, descriptor.getJavaClass()).isEmpty());
        }
    }

    @Test
    public void allClassesAllMappings() throws Exception {
        MappingCollector.Criteria criteria = new MappingCollector.Criteria() {
            public boolean collect(DatabaseMapping mapping) {
                return true;
            }

            public boolean collect(ClassDescriptor descriptor) {
                return true;
            }
        };

        Server session = JpaHelper.getServerSession(getEMF());
        MappingCollector collector = new MappingCollector(session, criteria);

        assertSame(criteria, collector.getCriteria());
        assertNotNull(collector);
        assertEquals(session.getDescriptors().size(), collector.getMappings().size());

        for (Iterator<?> i = session.getDescriptors().values().iterator(); i.hasNext();) {
            ClassDescriptor descriptor = (ClassDescriptor) i.next();

            assertFalse(collector.getMappings(session, descriptor.getJavaClass()).isEmpty());
        }
    }
}
