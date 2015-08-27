package core.tut.pori.websocket;

import javax.websocket.CloseReason;

import core.tut.pori.http.Response;

/**
 * Definitions for websocket package.
 * 
 */
public final class Definitions {
	/* close reasons */
	/** CloseReason with HTTP status code Bad Request */
	public static final CloseReason CLOSE_REASON_BAD_REQUEST = new CloseReason(new CloseReason.CloseCode() {		
		@Override
		public int getCode() {
			return Response.Status.BAD_REQUEST.toStatusCode();
		}
	}, Response.Status.BAD_REQUEST.name());
	
	/** CloseReason with HTTP status code Forbidden */
	public static final CloseReason CLOSE_REASON_FORBIDDEN = new CloseReason(new CloseReason.CloseCode() {		
		@Override
		public int getCode() {
			return Response.Status.FORBIDDEN.toStatusCode();
		}
	}, Response.Status.FORBIDDEN.name());
	
	/** CloseReason with HTTP status code Not Found */
	public static final CloseReason CLOSE_REASON_NOT_FOUND = new CloseReason(new CloseReason.CloseCode() {		
		@Override
		public int getCode() {
			return Response.Status.NOT_FOUND.toStatusCode();
		}
	}, Response.Status.NOT_FOUND.name());
	
	/** CloseReason with HTTP status code Internal Server Error */
	public static final CloseReason CLOSE_REASON_INTERNAL_SERVER_ERROR = new CloseReason(new CloseReason.CloseCode() {		
		@Override
		public int getCode() {
			return Response.Status.INTERNAL_SERVER_ERROR.toStatusCode();
		}
	}, Response.Status.INTERNAL_SERVER_ERROR.name());
	
	/** CloseReason with HTTP status code Unauthorized */
	public static final CloseReason CLOSE_REASON_UNAUTHORIZED = new CloseReason(new CloseReason.CloseCode() {		
		@Override
		public int getCode() {
			return Response.Status.UNAUTHORIZED.toStatusCode();
		}
	}, Response.Status.UNAUTHORIZED.name());
	
	
	/**
	 * 
	 */
	private Definitions(){
		// nothing needed
	}
}
