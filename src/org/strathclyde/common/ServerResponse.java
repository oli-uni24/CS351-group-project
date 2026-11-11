package org.strathclyde.common;

/**
 * Returned by every controller method
 *
 * On success, boolean success is true and String message holds reply text
 * On failure, boolean success is false and String message explains what went wrong
 *
 * ClientSessionHandler turns this into a reply line for the client to read on the terminal
 * OK message on success
 * ERROR message on failure
 */
public class ServerResponse {
    private final boolean success;
    private final String message;

    public ServerResponse (boolean success, String message){
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess(){ return success;}
    public String getMessage(){ return message;}
}
