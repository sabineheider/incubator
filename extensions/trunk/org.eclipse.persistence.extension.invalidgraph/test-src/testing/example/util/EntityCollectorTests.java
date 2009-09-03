package testing.example.util;

import javax.persistence.PersistenceContext;

import org.junit.Test;

import testing.EclipseLinkJPATest;

@PersistenceContext(unitName = "employee")
public class EntityCollectorTests extends EclipseLinkJPATest {

    @Test
    public void empty() {

    }
}
