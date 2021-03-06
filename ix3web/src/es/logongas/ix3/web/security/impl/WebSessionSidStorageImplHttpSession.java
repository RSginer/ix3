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
package es.logongas.ix3.web.security.impl;

import es.logongas.ix3.web.security.WebSessionSidStorage;
import java.io.Serializable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Almacena la sesión en el servidor usando HttpSession
 * @author logongas
 */
public class WebSessionSidStorageImplHttpSession implements WebSessionSidStorage {   
   
    
    @Override
    public void setSid(HttpServletRequest httpServletRequest,HttpServletResponse httpServletResponse,Serializable sid) {
        HttpSession httpSession = httpServletRequest.getSession(true);
        httpSession.setAttribute("sid", sid);
    }

    @Override
    public Serializable getSid(HttpServletRequest httpServletRequest,HttpServletResponse httpServletResponse) {
        HttpSession httpSession = httpServletRequest.getSession(false);
        if (httpSession != null) {
            return (Serializable) httpSession.getAttribute("sid");
        } else {
            return null;
        }
    }

    @Override
    public void deleteSid(HttpServletRequest httpServletRequest,HttpServletResponse httpServletResponse) {
        HttpSession httpSession = httpServletRequest.getSession(false);
        if (httpSession!=null) {
            httpSession.setAttribute("sid", null);
        }
    }
    
}
