package com.brianlindsey.SlackNewsletter.models;


import com.google.gson.annotations.SerializedName;

/**
 * This class is used to hold the meta information of the response.
 *
 * @author Mathias Markl
 */
public class Meta {

    @SerializedName("status")
    private int status;

    @SerializedName("msg")
    private String msg;

    /**
     * Returns the status.
     * 
     * @return the status as an integer value
     */
    public int getStatus() {
	return status;
    }

    /**
     * Sets the status information of the meta object.
     * 
     * @param status
     *            the status
     */
    public void setStatus(int status) {
	this.status = status;
    }

    /**
     * Returns the meta message.
     * 
     * @return the message
     */
    public String getMsg() {
	return msg;
    }

    /**
     * Sets the message of the meta object.
     * 
     * @param msg
     *            the message
     */
    public void setMsg(String msg) {
	this.msg = msg;
    }

    @Override
    public String toString() {
	return "Meta [status = " + status + ", msg = " + msg + "]";
    }
}