/**
 * Copyright 2014 Tampere University of Technology, Pori Department
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package core.tut.pori.context;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import javax.websocket.server.ServerApplicationConfig;
import javax.websocket.server.ServerEndpointConfig;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import core.tut.pori.utils.StringUtils;
import core.tut.pori.websocket.Definitions;
import core.tut.pori.websocket.SocketService;

/**
 * WebSocket Service end-point handler.
 * 
 * This class can be used to retrieve initialized instances of WebSocket end-point services.
 * 
 * One should not initialize this handler directly, as an instantiated version is available from ServiceInitializer.
 */
public class WebSocketHandler {
	/** websocket service uri */
	public static final String SERVICE_URI = "/websocket/";
	private static final Logger LOGGER = Logger.getLogger(WebSocketHandler.class);
	private static final String PARAMETER_END_POINT = "end_point";
	private static final String SERVLET_CONFIGURATION_FILE = "websocket-servlet.xml";
	private static WebSocketEndpoint END_POINT = null;
	private ClassPathXmlApplicationContext _context = null;
	private Map<String, SocketService> _websockets = null;
	
	/**
	 * 
	 */
	public WebSocketHandler(){
		END_POINT = new WebSocketEndpoint();
		initialize();
	}
	
	/**
	 * 
	 */
	public void close(){
		_context.close();
		_context = null;
		_websockets = null;
		END_POINT = null;
	}
	
	/**
	 * Do NOT close or cleanup the instances returned by this method, the initialization and destruction is handled automatically.
	 * 
	 * @param cls
	 * @return the service or null if not found
	 */
	public <T extends SocketService> T getSocketService(Class<T> cls){
		try{
			for(T candidate : _context.getBeansOfType(cls).values()){
				if(candidate.getClass().equals(cls)){
					return candidate;
				}
			}
		} catch (BeansException ex){
			LOGGER.warn(ex, ex);		
		}
		return null;
	}
	
	/**
	 * 
	 */
	private void initialize(){
		LOGGER.debug("Initializing handler...");
		Date started = new Date();
		_context = new ClassPathXmlApplicationContext(core.tut.pori.properties.SystemProperty.CONFIGURATION_FILE_PATH+SERVLET_CONFIGURATION_FILE);

		LOGGER.debug("Class Path XML Context initialized in "+StringUtils.getDurationString(started, new Date()));

		Map<String, SocketService> services = _context.getBeansOfType(SocketService.class);
		int count = services.size();
		LOGGER.info("Found "+count+" service(s).");
		_websockets = new HashMap<>(count);

		for(Iterator<SocketService> iter = services.values().iterator();iter.hasNext();){
			addService(iter.next());
		}

		LOGGER.debug("Web Socket Handler initialized in "+StringUtils.getDurationString(started, new Date()));
	}

	/**
	 * 
	 * @param service
	 * @throws IllegalArgumentException
	 */
	private void addService(SocketService service) throws IllegalArgumentException {
		String name = service.getEndPointName();
		if(name == null){
			throw new IllegalArgumentException("Invalid service name for "+service.getClass().toString());
		}else if(_websockets.containsKey(name)){
			throw new IllegalArgumentException("Duplicate Web Socket end point name "+name+" for "+service.getClass().toString());
		}else{
			_websockets.put(name, service);
		}
	}

	/**
	 * WebSocket server end point
	 */
	private class WebSocketEndpoint extends Endpoint {
		
		/**
		 * 
		 */
		public WebSocketEndpoint(){
			LOGGER.debug("End point initialized at "+SERVICE_URI);
		}

		@Override
		public void onClose(Session session, CloseReason closeReason) {
			String endPointName = session.getPathParameters().get(PARAMETER_END_POINT);
			SocketService socket = _websockets.get(endPointName);
			if(socket == null){
				LOGGER.debug("Ignoring close on non-existent end point, name: "+endPointName);
			}else{
				try{
					socket.onClose(session, closeReason);
				}  catch (Throwable ex){
					LOGGER.error(ex, ex);
				}
			}
		}

		@Override
		public void onError(Session session, Throwable throwable) {
			SocketService socket = _websockets.get(session.getPathParameters().get(PARAMETER_END_POINT));
			try{
				socket.onError(session, throwable);
			} catch (IllegalArgumentException ex){
				try {
					session.close(Definitions.CLOSE_REASON_BAD_REQUEST);
				} catch (IOException ex1) {
					LOGGER.debug(ex, ex1);
				}
			} catch (Throwable ex){
				try {
					session.close(Definitions.CLOSE_REASON_INTERNAL_SERVER_ERROR);
				} catch (IOException ex1) {
					LOGGER.debug(ex, ex1);
				}
			}
		}

		@Override
		public void onOpen(final Session session, EndpointConfig config) {
			String endPointName = session.getPathParameters().get(PARAMETER_END_POINT);
			final SocketService socket = _websockets.get(endPointName);
			if(socket == null){
				LOGGER.warn("Closing session to non-existent end point, name: "+endPointName);
				try {
					session.close(Definitions.CLOSE_REASON_NOT_FOUND);
				} catch (IOException ex) {
					LOGGER.debug(ex, ex);
				}
				return;
			}
			
			try{
				if(socket.accept(session)){
					session.addMessageHandler(new MessageHandler.Whole<String>() {
						@Override
						public void onMessage(String message) {
							socket.received(session, message);
						}
					});
				}else{
					LOGGER.debug("Closing rejected session.");
					try {
						session.close(Definitions.CLOSE_REASON_UNAUTHORIZED);
					} catch (IOException ex) {
						LOGGER.debug(ex, ex);
					}
				}
			} catch (IllegalArgumentException ex){
				try {
					session.close(Definitions.CLOSE_REASON_BAD_REQUEST);
				} catch (IOException ex1) {
					LOGGER.debug(ex, ex1);
				}
			} catch (Throwable ex){
				try {
					session.close(Definitions.CLOSE_REASON_INTERNAL_SERVER_ERROR);
				} catch (IOException ex1) {
					LOGGER.debug(ex, ex1);
				}
			}
		}		
	} // class WebSocketEndpoint
	
	/**
	 * Server End-point configurator.
	 * 
	 * This class binds the WebSocketEndpoint to accessible uri on the server.
	 */
	public static class WebSocketHandlerConfigurator implements ServerApplicationConfig{

		@Override
		public Set<Class<?>> getAnnotatedEndpointClasses(Set<Class<?>> set) {
			return Collections.emptySet();
		}

		@Override
		public Set<ServerEndpointConfig> getEndpointConfigs(Set<Class<? extends Endpoint>> set) {
			Set<ServerEndpointConfig> configs = new HashSet<>();
			configs.add(ServerEndpointConfig.Builder.
					create(WebSocketEndpoint.class, SERVICE_URI+"{"+PARAMETER_END_POINT+"}")
					.configurator(new ServerEndpointConfig.Configurator(){
						@SuppressWarnings("unchecked")
						@Override
						public <T> T getEndpointInstance(Class<T> cls) throws InstantiationException {
							return (T) END_POINT;
						}						
					})
					.build()
				);
			return configs;
		}
	} // class WebSocketHandlerConfigurator
}
