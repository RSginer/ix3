/*
 * Copyright 2013 Lorenzo.
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
package es.logongas.ix3.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * Utilidades para obtener anotaciones de clases
 *
 * @author Lorenzo
 */
public class ReflectionUtil {

    /**
     * Obtiene una anotación de una clase. La anotación se obtiene de la
     * propiedad o del método
     *
     * @param <T>
     * @param baseClass La clase en la que se busca la anotación
     * @param propertyName El nombre de la propiedad sobre la que se busca la
     * anotación
     * @param annotationClass El tipo de anotación a buscar
     * @return Retorna la anotación.Si la anotación existe tanto en la propiedad
     * como en el método se retorna solo el de la propiedad.
     */
    static public <T extends Annotation> T getAnnotation(Class baseClass, String propertyName, Class<T> annotationClass) {
        if (annotationClass == null) {
            throw new IllegalArgumentException("El argumento annotationClass no puede ser null");
        }

        if (baseClass == null) {
            return null;
        }

        if ((propertyName == null) || (propertyName.trim().equals(""))) {
            throw new IllegalArgumentException("El argumento propertyName no puede ser null");
        }

        T annotationField = getFieldAnnotation(baseClass, propertyName, annotationClass);
        if (annotationField != null) {
            return annotationField;
        }

        T annotationMethod = getMethodAnnotation(baseClass, propertyName, annotationClass);
        if (annotationMethod != null) {
            return annotationMethod;
        }

        //No hemos encontrado la anotación
        return null;

    }

    static private <T extends Annotation> T getFieldAnnotation(Class baseClass, String propertyName, Class<T> annotationClass) {
        Field field = getField(baseClass, propertyName);
        if (propertyName.equals("listaValores")) {
            System.out.println("La propiedad=" + field);
        }
        if (field == null) {

            return null;
        }

        T annotation = field.getAnnotation(annotationClass);

        return annotation;
    }

    static private <T extends Annotation> T getMethodAnnotation(Class baseClass, String methodName, Class<T> annotationClass) {
        String suffixMethodName = StringUtils.capitalize(methodName);
        Method method = getMethod(baseClass, "get" + suffixMethodName);
        if (method == null) {
            method = getMethod(baseClass, "is" + suffixMethodName);
            if (method == null) {
                method = getMethod(baseClass, methodName);
                if (method == null) {

                    return null;
                }
            }
        }
        T annotation = method.getAnnotation(annotationClass);

        return annotation;
    }

    static public Field getField(Class clazz, String propertyName) {
        return ReflectionUtils.findField(clazz, propertyName);
    }

    static public Method getMethod(Class clazz, String methodName) {
        Method[] methods = clazz.getMethods();

        Method method = null;
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals(methodName)) {

                if (method != null) {
                    throw new RuntimeException("Existen dos o mas metodos llamados '" + methodName + "' en '" + clazz.getName() + "'");
                }

                method = methods[i];
            }
        }

        return method;

    }

    /**
     * Obtiene el valor de la propiedad de un Bean
     *
     * @param obj El objeto Bean
     * @param propertyName El nombre de la propiedad
     * @return El valor de la propiedad
     */
    static public Object getValueFromBean(Object obj, String propertyName) {
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

}
