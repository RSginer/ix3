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
package es.logongas.ix3.dao.impl;

import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.dao.GenericDAO;
import es.logongas.ix3.core.Order;
import es.logongas.ix3.core.Page;
import es.logongas.ix3.dao.Filter;
import es.logongas.ix3.dao.FilterOperator;
import es.logongas.ix3.dao.TransactionManager;
import es.logongas.ix3.dao.metadata.MetaData;
import es.logongas.ix3.dao.metadata.MetaDataFactory;
import es.logongas.ix3.util.ReflectionUtil;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.transform.ResultTransformer;
import org.springframework.beans.factory.annotation.Autowired;

public class GenericDAOImplHibernate<EntityType, PrimaryKeyType extends Serializable> implements GenericDAO<EntityType, PrimaryKeyType> {

    @Autowired
    protected SessionFactory sessionFactory;
    @Autowired
    protected MetaDataFactory metaDataFactory;
    @Autowired
    protected TransactionManager transactionManager;

    @Autowired
    protected ExceptionTranslator exceptionTranslator;

    Class entityType;

    protected final Log log = LogFactory.getLog(getClass());

    public GenericDAOImplHibernate() {
        entityType = (Class<EntityType>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public GenericDAOImplHibernate(Class<EntityType> entityType) {
        this.entityType = entityType;
    }

    @Override
    final public EntityType create() throws BusinessException {
        return create(null);
    }

    @Override
    final public EntityType create(Map<String, Object> initialProperties) throws BusinessException {
        Session session = sessionFactory.getCurrentSession();

        try {
            EntityType entity;
            entity = (EntityType) getEntityMetaData().getType().newInstance();
            if (initialProperties != null) {
                for (String key : initialProperties.keySet()) {
                    ReflectionUtil.setValueToBean(entity, key, initialProperties.get(key));
                }
            }
            this.postCreate(session, entity);
            return entity;
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    final public void insert(EntityType entity) throws BusinessException {
        Session session = sessionFactory.getCurrentSession();
        boolean isActivePreviousTransaction = transactionManager.isActive();
        try {
            this.preInsertBeforeTransaction(session, entity);
            if (isActivePreviousTransaction == false) {
                transactionManager.begin();
            }
            this.preInsertInTransaction(session, entity);
            session.save(entity);
            this.postInsertInTransaction(session, entity);
            if (isActivePreviousTransaction == false) {
                transactionManager.commit();
            }
            this.postInsertAfterTransaction(session, entity);
        } catch (BusinessException ex) {
            try {
                if ((transactionManager.isActive() == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw ex;
        } catch (javax.validation.ConstraintViolationException cve) {
            try {
                if ((transactionManager.isActive() == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve));
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            try {
                if ((transactionManager.isActive() == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve));
        } catch (RuntimeException ex) {
            try {
                if ((transactionManager.isActive() == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw ex;
        } catch (Exception ex) {
            try {
                if ((transactionManager.isActive() == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new RuntimeException(ex);
        }
    }

    @Override
    final public boolean update(EntityType entity) throws BusinessException {
        Session session = sessionFactory.getCurrentSession();
        MetaData metaData = metaDataFactory.getMetaData(entity);
        boolean isActivePreviousTransaction = transactionManager.isActive();
        boolean hasUpdate;
        try {

            String idName = metaData.getPrimaryKeyPropertyName();
            Serializable id = (Serializable) ReflectionUtil.getValueFromBean(entity, idName);
            EntityType entity2;
            if (id == null) {
                entity2 = null;
            } else {
                entity2 = (EntityType) session.get(getEntityMetaData().getType(), id);
            }

            if (entity2 == null) {
                this.preInsertBeforeTransaction(session, entity);
                if (isActivePreviousTransaction == false) {
                    transactionManager.begin();
                }
                this.preInsertInTransaction(session, entity);
                session.save(entity);
                this.postInsertInTransaction(session, entity);
                if (isActivePreviousTransaction == false) {
                    transactionManager.commit();
                }
                this.postInsertAfterTransaction(session, entity);
                hasUpdate = false;
            } else {
                this.preUpdateBeforeTransaction(session, entity);
                if (isActivePreviousTransaction == false) {
                    transactionManager.begin();
                }
                this.preUpdateInTransaction(session, entity);
                session.evict(entity2);
                session.update(entity);
                this.postUpdateInTransaction(session, entity);
                if (isActivePreviousTransaction == false) {
                    transactionManager.commit();
                }
                this.postUpdateAfterTransaction(session, entity);
                hasUpdate = true;
            }
            return hasUpdate;
        } catch (BusinessException ex) {
            try {
                if ((transactionManager.isActive() == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw ex;
        } catch (javax.validation.ConstraintViolationException cve) {
            try {
                if ((transactionManager.isActive() == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve));
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            try {
                if ((transactionManager.isActive() == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve));
        } catch (RuntimeException ex) {
            try {
                if ((transactionManager.isActive() == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw ex;
        } catch (Exception ex) {
            try {
                if ((transactionManager.isActive() == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new RuntimeException(ex);
        }
    }

    @Override
    final public EntityType read(PrimaryKeyType id) throws BusinessException {
        Session session = sessionFactory.getCurrentSession();
        try {
            this.preRead(session, id);
            EntityType entity = (EntityType) session.get(getEntityMetaData().getType(), id);
            this.postRead(session, id, entity);
            return entity;
        } catch (BusinessException ex) {
            throw ex;
        } catch (javax.validation.ConstraintViolationException cve) {
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve));
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve));
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    final public boolean delete(PrimaryKeyType id) throws BusinessException {
        Session session = sessionFactory.getCurrentSession();
        boolean isActivePreviousTransaction = transactionManager.isActive();
        boolean exists;
        EntityType entity = null;
        try {
            this.preDeleteBeforeTransaction(session, id);
            if (isActivePreviousTransaction == false) {
                transactionManager.begin();
            }
            entity = (EntityType) session.get(getEntityMetaData().getType(), id);
            this.preDeleteInTransaction(session, id, entity);
            if (entity == null) {
                exists = false;
                this.postDeleteInTransaction(session, id, entity);
                if (isActivePreviousTransaction == false) {
                    transactionManager.commit();
                }
            } else {
                session.delete(entity);
                exists = true;
                this.postDeleteInTransaction(session, id, entity);
                if (isActivePreviousTransaction == false) {
                    transactionManager.commit();
                }
            }

            this.postDeleteAfterTransaction(session, id, entity);
            return exists;
        } catch (BusinessException ex) {
            try {
                if ((transactionManager.isActive() == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw ex;
        } catch (javax.validation.ConstraintViolationException cve) {
            try {
                if ((transactionManager.isActive() == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve));
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            try {
                if ((transactionManager.isActive() == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve));
        } catch (RuntimeException ex) {
            try {
                if ((transactionManager.isActive() == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw ex;
        } catch (Exception ex) {
            try {
                if ((transactionManager.isActive() == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new RuntimeException(ex);
        }
    }

    @Override
    final public List<EntityType> search(List<Filter> filters) throws BusinessException {
        return search(filters, null);
    }

    @Override
    final public List<EntityType> search(List<Filter> filters, List<Order> orders) throws BusinessException {
        return pageableSearch(filters, orders, 0, Integer.MAX_VALUE).getContent();
    }

    @Override
    public Page<EntityType> pageableSearch(List<Filter> filters, int pageNumber, int pageSize) throws BusinessException {
        return pageableSearch(filters, null, pageNumber, pageSize);
    }

    @Override
    public Page<EntityType> pageableSearch(List<Filter> filters, List<Order> orders, int pageNumber, int pageSize) throws BusinessException {

        if (pageNumber < 0) {
            throw new RuntimeException("El agumento pageNumber no pude ser negativo");
        }
        if (pageSize < 1) {
            throw new RuntimeException("El agumento pageNumber debe ser mayor que 0");
        }
        if (orders == null) {
            orders = new ArrayList<Order>();
        }

        Session session = sessionFactory.getCurrentSession();
        try {
            Criteria criteria = session.createCriteria(getEntityMetaData().getType());
            if (filters != null) {
                for (Filter filter : filters) {
                    Object value = filter.getValue();
                    String propertyName=filter.getPropertyName();
                    FilterOperator filterOperator=filter.getFilterOperator();
                    
                    
                    if (filterOperator==FilterOperator.eq)   {  
                        if (value instanceof Object[]) {
                            criteria.add(Restrictions.in(propertyName, (Object[]) value));
                        } else if (value instanceof Collection) {
                            criteria.add(Restrictions.in(propertyName, (Collection) value));
                        } else {
                            criteria.add(Restrictions.eq(propertyName, value));
                        }
                    } else if (filterOperator==FilterOperator.ne)   {  
                        criteria.add(Restrictions.ne(propertyName, value));   
                    } else if (filterOperator==FilterOperator.gt)   {  
                        criteria.add(Restrictions.gt(propertyName, value));                         
                    } else if (filterOperator==FilterOperator.ge)   {  
                        criteria.add(Restrictions.ge(propertyName, value));                         
                    } else if (filterOperator==FilterOperator.lt)   {  
                        criteria.add(Restrictions.lt(propertyName, value));                         
                    } else if (filterOperator==FilterOperator.le)   {  
                        criteria.add(Restrictions.le(propertyName, value)); 
                    } else if (filterOperator==FilterOperator.like)   {  
                        criteria.add(Restrictions.like(propertyName, value));                         
                    } else {
                        throw new RuntimeException("El nombre del operador no es válido:"+filterOperator );
                    }
                }
            }

            if (orders != null) {
                for (Order order : orders) {
                    org.hibernate.criterion.Order criteriaOrder;

                    switch (order.getOrderDirection()) {
                        case Ascending:
                            criteriaOrder = org.hibernate.criterion.Order.asc(order.getFieldName());
                            break;
                        case Descending:
                            criteriaOrder = org.hibernate.criterion.Order.desc(order.getFieldName());
                            break;
                        default:
                            throw new RuntimeException("orderField.getOrder() desconocido" + order.getOrderDirection());
                    }
                    criteria.addOrder(criteriaOrder);
                }
            }

            List results;
            int totalPages;
            if ((pageSize == Integer.MAX_VALUE) && (pageNumber == 0)) {
                //Si el tamaño de página es tan gande (el máximo), seguro que no hace falta paginar
                results = criteria.list();
                totalPages = 1;
            } else {
                CriteriaImpl criteriaImpl = (CriteriaImpl) criteria;

                Projection originalProjection = criteriaImpl.getProjection();
                ResultTransformer originalResultTransformer = criteriaImpl.getResultTransformer();

                criteria.setProjection(Projections.rowCount());
                Long totalCount = (Long) criteria.uniqueResult();

                criteria.setProjection(originalProjection);
                criteria.setResultTransformer(originalResultTransformer);

                criteria.setMaxResults(pageSize);
                criteria.setFirstResult(pageSize * pageNumber);

                results = criteria.list();

                if (totalCount == 0) {
                    totalPages = 0;
                } else {
                    totalPages = (int) (Math.ceil(((double) totalCount) / ((double) pageSize)));
                }
            }

            Page page = new PageImpl(results, pageSize, pageNumber, totalPages);

            return page;
        } catch (javax.validation.ConstraintViolationException cve) {
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve));
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve));
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    @Override
    final public EntityType readByNaturalKey(Object naturalKey) throws BusinessException {
        Session session = sessionFactory.getCurrentSession();
        try {

            this.preReadByNaturalKey(session, naturalKey);
            EntityType entity = (EntityType) session.bySimpleNaturalId(getEntityMetaData().getType()).load(naturalKey);
            this.postReadByNaturalKey(session, naturalKey, entity);
            return entity;
        } catch (BusinessException ex) {
            throw ex;
        } catch (javax.validation.ConstraintViolationException cve) {
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve));
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve));
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    private MetaData getEntityMetaData() {
        return metaDataFactory.getMetaData(entityType);
    }

    protected void postCreate(Session session, EntityType entity) throws BusinessException {
    }

    protected void preInsertBeforeTransaction(Session session, EntityType entity) throws BusinessException {
    }

    protected void preInsertInTransaction(Session session, EntityType entity) throws BusinessException {
    }

    protected void postInsertInTransaction(Session session, EntityType entity) throws BusinessException {
    }

    protected void postInsertAfterTransaction(Session session, EntityType entity) throws BusinessException {
    }

    protected void preRead(Session session, PrimaryKeyType id) throws BusinessException {
    }

    protected void postRead(Session session, PrimaryKeyType id, EntityType entity) throws BusinessException {
    }

    protected void preReadByNaturalKey(Session session, Object naturalKey) throws BusinessException {
    }

    protected void postReadByNaturalKey(Session session, Object naturalKey, EntityType entity) throws BusinessException {
    }

    protected void preUpdateBeforeTransaction(Session session, EntityType entity) throws BusinessException {
    }

    protected void preUpdateInTransaction(Session session, EntityType entity) throws BusinessException {
    }

    protected void postUpdateInTransaction(Session session, EntityType entity) throws BusinessException {
    }

    protected void postUpdateAfterTransaction(Session session, EntityType entity) throws BusinessException {
    }

    protected void preDeleteBeforeTransaction(Session session, PrimaryKeyType id) throws BusinessException {
    }

    protected void preDeleteInTransaction(Session session, PrimaryKeyType id, EntityType entity) throws BusinessException {
    }

    protected void postDeleteInTransaction(Session session, PrimaryKeyType id, EntityType entity) throws BusinessException {
    }

    protected void postDeleteAfterTransaction(Session session, PrimaryKeyType id, EntityType entity) throws BusinessException {
    }

}
