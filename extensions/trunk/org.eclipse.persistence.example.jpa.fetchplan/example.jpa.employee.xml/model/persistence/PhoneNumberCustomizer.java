/*******************************************************************************
 * Copyright (c) 1998, 2009 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     dclarke - TODO
 ******************************************************************************/
package model.persistence;

import org.eclipse.persistence.config.DescriptorCustomizer;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.mappings.OneToOneMapping;
import org.eclipse.persistence.queries.FetchGroup;
import org.eclipse.persistence.queries.ReadObjectQuery;

/**
 * Customize the PhoneNumber.owner relationship to use a minimal FetchGroup so
 * that if it is being resolved to a partial entity in the persistence context
 * it will not force the entire entity to be resolved.
 * <p>
 * This type of configuration will be required on mappings where the user wants
 * the 1:1 or M:1 lookup to simply connect to the partially entity already in the
 * context.
 * 
 * @author dclarke
 * @since EclipseLink 1.2
 */
public class PhoneNumberCustomizer implements DescriptorCustomizer {

    public void customize(ClassDescriptor descriptor) throws Exception {
        FetchGroup minEmpFG = new FetchGroup();
        minEmpFG.addAttribute("id");
        OneToOneMapping ownerMapping = (OneToOneMapping) descriptor.getMappingForAttributeName("owner");
        ((ReadObjectQuery)ownerMapping.getSelectionQuery()).setFetchGroup(minEmpFG);
    }

}
