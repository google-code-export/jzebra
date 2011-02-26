/*
 * Backwards compability for older implimentations
 */

package jzebra;

import java.util.logging.Level;

/**
 * Renamed to PrintApplet version 1.2.0+
 * @author tfino
 */
@Deprecated
public class RawPrintApplet extends PrintApplet {
    @Deprecated
    public RawPrintApplet() {
        super();
        LogIt.log(Level.WARNING, "Since version 1.2.0, use of \"" +
                this.getClass().getCanonicalName() + "\" has been renamed and " +
                "is now deprecated and will be removed in future versions." +
                "  Please use \"" + PrintApplet.class.getCanonicalName() + "\" instead. " +
                "All functionality will remain the same.");
    }
}
