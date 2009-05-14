/*
 * KMLConverterApp.java
 */

package kmlconverter;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class KMLConverterApp extends SingleFrameApplication {

    /**
     * At startup create and show the main frame of the application.
     */
    @Override
    protected void startup() {
            show(new KMLConverterView(this));
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     * @param root
     */
    @Override
    protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of KMLConverterApp
     */
    public static KMLConverterApp getApplication() {
        return Application.getInstance(KMLConverterApp.class);
    }

    /**
     * Main method launching the application.
     * @param args can accept filename as an argument, file will be loadet to jTable on startup
     */
    public static void main(String[] args){
//        arguments = args;
        launch(KMLConverterApp.class, args);
    }
}
