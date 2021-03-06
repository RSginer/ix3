/*
 * Copyright 2015 logongas.
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
package es.logongas.ix3.dao;

import java.util.List;
import java.util.Map;

/**
 * Lanzar consultas nativas contra la base de datos
 * @author logongas
 */
public interface NativeDAO {
    List<Object> createNativeQuery(DataSession dataSession,String query,List<Object> params);
    List<Object> createNativeQuery(DataSession dataSession,String query,Map<String,Object> params);
}
