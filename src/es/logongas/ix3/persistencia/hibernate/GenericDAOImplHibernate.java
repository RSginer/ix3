/*
 * Copyright 2012 Lorenzo González.
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
package es.logongas.ix3.persistencia.hibernate;

import es.logongas.ix3.persistencia.dao.BussinessException;
import es.logongas.ix3.persistencia.dao.Criteria;
import es.logongas.ix3.persistencia.dao.GenericDAO;
import es.logongas.ix3.persistencia.dao.MetaData;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class GenericDAOImplHibernate<EntityType, PrimaryKeyType extends Serializable> implements GenericDAO<EntityType, PrimaryKeyType> {

    @Autowired
    SessionFactory sessionFactory;
    
    MetaData metaData;
    
    protected final Log log = LogFactory.getLog(getClass());
    
    public GenericDAOImplHibernate() {
        metaData=new MetaDataImplHibernate((Class<EntityType>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
    }
    
    public GenericDAOImplHibernate(Class<EntityType> entityType) {
        metaData=new MetaDataImplHibernate(entityType);
    }    
    
    
    @Override
    public EntityType create() throws BussinessException {
        try {
            return (EntityType)getMetaData().getEntiyType().newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    
    @Override
    public void insert(EntityType entity) throws BussinessException {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            session.save(entity);
            session.getTransaction().commit();
        } catch (javax.validation.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BussinessException(cve);            
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BussinessException(cve);           
        } catch (RuntimeException ex) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw ex;
        } catch (Exception ex) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new RuntimeException(ex);
        }
    }    
    
    @Override
    public boolean update(EntityType entity) throws BussinessException {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            
            EntityType entity2 = (EntityType) session.get(getMetaData().getEntiyType(), session.getIdentifier(entity));
            if (entity == null) {
                session.getTransaction().commit();
                return false;
            } else {
                session.evict(entity2);
                session.update(entity);
                session.getTransaction().commit();
                return true;
            }            
        } catch (javax.validation.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BussinessException(cve);            
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BussinessException(cve);           
        } catch (RuntimeException ex) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw ex;
        } catch (Exception ex) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new RuntimeException(ex);
        }
    }

    @Override
    public EntityType read(PrimaryKeyType id) throws BussinessException {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            EntityType entity = (EntityType) session.get(getMetaData().getEntiyType(), id);           
            session.getTransaction().commit();

            return entity;
        } catch (javax.validation.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BussinessException(cve);            
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BussinessException(cve);           
        } catch (RuntimeException ex) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw ex;
        } catch (Exception ex) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean delete(PrimaryKeyType id) throws BussinessException {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            EntityType entity = (EntityType) session.get(getMetaData().getEntiyType(), id);
            if (entity == null) {
                session.getTransaction().commit();
                return false;
            } else {
                session.delete(entity);
                session.getTransaction().commit();
                return true;
            } 
        } catch (javax.validation.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BussinessException(cve);            
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BussinessException(cve);            
        } catch (RuntimeException ex) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw ex;
        } catch (Exception ex) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<EntityType> search(List<Criteria> criterias) throws BussinessException {
        Session session = sessionFactory.getCurrentSession();
        try {

            Query query = session.createQuery("SELECT e FROM " + getMetaData().getEntiyType().getName() + " e");
            List<EntityType> entities = query.list();

            return entities;
        } catch (javax.validation.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BussinessException(cve);            
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BussinessException(cve);            
        } catch (RuntimeException ex) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw ex;
        } catch (Exception ex) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new RuntimeException(ex);
        }
    }

    @Override
    public EntityType readByNaturalKey(Object value) throws BussinessException {
        Session session = sessionFactory.getCurrentSession();
        try {
            EntityType entity=(EntityType)session.bySimpleNaturalId(getMetaData().getEntiyType()).load(value);

            return entity;
        } catch (javax.validation.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BussinessException(cve);            
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BussinessException(cve);            
        } catch (RuntimeException ex) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw ex;
        } catch (Exception ex) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new RuntimeException(ex);
        }

    }    
    

    @Override
    public MetaData getMetaData() {
        return metaData;
    }


}
