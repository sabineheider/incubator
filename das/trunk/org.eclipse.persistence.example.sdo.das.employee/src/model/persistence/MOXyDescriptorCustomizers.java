package model.persistence;

import model.*;

import org.eclipse.persistence.descriptors.*;
import org.eclipse.persistence.mappings.converters.ObjectTypeConverter;
import org.eclipse.persistence.oxm.mappings.XMLDirectMapping;

public class MOXyDescriptorCustomizers {

	public static void afterLoadEmployee(ClassDescriptor descriptor) {
		XMLDirectMapping genderMapping = (XMLDirectMapping) descriptor.getMappingForAttributeName("gender");

		ObjectTypeConverter converter = new ObjectTypeConverter();
		converter.setFieldClassification(String.class);
		converter.addConversionValue(Gender.Male.name(), Gender.Male);
		converter.addConversionValue(Gender.Female.name(), Gender.Female);

		genderMapping.setConverter(converter);
		
		descriptor.getDescriptorEventManager().addListener(new FixPhonesListener());
	}
	
	public static class FixPhonesListener extends DescriptorEventAdapter {

		@Override
		public void postBuild(DescriptorEvent event) {
			Employee emp = (Employee) event.getSource();
			
			for (PhoneNumber phone: emp.getPhoneNumbers()) {
				phone.setOwner(emp);
			}
		}
		
	}
}
