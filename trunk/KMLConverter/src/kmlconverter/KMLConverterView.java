/*
 * KMLConverterView.java
 */
package kmlconverter;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.jdesktop.application.Action;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.table.AbstractTableModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * The application's main frame.
 */
public class KMLConverterView extends FrameView implements KeyListener {

    /**
     *
     * @param app
     */
    public KMLConverterView(SingleFrameApplication app) {
        super(app);
        this.kmlTableModel = new KMLjTableModel();
        columnStatus = new HashMap<Integer, String>();
        kmlFileFilter = new KMLFileFilter();
        txtFileFilter = new TXTFileFilter();
        chooser.addChoosableFileFilter(kmlFileFilter);
        chooser.addChoosableFileFilter(txtFileFilter);
        initComponents();
        setTableSelectionListeners();
        mainTable.addKeyListener(this);
        prefs = Preferences.userNodeForPackage(KMLConverterView.class);
    }

    private void setTableSelectionListeners() {
        //Detects which column is selected and outputs result
        //to statusBar

        if (mainTable.getColumnSelectionAllowed()) {

            mainTable.setColumnSelectionAllowed(true);
            ListSelectionModel colSM =
                    mainTable.getColumnModel().getSelectionModel();
            colSM.addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    //Ignore extra messages.
                    if (e.getValueIsAdjusting()) {
                        return;
                    }

                    ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                    if (lsm.isSelectionEmpty()) {
                        statusBar.setMessage("No columns are selected.");
                    } else {
                        selectedColIndex = lsm.getMinSelectionIndex();
                        statusBar.setMessage("Column " + selectedColIndex + " is now selected." + "(" + columnStatus.get(selectedColIndex) + ")");
                    }
                }
            });
        }
    }

    /**
     *
     */
    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = KMLConverterApp.getApplication().getMainFrame();
            aboutBox = new KMLConverterAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        KMLConverterApp.getApplication().show(aboutBox);
    }

    /**
     *
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     * @throws WrongFileException
     */
    @Action
    public void openFile() throws FileNotFoundException, IOException, WrongFileException {
        //XXX: test what happens when application is run for the first time and
        //there is nothing stored in preferences
        File currentDirectory = new File(prefs.get("LAST_IMPORT_DIR",
                System.getProperty("user.dir")));
        chooser.setCurrentDirectory(currentDirectory);
        int returnVal = chooser.showOpenDialog(jLabelDataFile);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            prefs.put("LAST_IMPORT_DIR", chooser.getCurrentDirectory().toString());
            ButtonGroup group = new javax.swing.ButtonGroup();
            String question = "Please select CSV file delimiter:";
            JRadioButton commaJRadioButton = new javax.swing.JRadioButton("Comma separated");
            JRadioButton tabJRadioButton = new javax.swing.JRadioButton("Tab separated");
            tabJRadioButton.setSelected(true);
            group.add(commaJRadioButton);
            group.add(tabJRadioButton);
            String[] options = {"Tab delimited", "Comma delimited"};
            String answer = (String) javax.swing.JOptionPane.showInputDialog(null,
                    question,
                    "Delimiters",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    null);

            String currentFileDelimeter = "";
            if (answer != null && answer.equals("Comma delimited")) {
                currentFileDelimeter = ",";
            } else if (answer != null && answer.equals("Tab delimited")) {
                currentFileDelimeter = "\t";
            } else {
                return;
            }

            jTextFieldCSVFilePath.setText(chooser.getSelectedFile().getAbsolutePath());
            currentFileName = chooser.getSelectedFile().getName().replaceAll(".txt|.csv", "");

            try {
                CSVFile parseCSV = new CSVFile();
                String fileName = new String();
                fileName = jTextFieldCSVFilePath.getText();
                ArrayList<ArrayList> data = parseCSV.openFile(fileName, currentFileDelimeter);
                ArrayList<String> header = parseCSV.openFileGetHeaders(fileName, currentFileDelimeter);
                kmlTableModel = new KMLjTableModel(header, data);
                mainTable.setModel(kmlTableModel);
                setColumnSizes();
                statusBar.setMessage("File imported successfully. All columns were set to 'description'");

            } catch (FileNotFoundException ex) {
                Logger.getLogger(KMLConverterApp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(KMLConverterApp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (WrongFileException ex) {
                statusBar.setMessage("File you are trying to open does not seem to have enough columns.");
            }

            //Fill column status tracker with values for each column
            for (int i = 0; i < mainTable.getColumnCount(); i++) {
                //make shure that all colums are set to Description
                columnStatus.put(i, "Description");
            }
        }
    }

    //TODO this method can be used in order to delete folder names form combobox editor
    @Override
    public void keyPressed(KeyEvent kevt) {
        try {
            if (kevt.getKeyCode() == KeyEvent.VK_DELETE && mainTable.hasFocus()) {
                TableColumn columnToRemove = mainTable.getColumnModel().getColumn(selectedColIndex);
                mainTable.removeColumn(columnToRemove);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        //do nothing
    }

    @Override
    public void keyReleased(KeyEvent e) {
        //do nothing
    }


    //TODO expand this method in order to remember which
    //direcotry user accessed last and next time display it in the chooser
    private void rememberCurrentDirectory() {
        System.out.println(chooser.getCurrentDirectory());
    }

    private void setColumnSizes() {
        KMLjTableModel model = (KMLjTableModel) mainTable.getModel();
        TableColumn column = null;
        Component comp = null;
        int headerWidth = 0;
        int cellWidth = 0;

        TableCellRenderer headerRenderer =
                mainTable.getTableHeader().getDefaultRenderer();

        for (int i = 0; i < mainTable.getColumnCount(); i++) {
            column = mainTable.getColumnModel().getColumn(i);

            comp = headerRenderer.getTableCellRendererComponent(
                    null, column.getHeaderValue(),
                    false, false, 0, 0);
            headerWidth = comp.getPreferredSize().width;

            comp = mainTable.getDefaultRenderer(model.getColumnClass(i)).
                    getTableCellRendererComponent(
                    mainTable, model.getColumnLongestValue(i),
                    false, false, 0, i);
            cellWidth = comp.getPreferredSize().width;

            column.setPreferredWidth(Math.max(headerWidth, cellWidth));
        }
    }

    /**
     * 
     */
    @Action
    public void setCoulumDataType() {

        int coordinateType = coordinateTypeSelector.getSelectedIndex();
        int altitudeType = altitudeTypeSelector.getSelectedIndex();

        if (jRadioButtonName.isSelected()) {
            columnStatus.put(selectedColIndex, "Name");

            //code can be used later in order to change table column names
            mainTable.getColumnModel().getColumn(selectedColIndex).setHeaderValue("Point Name");
            mainTable.getTableHeader().resizeAndRepaint();
            setColumnSizes();

            statusBar.setMessage("Column data type set to - Point Name");

        }

        if (jRadioButtonFolder.isSelected()) {
            columnStatus.put(selectedColIndex, "Folder Name");
            mainTable.getColumnModel().getColumn(selectedColIndex).setHeaderValue("Folder Name");
            mainTable.getTableHeader().resizeAndRepaint();
            setColumnSizes();

            statusBar.setMessage("Column data type set to - Folder Name");
        }

        if (jRadioButtonDescription.isSelected()) {
            columnStatus.put(selectedColIndex, "Description");
            statusBar.setMessage("Column data type set to - Description");
        }

        if (jRadioButtonSkip.isSelected()) {
            columnStatus.put(selectedColIndex, "Skip");
            statusBar.setMessage("Column data type set to - Skip. Values will not be exported.");
        }

        if (jRadioButtonAltitude.isSelected()) {
            int rowCount = mainTable.getRowCount();
            for (int i = 0; i < rowCount; i++) {
                Object temp = mainTable.getValueAt(i, selectedColIndex);
                if (temp != null && altitudeType == 0) {
                    Double feet = new Double(temp.toString());
                    Double convertedAltitude = feet * 0.3048;
                    mainTable.setValueAt(convertedAltitude, i, selectedColIndex);
                }

                if (temp != null && altitudeType == 1) {
                    //units of 100 foot, i.e. Flightlevel 100 = 10,000 ft
                    Double flightLevel = new Double(temp.toString());
                    Double feet = flightLevel * 100;
                    Double convertedAltitude = feet * 0.3048;
                    mainTable.setValueAt(convertedAltitude, i, selectedColIndex);
                }

                if (temp != null && altitudeType == 2) {
                    //do nothing, altitude is already specified in meeters
                }

                if (temp != null && altitudeType == 3) {
                    Double kilometers = new Double(temp.toString());
                    Double convertedAltitude = kilometers / 1000;
                    mainTable.setValueAt(convertedAltitude, i, selectedColIndex);
                }
            }
            columnStatus.put(selectedColIndex, "Altitude");
            statusBar.setMessage("Column data type set to - Altitude. Values converted to KML format.");
        }

        //if Latitude
        if (coordinateType == 0 && jRadioButtonCoordinates.isSelected()) {
            int rowCount = mainTable.getRowCount();
            for (int i = 0; i < rowCount; i++) {
                //get row from the selected column
                Object temp = mainTable.getValueAt(i, selectedColIndex);
                if (temp != null && temp.toString().length() == 5) {//latitude without seconds

                    //Uses BigDecimal in order to enabe control of precision after the .
                    //and get precise coordinate value

                    //Get subSting of a number and convert it to BigDecimal
                    BigDecimal degrees = new BigDecimal(Double.parseDouble(
                            temp.toString().substring(0, 2)));
                    BigDecimal minutes = new BigDecimal(Double.parseDouble(
                            temp.toString().substring(2, 4)));
                    BigDecimal sixty = new BigDecimal(60.00);
                    String direction = temp.toString().substring(4);
                    //Decimal Degrees = Degrees + minutes/60
                    BigDecimal convertedCoordinates = degrees.add(minutes.divide(sixty, new MathContext(5)));
                    //negate coordinate if it is -> South
                    if (direction.toLowerCase().equals("s")) {
                        convertedCoordinates.negate();
                    }
                    mainTable.setValueAt(convertedCoordinates, i, selectedColIndex);
                } else if (temp != null && temp.toString().length() == 7) {//latitude with seconds

                    //Uses BigDecimal in order to enabe control of precision after the .
                    //and get precise coordinate value

                    //Get subSting of a number and convert it to BigDecimal
                    BigDecimal degrees = new BigDecimal(Double.parseDouble(
                            temp.toString().substring(0, 2)));

                    BigDecimal minutes = new BigDecimal(Double.parseDouble(
                            temp.toString().substring(2, 4)));
                    minutes = minutes.divide(new BigDecimal(60), new MathContext(5));

                    BigDecimal seconds = new BigDecimal(Double.parseDouble(
                            temp.toString().substring(4, 6)));
                    seconds = seconds.divide(new BigDecimal(3600), new MathContext(5));

                    String direction = temp.toString().substring(6);
                    //Decimal Degrees = Degrees + minutes/60
                    BigDecimal convertedCoordinates = degrees.add(minutes.add(seconds), new MathContext(5));

                    //negate coordinate if it is -> South
                    if (direction.toLowerCase().equals("s")) {
                        convertedCoordinates.negate();
                    }
                    mainTable.setValueAt(convertedCoordinates, i, selectedColIndex);
                } else {
                    statusBar.setMessage("Data in selected column does not seem to be of appropriate type.");
                    return;
                }
                columnStatus.put(selectedColIndex, "Latitude");
            }
            statusBar.setMessage("Column data type set to - Latitude. Values converted to KML format.");
        }

        //if Longitude
        if (coordinateType == 1 && jRadioButtonCoordinates.isSelected()) {
            int rowCount = mainTable.getRowCount();
            for (int i = 0; i < rowCount; i++) {
                //get row from the selected column
                Object temp = mainTable.getValueAt(i, selectedColIndex);
                if (temp != null && temp.toString().length() == 6) { //Longitude without seconds
                    //Uses BigDecimal in order to enabe control of precision after the .
                    //and get precise coordinate value

                    //Get subSting of a number and convert it to BigDecimal
                    //degrees
                    BigDecimal degrees = new BigDecimal(Double.parseDouble(
                            temp.toString().substring(0, 3)));
                    //minutes/60
                    BigDecimal minutes = new BigDecimal(Double.parseDouble(
                            temp.toString().substring(3, 5)));
                    minutes = minutes.divide(new BigDecimal(60), new MathContext(5));

                    //direction
                    String direction = temp.toString().substring(5);

                    //Decimal Degrees = Degrees + minutes/60
                    BigDecimal convertedCoordinates = degrees.add(minutes, new MathContext(5));

                    //negate coordinate if it is -> West
                    if (direction.toLowerCase().equals("w")) {
                        convertedCoordinates = convertedCoordinates.negate();
                    }
                    mainTable.setValueAt(convertedCoordinates, i, selectedColIndex);
                } else if (temp != null && temp.toString().length() == 8) {//Longitude with seconds
                    //Uses BigDecimal in order to enabe control of precision after the .
                    //and get precise coordinate value

                    //Get subSting of a number and convert it to BigDecimal
                    //degrees
                    BigDecimal degrees = new BigDecimal(Double.parseDouble(
                            temp.toString().substring(0, 3)));
                    //minutes/60
                    BigDecimal minutes = new BigDecimal(Double.parseDouble(
                            temp.toString().substring(3, 5)));
                    minutes = minutes.divide(new BigDecimal(60), new MathContext(5));
                    //seconds/3600
                    BigDecimal seconds = new BigDecimal(Double.parseDouble(
                            temp.toString().substring(4, 7)));
                    seconds = seconds.divide(new BigDecimal(3600), new MathContext(5));
                    //direction
                    String direction = temp.toString().substring(7);

                    //Decimal Degrees = Degrees + minutes/60 + seconds/3600
                    BigDecimal convertedCoordinates = degrees.add(minutes.add(seconds), new MathContext(5));

                    //negate coordinate if it is -> West
                    if (direction.toLowerCase().equals("w")) {
                        convertedCoordinates = convertedCoordinates.negate();
                    }
                    mainTable.setValueAt(convertedCoordinates, i, selectedColIndex);
                } else {
                    statusBar.setMessage("Data in selected column does not seem to be of appropriate type.");
                    return;
                }
                columnStatus.put(selectedColIndex, "Longitude");
            }
            statusBar.setMessage("Column data type set to - Longitude. Values converted to KML format.");
        }
    }

    private boolean dataIsReadyForExport() {
        boolean nameColumnExistsAtLeastOnce = false;
        int nameColumnCount = 0;
        boolean descriptionColumnExistsAtLeastOnce = false;
        int descriptionColumnCount = 0;
        boolean longitudeColumnExistsOnce = false;
        int longitudeColumnCount = 0;
        boolean lattitudeColumnExistsOnce = false;
        int lattitudeColumnCount = 0;
        exportErrorsString = "";

        //Check we have at least one column marked as 'name'
        for (int columnIndex = 0; columnIndex < mainTable.getColumnCount(); columnIndex++) {
            if (columnStatus.get(columnIndex).equals("Name")) {
                nameColumnCount++;
            }
        }
        if (nameColumnCount > 0) {
            nameColumnExistsAtLeastOnce = true;
        } else {
            exportErrorsString = exportErrorsString + "No 'name' columns found.\n";
//            return false;
        }

        //Check we have at least one column marked as 'description'
        for (int columnIndex = 0; columnIndex < mainTable.getColumnCount(); columnIndex++) {
            if (columnStatus.get(columnIndex).equals("Description")) {
                descriptionColumnCount++;
            }
        }
        if (descriptionColumnCount > 0) {
            descriptionColumnExistsAtLeastOnce = true;
        } else {
            exportErrorsString = exportErrorsString + "No 'description' columns found.\n";
//            return false;
        }

        //Check that 'lattitude' column exists only once
        for (int columnIndex = 0; columnIndex < mainTable.getColumnCount(); columnIndex++) {
            if (columnStatus.get(columnIndex).equals("Latitude")) {
                lattitudeColumnCount++;
            }
        }
        if (lattitudeColumnCount == 1) {
            lattitudeColumnExistsOnce = true;
        } else {
            exportErrorsString = exportErrorsString + "Illegal number of 'lattitude' columns found. " +
                    lattitudeColumnCount + "\n";
//            return false;
        }

        //Check that 'longitude' column exists only once
        for (int columnIndex = 0; columnIndex < mainTable.getColumnCount(); columnIndex++) {
            if (columnStatus.get(columnIndex).equals("Longitude")) {
                longitudeColumnCount++;
            }
        }
        if (longitudeColumnCount == 1) {
            longitudeColumnExistsOnce = true;
        } else {
            exportErrorsString = exportErrorsString + "Illegal number of 'longitude' columns found. " +
                    longitudeColumnCount + "\n";
//            return false;
        }


        if (nameColumnExistsAtLeastOnce && descriptionColumnExistsAtLeastOnce &&
                lattitudeColumnExistsOnce && longitudeColumnExistsOnce) {
            return true;
        } else {
            return false;
        }

    }

    /**
     *
     *
     *
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    @Action
    public void exportKML() throws FileNotFoundException, IOException {

        if (!dataIsReadyForExport()) {
            statusBar.setMessage("");
            JOptionPane.showMessageDialog(mainPanel, "Sorry!\nData cannot be exported, " +
                    "required column type(s) are not set correctly.\n\n" +
                    "Following errors were detected:\n\n" +
                    exportErrorsString, "Export Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        File currentDirectory = new File(prefs.get("LAST_SAVE_DIR",
                System.getProperty("user.dir")));
        chooser.setCurrentDirectory(currentDirectory);
        chooser.setFileFilter(kmlFileFilter);
        chooser.setSelectedFile(new File(currentFileName + ".kml"));
        int returnVal = chooser.showSaveDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            prefs.put("LAST_SAVE_DIR", chooser.getCurrentDirectory().toString());
            File fFile = chooser.getSelectedFile();
            if (fFile.exists()) {
                int response = JOptionPane.showConfirmDialog(null,
                        "Overwrite existing file?", "Confirm Overwrite",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.CANCEL_OPTION) {
                    return;
                }
            }

            //all confirmed lets rock!!!s
            try {

                //get all unique folder names
                HashSet<String> folderNamesSet = new HashSet<String>();
                for (int col = 0; col <= mainTable.getColumnCount() - 1; col++) {
                    if (columnStatus.get(col).equals("Folder Name")) {
                        for (int i = 0; i < mainTable.getRowCount(); i++) {
                            folderNamesSet.add(mainTable.getValueAt(i, col).toString());
                        }
                    }
                }

                OutputStream fout = new FileOutputStream(chooser.getSelectedFile().getAbsolutePath());
                OutputStream bout = new BufferedOutputStream(fout);
                //Save file using Latin-1 encoding
                OutputStreamWriter out = new OutputStreamWriter(bout, "UTF8");

                out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
                out.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\r\n");
                out.write("<Folder> <name>KML Converter Folder</name><description>Converted points</description>\r\n");
                //pass through every row for each folder name
                for (Object folderName : folderNamesSet) {
                    out.write("<Folder> <name>" + folderName + "</name>\r\n");
                    //Define point icon
                    out.write("<Style id=\"aircraftIcon\">");
                    out.write("<IconStyle>");
                    out.write("<Icon>");
                    out.write("<href>http://maps.google.com/mapfiles/kml/shapes/airports.png</href>");
                    out.write("</Icon>");
                    out.write("</IconStyle>");
                    out.write("</Style>");
                    //for each row print following xml lines
                    for (int row = 0; row < mainTable.getRowCount(); row++) {
                        if (mainTable.getValueAt(row, 0).toString().equals(folderName)) {
                            out.write("<Placemark>\r\n");
                            out.write("<name>");
                            for (int col = 0; col <= mainTable.getColumnCount() - 1; col++) {
                                if (columnStatus.get(col).equals("Name")) {
                                    out.write(mainTable.getValueAt(row, col).
                                            toString().replace("&", "and") + " ");
                                }
                            }
                            out.write("</name>\r\n");

                            out.write("<description><![CDATA[\n");
                            for (int col = 0; col <= mainTable.getColumnCount() - 1; col++) {
                                if (columnStatus.get(col).equals("Description") &&
                                        mainTable.getValueAt(row, col) != null) {
                                    out.write(mainTable.getColumnName(col) + ": " +
                                            mainTable.getValueAt(row, col).
                                            toString() + "<br>");
                                }
                            }
                            out.write("\n]]></description>\r\n");


                            out.write("<styleUrl>#aircraftIcon</styleUrl>");
                            out.write("<Point>\r\n");
                            out.write("<altitudeMode>relativeToGround</altitudeMode>");
                            out.write("<coordinates>");
                            //implement while loop here in order no to print coordinate more that once
                            //and not to go through the row when value was already found
                            for (int col = 0; col <= mainTable.getColumnCount() - 1; col++) {
                                if (columnStatus.get(col).equals("Longitude")) {
                                    out.write(mainTable.getValueAt(row, col) + ",");
                                }
                            }

                            for (int col = 0; col <= mainTable.getColumnCount() - 1; col++) {
                                if (columnStatus.get(col).equals("Latitude")) {
                                    out.write(mainTable.getValueAt(row, col) + ",");
                                }
                            }
                            for (int col = 0; col <= mainTable.getColumnCount() - 1; col++) {
                                if (columnStatus.get(col).equals("Altitude")) {
                                    out.write(mainTable.getValueAt(row, col) + "");
                                }
                            }

                            out.write("</coordinates>\r\n");
                            out.write("</Point>\r\n");
                            out.write("</Placemark>\r\n");
                        }
                    }
                    out.write("</Folder>\r\n");
                }
                out.write("</Folder>\r\n");
                out.write("</kml>");

                out.flush();
                out.close();
                statusBar.setMessage("Data exported to KML file successfully!");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(KMLConverterApp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(KMLConverterApp.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        mainPanel = new javax.swing.JPanel();
        jTextFieldCSVFilePath = new javax.swing.JTextField();
        jButtonBrowse = new javax.swing.JButton();
        jLabelDataFile = new javax.swing.JLabel();
        mainTableJScrollPane = new javax.swing.JScrollPane();
        mainTable = new javax.swing.JTable();
        jPanelColumnDataFormat = new javax.swing.JPanel();
        jRadioButtonName = new javax.swing.JRadioButton();
        jRadioButtonDescription = new javax.swing.JRadioButton();
        jRadioButtonCoordinates = new javax.swing.JRadioButton();
        coordinateTypeSelector = new javax.swing.JComboBox();
        ConvertButton = new javax.swing.JButton();
        jRadioButtonAltitude = new javax.swing.JRadioButton();
        jRadioButtonSkip = new javax.swing.JRadioButton();
        altitudeTypeSelector = new javax.swing.JComboBox();
        jRadioButtonFolder = new javax.swing.JRadioButton();
        exportButton = new javax.swing.JButton();
        HowToPanel = new javax.swing.JPanel();
        HowToJLabel = new javax.swing.JLabel();
        statusBar = new kmlconverter.StatusBar();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        buttonGroup1 = new javax.swing.ButtonGroup();
        jTextField1 = new javax.swing.JTextField();

        mainPanel.setName("mainPanel"); // NOI18N

        jTextFieldCSVFilePath.setEditable(false);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(kmlconverter.KMLConverterApp.class).getContext().getResourceMap(KMLConverterView.class);
        jTextFieldCSVFilePath.setText(resourceMap.getString("jTextFieldCSVFilePath.text")); // NOI18N
        jTextFieldCSVFilePath.setName("jTextFieldCSVFilePath"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(kmlconverter.KMLConverterApp.class).getContext().getActionMap(KMLConverterView.class, this);
        jButtonBrowse.setAction(actionMap.get("openFile")); // NOI18N
        jButtonBrowse.setText(resourceMap.getString("jButtonBrowse.text")); // NOI18N
        jButtonBrowse.setName("jButtonBrowse"); // NOI18N

        jLabelDataFile.setText(resourceMap.getString("jLabelDataFile.text")); // NOI18N
        jLabelDataFile.setName("jLabelDataFile"); // NOI18N

        mainTableJScrollPane.setName("mainTableJScrollPane"); // NOI18N

        mainTable.setModel(kmlTableModel);
        mainTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        mainTable.setColumnSelectionAllowed(true);
        mainTable.setDragEnabled(true);
        mainTable.setGridColor(resourceMap.getColor("mainTable.gridColor")); // NOI18N
        mainTable.setName("mainTable"); // NOI18N
        mainTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        mainTable.getTableHeader().setReorderingAllowed(false);
        mainTableJScrollPane.setViewportView(mainTable);

        jPanelColumnDataFormat.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanelColumnDataFormat.border.title"))); // NOI18N
        jPanelColumnDataFormat.setName("jPanelColumnDataFormat"); // NOI18N

        buttonGroup1.add(jRadioButtonName);
        jRadioButtonName.setText(resourceMap.getString("jRadioButtonName.text")); // NOI18N
        jRadioButtonName.setName("jRadioButtonName"); // NOI18N

        buttonGroup1.add(jRadioButtonDescription);
        jRadioButtonDescription.setText(resourceMap.getString("jRadioButtonDescription.text")); // NOI18N
        jRadioButtonDescription.setName("jRadioButtonDescription"); // NOI18N

        buttonGroup1.add(jRadioButtonCoordinates);
        jRadioButtonCoordinates.setText(resourceMap.getString("jRadioButtonCoordinates.text")); // NOI18N
        jRadioButtonCoordinates.setName("jRadioButtonCoordinates"); // NOI18N

        coordinateTypeSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Latitude", "Longitude" }));
        coordinateTypeSelector.setName("coordinateTypeSelector"); // NOI18N

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, jRadioButtonCoordinates, org.jdesktop.beansbinding.ELProperty.create("${selected}"), coordinateTypeSelector, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        ConvertButton.setAction(actionMap.get("setCoulumDataType")); // NOI18N
        ConvertButton.setText(resourceMap.getString("ConvertButton.text")); // NOI18N
        ConvertButton.setName("ConvertButton"); // NOI18N

        buttonGroup1.add(jRadioButtonAltitude);
        jRadioButtonAltitude.setText(resourceMap.getString("jRadioButtonAltitude.text")); // NOI18N
        jRadioButtonAltitude.setName("jRadioButtonAltitude"); // NOI18N

        buttonGroup1.add(jRadioButtonSkip);
        jRadioButtonSkip.setText(resourceMap.getString("jRadioButtonSkip.text")); // NOI18N
        jRadioButtonSkip.setName("jRadioButtonSkip"); // NOI18N

        altitudeTypeSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Feet", "Flight Level", "Meters", "Kilometers" }));
        altitudeTypeSelector.setName("altitudeTypeSelector"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, jRadioButtonAltitude, org.jdesktop.beansbinding.ELProperty.create("${selected}"), altitudeTypeSelector, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        buttonGroup1.add(jRadioButtonFolder);
        jRadioButtonFolder.setText(resourceMap.getString("jRadioButtonFolder.text")); // NOI18N
        jRadioButtonFolder.setName("jRadioButtonFolder"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanelColumnDataFormatLayout = new org.jdesktop.layout.GroupLayout(jPanelColumnDataFormat);
        jPanelColumnDataFormat.setLayout(jPanelColumnDataFormatLayout);
        jPanelColumnDataFormatLayout.setHorizontalGroup(
            jPanelColumnDataFormatLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelColumnDataFormatLayout.createSequentialGroup()
                .add(jPanelColumnDataFormatLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(ConvertButton)
                    .add(jRadioButtonSkip)
                    .add(jPanelColumnDataFormatLayout.createSequentialGroup()
                        .add(jPanelColumnDataFormatLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jRadioButtonAltitude)
                            .add(jRadioButtonCoordinates)
                            .add(jRadioButtonDescription)
                            .add(jRadioButtonName))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanelColumnDataFormatLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanelColumnDataFormatLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                .add(altitudeTypeSelector, 0, 0, Short.MAX_VALUE)
                                .add(coordinateTypeSelector, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .add(jRadioButtonFolder))))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelColumnDataFormatLayout.setVerticalGroup(
            jPanelColumnDataFormatLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelColumnDataFormatLayout.createSequentialGroup()
                .add(jPanelColumnDataFormatLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jRadioButtonName)
                    .add(jRadioButtonFolder))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jRadioButtonDescription)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelColumnDataFormatLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jRadioButtonCoordinates)
                    .add(coordinateTypeSelector, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelColumnDataFormatLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jRadioButtonAltitude)
                    .add(altitudeTypeSelector, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jRadioButtonSkip)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ConvertButton))
        );

        exportButton.setAction(actionMap.get("exportKML")); // NOI18N
        exportButton.setText(resourceMap.getString("exportButton.text")); // NOI18N
        exportButton.setName("exportButton"); // NOI18N

        HowToPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("HowToPanel.border.title"))); // NOI18N
        HowToPanel.setName("HowToPanel"); // NOI18N

        HowToJLabel.setText(resourceMap.getString("howToDesc.text")); // NOI18N
        HowToJLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        HowToJLabel.setMaximumSize(new java.awt.Dimension(400, 16));
        HowToJLabel.setMinimumSize(new java.awt.Dimension(400, 16));
        HowToJLabel.setName("howToDesc"); // NOI18N
        HowToJLabel.setPreferredSize(new java.awt.Dimension(400, 16));

        org.jdesktop.layout.GroupLayout HowToPanelLayout = new org.jdesktop.layout.GroupLayout(HowToPanel);
        HowToPanel.setLayout(HowToPanelLayout);
        HowToPanelLayout.setHorizontalGroup(
            HowToPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, HowToPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(HowToJLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 479, Short.MAX_VALUE)
                .addContainerGap())
        );
        HowToPanelLayout.setVerticalGroup(
            HowToPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(HowToPanelLayout.createSequentialGroup()
                .add(HowToJLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)
                .addContainerGap())
        );

        HowToJLabel.getAccessibleContext().setAccessibleName(resourceMap.getString("howToDesc.AccessibleContext.accessibleName")); // NOI18N

        statusBar.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        statusBar.setName("statusBar"); // NOI18N

        org.jdesktop.layout.GroupLayout mainPanelLayout = new org.jdesktop.layout.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, mainTableJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 755, Short.MAX_VALUE)
                    .add(mainPanelLayout.createSequentialGroup()
                        .add(statusBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 617, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(exportButton))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, mainPanelLayout.createSequentialGroup()
                        .add(jLabelDataFile)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTextFieldCSVFilePath, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 586, Short.MAX_VALUE)
                        .add(18, 18, 18)
                        .add(jButtonBrowse))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, mainPanelLayout.createSequentialGroup()
                        .add(jPanelColumnDataFormat, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(HowToPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mainPanelLayout.createSequentialGroup()
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelDataFile)
                    .add(jTextFieldCSVFilePath, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButtonBrowse))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(HowToPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanelColumnDataFormat, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mainTableJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(exportButton)
                    .add(statusBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );

        jPanelColumnDataFormat.getAccessibleContext().setAccessibleName(resourceMap.getString("jPanel1.AccessibleContext.accessibleName")); // NOI18N
        HowToPanel.getAccessibleContext().setAccessibleName(resourceMap.getString("jPanel2.AccessibleContext.accessibleName")); // NOI18N

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        jMenuItem1.setAction(actionMap.get("openFile")); // NOI18N
        jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        fileMenu.add(jMenuItem1);

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        jTextField1.setText(resourceMap.getString("jTextField1.text")); // NOI18N
        jTextField1.setName("jTextField1"); // NOI18N

        setComponent(mainPanel);
        setMenuBar(menuBar);

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton ConvertButton;
    private javax.swing.JLabel HowToJLabel;
    private javax.swing.JPanel HowToPanel;
    private javax.swing.JComboBox altitudeTypeSelector;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox coordinateTypeSelector;
    private javax.swing.JButton exportButton;
    private javax.swing.JButton jButtonBrowse;
    private javax.swing.JLabel jLabelDataFile;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel jPanelColumnDataFormat;
    private javax.swing.JRadioButton jRadioButtonAltitude;
    private javax.swing.JRadioButton jRadioButtonCoordinates;
    private javax.swing.JRadioButton jRadioButtonDescription;
    private javax.swing.JRadioButton jRadioButtonFolder;
    private javax.swing.JRadioButton jRadioButtonName;
    private javax.swing.JRadioButton jRadioButtonSkip;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextFieldCSVFilePath;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JTable mainTable;
    private javax.swing.JScrollPane mainTableJScrollPane;
    private javax.swing.JMenuBar menuBar;
    private kmlconverter.StatusBar statusBar;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
    private JDialog aboutBox;
    private JFileChooser chooser = new JFileChooser();
    private AbstractTableModel kmlTableModel;
    private int selectedColIndex;
    private HashMap<Integer, String> columnStatus;
    String exportErrorsString;
    String currentFileName;
    KMLFileFilter kmlFileFilter;
    TXTFileFilter txtFileFilter;
    Preferences prefs;
}
