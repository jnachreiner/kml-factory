/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package kmlconverter;

import java.awt.Dimension;
import javax.swing.JLabel;

/**
 * Simple class that provides status bar for KML Converter main application window.
 * Has only one method to set status bar message.
 * 
 * Initial code taken from <a href=http://www.java-tips.org/java-se-tips/javax.swing/creating-a-status-bar.html>Java Tips</a>
 
 * @author kiril
 * @version 25/04/2009
 */
public class StatusBar extends JLabel {

    /** Creates a new instance of StatusBar */
    public StatusBar() {
        super();
        super.setPreferredSize(new Dimension(100, 16));
        setMessage("Ready");
    }

    public void setMessage(String message) {
        setText(" "+message);
    }
}
