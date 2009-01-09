package service;

import java.io.IOException;
import java.io.InputStream;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import model.Employee;
import model.persistence.PersistenceHelper;

import org.eclipse.persistence.sdo.helper.jaxb.JAXBHelperContext;

import commonj.sdo.DataObject;

/**
 * 
 * @author dclarke
 * @since EclipseLink 1.1
 */
public class EmployeeDAS {

	private static final String MODEL_PACKAGE = "model";

	private static final String SCHEMA = "xsd/jpadas-employee.xsd";

	private JAXBHelperContext context;

	private EntityManagerFactory emf;

	/**
	 * Return the JAXBHelperContext and lazily create one if null.
	 */
	public JAXBHelperContext getContext() {
		if (this.context == null) {
			InputStream xsdIn = null;

			try {
				JAXBContext jaxbContext = JAXBContext.newInstance(MODEL_PACKAGE);
				this.context = new JAXBHelperContext(jaxbContext);

				xsdIn = Thread.currentThread().getContextClassLoader().getResourceAsStream(SCHEMA);
				this.context.getXSDHelper().define(xsdIn, null);

				// Make this the default context
				this.context.makeDefaultContext();
			} catch (JAXBException e) {
				throw new RuntimeException("EmployeeDAS.getContext()::Could not create JAXBContext for: " + MODEL_PACKAGE, e);
			} finally {
				if (xsdIn != null) {
					try {
						xsdIn.close();
					} catch (IOException e) {
					}
				}
			}
		}
		return this.context;
	}

	protected EntityManagerFactory getEMF() {
		if (this.emf == null || !this.emf.isOpen()) {
			this.emf = PersistenceHelper.createEMF();
		}

		return this.emf;
	}

	public void close() {
		if (this.emf != null && this.emf.isOpen()) {
			this.emf.close();
		}

		this.emf = null;
		this.context = null;
	}

	public DataObject findEmployee(int id) {
		EntityManager em = getEMF().createEntityManager();

		try {
			Employee emp = em.find(Employee.class, id);

			if (emp == null) {
				return null;
			}

			return getContext().wrap(emp);
		} finally {
			em.close();
		}
	}

	public DataObject merge(DataObject empDO) {
		EntityManager em = getEMF().createEntityManager();

		try {
			em.getTransaction().begin();
			Employee emp = (Employee) getContext().unwrap(empDO);

			if (emp == null) {
				return null;
			}
			emp = em.merge(emp);

			em.getTransaction().commit();

			return getContext().wrap(emp);
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}

	public int findMinimumEmployeeId() {
		EntityManager em = getEMF().createEntityManager();

		try {
			return (Integer) em.createQuery("SELECT MIN(E.id) FROM Employee e").getSingleResult();
		} finally {
			em.close();
		}
	}
}
