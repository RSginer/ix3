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
package es.logongas.ix3.security.model;

import org.hibernate.Hibernate;

/**
 * Cada de uno de los posibles permisos de un tipo de objeto. Por ejemplo. Para
 * las impresoras cada permiso sería: imprmir, cancelar ,etc. Para una url cada
 * permiso sería hacer GET, PUT, POST, DELETE , etc. Para una entidad REST cada
 * sería : READ, LIST, CREATE , UPDATE, DELETE , etc.
 *
 * @author Lorenzo González
 */
public class Permission {

    private int idPermission;
    private String name;
    private String description;
    private SecureResourceType secureResourceType;

    public Permission() {
    }

    public Permission(int idPermission, String name, String description, SecureResourceType secureResourceType) {
        this.idPermission = idPermission;
        this.name = name;
        this.description = description;
        this.secureResourceType = secureResourceType;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (Hibernate.getClass(this) != Hibernate.getClass(obj)) {
            return false;
        }

        Permission permission = (Permission) obj;
        Object obj1_prop1 = getName();
        Object obj2_prop1 = permission.getName();
        SecureResourceType obj1_prop2 = getSecureResourceType();
        SecureResourceType obj2_prop2 = permission.getSecureResourceType();


        if ((obj1_prop1 == null) || (obj2_prop1 == null) || (obj1_prop2 == null) || (obj2_prop2 == null)) {
            if ((obj1_prop1 == null) && (obj2_prop1 == null) && (obj1_prop2 == null) && (obj2_prop2 == null)) {
                return true;
            } else {
                return false;
            }
        } else if ((obj1_prop1.equals(obj2_prop1) == true) && (obj1_prop2.equals(obj2_prop2))) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        Object obj1_prop1 = name;
        Object obj1_prop2 = secureResourceType;
        int resultado;

        resultado = ((obj1_prop1 == null ? 0 : obj1_prop1.hashCode()) ^ (obj1_prop2 == null ? 0 : obj1_prop2.hashCode()));

        return resultado;
    }

    @Override
    public String toString() {
        return description;
    }

    /**
     * @return the idPermission
     */
    public int getIdPermission() {
        return idPermission;
    }

    /**
     * @param idPermission the idPermission to set
     */
    public void setIdPermission(int idPermission) {
        this.idPermission = idPermission;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the secureResourceType
     */
    public SecureResourceType getSecureResourceType() {
        return secureResourceType;
    }

    /**
     * @param secureResourceType the secureResourceType to set
     */
    public void setSecureResourceType(SecureResourceType secureResourceType) {
        this.secureResourceType = secureResourceType;
    }
}
