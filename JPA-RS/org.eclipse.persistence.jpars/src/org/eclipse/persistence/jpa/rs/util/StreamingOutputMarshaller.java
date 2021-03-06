/*******************************************************************************
 * Copyright (c) 2011 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * 		dclarke/tware - initial 
 *      tware
 ******************************************************************************/
package org.eclipse.persistence.jpa.rs.util;

import static org.eclipse.persistence.jaxb.JAXBContext.MEDIA_TYPE;

import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.internal.dynamic.DynamicEntityImpl;
import org.eclipse.persistence.jpa.rs.PersistenceContext;

/**
 * Simple {@link StreamingOutput} implementation that uses the provided
 * {@link JAXBContext} to marshal the result when requested to either XML or
 * JSON based on the accept media provided.
 * 
 * @author dclarke
 * @since EclipseLink 2.4.0
 */
public class StreamingOutputMarshaller implements StreamingOutput {
    private PersistenceContext context;
    private Object result;
    private MediaType mediaType;

    public StreamingOutputMarshaller(PersistenceContext context, Object result, MediaType acceptedType) {
        this.context = context;
        this.result = result;
        this.mediaType = acceptedType;
    }

    public StreamingOutputMarshaller(PersistenceContext context, Object result, List<MediaType> acceptedTypes) {
        this(context, result, mediaType(acceptedTypes));
    }

    public void write(OutputStream output) throws IOException, WebApplicationException {
        long millis = System.currentTimeMillis();
        if (result instanceof byte[]){
            output.write((byte[])result);
        } else if (result instanceof String){
            OutputStreamWriter writer = new OutputStreamWriter(output);
            writer.write((String)result);
            writer.flush();
            writer.close();
        } else {
            if (this.context != null && this.context.getJAXBContext() != null && this.result != null ) {
                try {
                    context.marshallEntity(result, mediaType, output);
                    System.out.println("SteamingOutputMarshaller done write. time: " + (System.currentTimeMillis() - millis));
                    return;
                } catch (JAXBException e) {
                    // TODO: proper warning message
                    e.printStackTrace();
                    System.out.println("WARNING, could not marshall entity, serializing. " + e.toString());
                }
            }
            // could not marshall, try serializing
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(result);
            oos.flush();
            oos.close();
            output.write(baos.toByteArray());
        }
        System.out.println("SteamingOutputMarshaller done write. time: " + (System.currentTimeMillis() - millis));

    }
    
    /**
     * Identify the preferred {@link MediaType} from the list provided. This
     * will check for JSON string or {@link MediaType} first then XML.
     * 
     * @param types
     *            List of {@link String} or {@link MediaType} values;
     * @return selected {@link MediaType}
     * @throws WebApplicationException
     *             with Status.UNSUPPORTED_MEDIA_TYPE if neither the JSON or XML
     *             values are found.
     */
    public static MediaType mediaType(List<?> types) {
        if (contains(types, MediaType.APPLICATION_JSON_TYPE)) {
            return MediaType.APPLICATION_JSON_TYPE;
        }
        if (contains(types, MediaType.APPLICATION_XML_TYPE)) {
            return MediaType.APPLICATION_XML_TYPE;
        }
        return MediaType.WILDCARD_TYPE;
    }

    private static boolean contains(List<?> types, MediaType type) {
        for (Object mt : types) {
            if (mt instanceof String) {
                if (((String) mt).contains(type.toString())) {
                    return true;
                }
            } else if (((MediaType) mt).equals(type)) {
                return true;
            }
        }
        return false;
    }
    
    public static Marshaller createMarshaller(PersistenceContext context, MediaType mediaType) throws JAXBException{
        Marshaller marshaller = context.getJAXBContext().createMarshaller();
        marshaller.setProperty(MEDIA_TYPE, mediaType.toString());
        marshaller.setProperty(org.eclipse.persistence.jaxb.JAXBContext.JSON_INCLUDE_ROOT, false);
        marshaller.setAdapter(new LinkAdapter(context.getBaseURI().toString(), context));
        marshaller.setListener(new Marshaller.Listener() {
            @Override
            public void beforeMarshal(Object source) {   
                if (source instanceof DynamicEntity){
                    DynamicEntityImpl sourceImpl = (DynamicEntityImpl)source;
                    PropertyChangeListener listener = sourceImpl._persistence_getPropertyChangeListener();
                    sourceImpl._persistence_setPropertyChangeListener(null);
                    ((DynamicEntity)source).set("self", source);
                    sourceImpl._persistence_setPropertyChangeListener(listener);
                }
            }
        });
        return marshaller;
    }
}
