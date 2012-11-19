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

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

public class HibernateUtil {

    private static HibernateUtilInternalState state=new HibernateUtilInternalState();

    public static void buildSessionFactory() {
        Configuration configuration = new Configuration();
        configuration.configure();
        ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
        state.sessionFactory = configuration.buildSessionFactory(serviceRegistry);        
    }

    public static void closeSessionFactory() {
        state.sessionFactory.close();
    }

    public static void openSessionAndAttachToThread() {
        Session session = state.sessionFactory.openSession();
        state.threadLocalSession.set(session);
    }

    public static SessionFactory getSessionFactory() {
        return new SessionFactoryImplThreadLocal(state);
    }

    public static void closeSessionAndDeattachFromThread() {
        Session session = state.threadLocalSession.get();
        if (session!=null) {
            session.close();
        }
        state.threadLocalSession.set(null);
    }

    public static boolean isSessionAttachToThread() {
        if (state.threadLocalSession.get() != null) {
            return true;
        } else {
            return false;
        }
    }
}
