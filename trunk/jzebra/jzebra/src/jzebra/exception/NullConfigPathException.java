/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jzebra.exception;
/**
 *
 * @author tfino
 */
public class NullConfigPathException extends java.io.IOException {

    /**
     * Creates a new instance of <code>NullConfigPathException</code> without detail message.
     */
    public NullConfigPathException() {
    }


    /**
     * Constructs an instance of <code>NullConfigPathException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public NullConfigPathException(String msg) {
        super(msg);
    }
}
