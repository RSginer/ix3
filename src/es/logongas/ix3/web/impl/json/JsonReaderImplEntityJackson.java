/*
 * Copyright 2013 Lorenzo González.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package es.logongas.ix3.web.impl.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.logongas.ix3.persistence.services.dao.BusinessException;
import es.logongas.ix3.persistence.services.dao.DAOFactory;
import es.logongas.ix3.persistence.services.dao.GenericDAO;
import es.logongas.ix3.persistence.services.metadata.CollectionType;
import es.logongas.ix3.persistence.services.metadata.MetaData;
import es.logongas.ix3.persistence.services.metadata.MetaDataFactory;
import es.logongas.ix3.web.services.json.JsonReader;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Se usa para una entidad de negocio y usa Jackson
 *
 * @author Lorenzo González
 */
public class JsonReaderImplEntityJackson implements JsonReader {

    private final Class clazz;
    private final ObjectMapper objectMapper;
    @Autowired
    private DAOFactory daoFactory;
    @Autowired
    private MetaDataFactory metaDataFactory;

    public JsonReaderImplEntityJackson(Class clazz) {
        this.clazz = clazz;
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        //DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.XXX'Z'");
        //objectMapper.setDateFormat(dateFormat);
        //objectMapper.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
    }

    @Override
    public Object fromJson(String json) {
        try {
            Object jsonObj = objectMapper.readValue(json, clazz);

            MetaData metaData = metaDataFactory.getMetaData(clazz);

            Object entity = readEntity(jsonObj, metaData);

            return entity;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    /**
     * Crea una entidad completa en base a la clave primaria y a los datos que vienen desde JSON
     * @param jsonObj Los datos JSON
     * @param metaData Los metadatos de la entidad a tranformar
     * @return El Objeto Entidad
     */
    private Object readEntity(Object jsonObj, MetaData metaData) {
        try {

            Object entity;

            GenericDAO genericDAO = daoFactory.getDAO(metaData.getType());

            if (emptyKey(getValueFromBean(jsonObj, metaData.getPrimaryKeyPropertyName())) == false) {
                //Si hay clave primaria es que hay que leerla entidad pq ya existe
                entity = genericDAO.read((Serializable) getValueFromBean(jsonObj, metaData.getPrimaryKeyPropertyName()));
            } else {
                //No hay clave primaria , así que creamos una nueva fila

                entity = genericDAO.create();
            }

            populateEntity(entity, jsonObj, metaData);

            return entity;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Lee una entidad de la base de datos en función de su clave primaria o de
     * alguna de sus claves naturales
     *
     * @param propertyValue
     * @param propertyMetaData
     * @return
     */
    private Object readForeingEntity(Object propertyValue, MetaData propertyMetaData) {
        try {
            if (propertyValue == null) {
                return null;
            }

            //Usamos un Set para guardar todas las entidades
            //Al ser un Set si son la misma se quedará solo una.
            Set entities = new HashSet();

            GenericDAO genericDAO = daoFactory.getDAO(propertyMetaData.getType());

            //Leer la entidad en función de su clave primaria
            String primaryKeyPropertyName = propertyMetaData.getPrimaryKeyPropertyName();
            Object primaryKey = getValueFromBean(propertyValue, primaryKeyPropertyName);
            if (primaryKey != null) {
                Object entity = genericDAO.read((Serializable) primaryKey);
                if (entity != null) {
                    entities.add(entity);
                }
            }

            //Leer la entidad en función de cada una de sus claves primarias
            for (String naturalKeyPropertyName : propertyMetaData.getNaturalKeyPropertiesName()) {
                Object naturalKey = getValueFromBean(propertyValue, naturalKeyPropertyName);
                if (naturalKey != null) {
                    Object entity = genericDAO.readByNaturalKey((Serializable) naturalKey);
                    if (entity != null) {
                        entities.add(entity);
                    }
                }
            }

            //Si hay más de un elemento es que hay conflictos entre
            //la clave primaria y las claves naturales pq identifican entidades distintas
            if (entities.size() > 1) {
                StringBuilder sb = new StringBuilder();
                sb.append(propertyMetaData.getPrimaryKeyPropertyName());
                sb.append(":");
                sb.append(getValueFromBean(propertyValue, propertyMetaData.getPrimaryKeyPropertyName()));
                sb.append(",");


                for (String naturalKeyPropertyName : propertyMetaData.getNaturalKeyPropertiesName()) {
                    sb.append(naturalKeyPropertyName);
                    sb.append(":");
                    sb.append(getValueFromBean(propertyValue, naturalKeyPropertyName));
                    sb.append(",");
                }

                throw new RuntimeException("El objeto JSON tiene clave primarias y claves naturales que referencian distintos objetos:");
            }

            if (entities.size() == 1) {
                //Retornamos el única elemento que se ha leido de la base de datos
                return entities.iterator().next();
            } else {
                //Si no hay nada retornamos null
                return null;
            }


        } catch (BusinessException be) {
            throw new RuntimeException(be);
        }
    }


    private void populateEntity(Object entity, Object jsonObj, MetaData metaData) throws BusinessException {

        for (String propertyName : metaData.getPropertiesMetaData().keySet()) {
            MetaData propertyMetaData = metaData.getPropertiesMetaData().get(propertyName);

            if (propertyMetaData.isCollection() == false) {
                switch (propertyMetaData.getMetaType()) {
                    case Scalar: {
                        Object rawValue = getValueFromBean(jsonObj, propertyName);
                        setValueToBean(entity, rawValue, propertyName);
                        break;
                    }
                    case Entity: {
                        //Debemos leer la referencia de la base de datos
                        Object rawValue = getValueFromBean(jsonObj, propertyName);

                        Object value = readForeingEntity(rawValue, propertyMetaData);
                        setValueToBean(entity, value, propertyName);
                        break;
                    }
                    case Component: {
                        //Es un componente, así que hacemos la llamada recursiva
                        Object rawValue = getValueFromBean(jsonObj, propertyName);
                        populateEntity(entity, rawValue, propertyMetaData);
                        break;
                    }
                    default:
                        throw new RuntimeException("El MetaTypo es desconocido:" + propertyMetaData.getMetaType());
                }
            } else {
                switch (propertyMetaData.getCollectionType()) {
                    case List:
                    case Set: {
                        if (propertyMetaData.isCollectionLazy() == false) {
                            Collection rawCollection = (Collection) getValueFromBean(jsonObj, propertyName);
                            Collection currentCollection = (Collection) getValueFromBean(entity, propertyName);

                            //Borramos todos los elementos para añadir despues los que vienen desde JSON
                            currentCollection.clear();

                            //Añadimos los elementos que vienen desde JSON
                            for (Object rawValue : rawCollection) {
                                Object value = readEntity(rawValue, propertyMetaData);
                                currentCollection.add(value);
                            }
                        } else {
                            //NO hamos nada pq las colecciones Lazy no se cargan
                        }
                        break;
                    }
                    case Map: {
                        if (propertyMetaData.isCollectionLazy() == false) {
                            Map rawMap = (Map) getValueFromBean(jsonObj, propertyName);
                            Map currentMap = (Map) getValueFromBean(entity, propertyName);

                            //Borramos todos los elementos para añadir despues los que vienen desde JSON
                            currentMap.clear();

                            //Añadimos los elementos que vienen desde JSON
                            for (Object key : rawMap.keySet()) {
                                Object rawValue = rawMap.get(key);
                                Object value = readEntity(rawValue, propertyMetaData);
                                currentMap.put(key, value);
                            }
                        } else {
                            //NO hamos nada pq las colecciones Lazy no se cargan
                        }
                        break;
                    }
                    default:
                        throw new RuntimeException("El CollectionType es desconocido:" + propertyMetaData.getCollectionType());
                }
            }

        }
    }


    private boolean emptyKey(Object primaryKey) {
        if (primaryKey == null) {
            return true;
        }
        if (primaryKey instanceof Number) {
            Number number = (Number) primaryKey;
            if (number.longValue() == 0) {
                return true;
            }
        }

        if (primaryKey instanceof String) {
            String s = (String) primaryKey;
            if (s.trim().equals("")) {
                return true;
            }
        }


        return false;
    }

    /**
     * Obtiene el valor de la propiedad de un Bean
     *
     * @param obj El objeto Bean
     * @param propertyName El nombre de la propiedad
     * @return El valor de la propiedad
     */
    private Object getValueFromBean(Object obj, String propertyName) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

            Method readMethod = null;
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                if (propertyDescriptor.getName().equals(propertyName)) {
                    readMethod = propertyDescriptor.getReadMethod();
                }
            }

            if (readMethod == null) {
                throw new RuntimeException("No existe la propiedad:" + propertyName);
            }

            return readMethod.invoke(obj);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Establece el valor de la propiedad de un Bean
     *
     * @param obj El objeto Bean
     * @param value El valor de la propiedad
     * @param propertyName El nombre de la propiedad
     */
    private void setValueToBean(Object obj, Object value, String propertyName) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

            Method writeMethod = null;
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                if (propertyDescriptor.getName().equals(propertyName)) {
                    writeMethod = propertyDescriptor.getWriteMethod();
                }
            }

            if (writeMethod == null) {
                throw new RuntimeException("No existe la propiedad:" + propertyName);
            }

            writeMethod.invoke(obj, value);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
