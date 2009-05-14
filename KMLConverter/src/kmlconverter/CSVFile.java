package kmlconverter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;



/** Class used to process CSV File in order to prepeare it to be
 *  displayed in main application main window.
 *
 * Has only two methods. One to process first line of the file where column names are expected to be.
 * And another to process the rest of the CSV file where actual data shuld be.
 *
 * @author Kiril Piskunov
 * @version 24/04/2009
 */
public class CSVFile {

    /**
     *
     * @param fileName - path to the CSV file to be imported
     * @param delimeter 
     * @return ArrayList<ArrayList> - procesed data
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public ArrayList<ArrayList> openFile(String fileName, String delimeter) throws
            FileNotFoundException, IOException {
        ArrayList<ArrayList> processedData = new ArrayList<ArrayList>();
        processedData = processData(new BufferedReader(new FileReader(fileName)), delimeter);
        processedData.get(0);
        
        return processedData;
    }

    /**
     *
     * @param fileName
     * @param delimiter
     * @return
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     * @throws WrongFileException
     */
    public ArrayList<String> openFileGetHeaders(String fileName, String delimiter) throws
        FileNotFoundException, IOException, WrongFileException {
        ArrayList<String> processedHeader = new ArrayList<String>();
        processedHeader = processHeader(new BufferedReader(new FileReader(fileName)), delimiter);

        //Check that imported file has at least 4 columns
        // Name,Description,Lattitude,Longitude
        if (processedHeader.size() < 4) {
            throw new WrongFileException();
        }

        return processedHeader;
    }

    /**
     *
     * @param is
     * @param delimiter
     * @return
     * @throws java.io.IOException
     */
    protected static ArrayList<ArrayList> processData(BufferedReader is, String delimiter) throws IOException {
        String line;
        ArrayList<Object> values = new ArrayList<Object>();
        ArrayList<ArrayList> data = new ArrayList<ArrayList>();
        is.readLine(); //skip first line where headers are stored

        //read every line, tokenize values 
        //add resulting values to vlaues variable - result it long array list of values
        //and there is no way to know where new line starts
        //this is taken care of late in class MyTableModel
        while ((line = is.readLine()) != null) {
            //System.out.println("line = `" + line + "'");
            StringTokenizer strTokenizer = new StringTokenizer(line, delimiter, true);
            int i = 0;
            boolean doubleDelim = true; // keep track of two consecutive delmeters

            //XXX: this one is still not working properly and if line
            //is finishing with null it is not added to jTable
            while (strTokenizer.hasMoreElements()) {
                Object element = strTokenizer.nextElement();
                if (!element.equals(delimiter)) {
                    values.add(cleanupLine(element.toString()));
                    doubleDelim = false;
                } else {
                    if (doubleDelim) {
                        values.add(null);
                    }
                    doubleDelim = true;
                }
            }
            data.add(values);
            values = new ArrayList<Object>();
        }
        return data;
    }

    //should be later changed to protected
    //as when this method is called headers are not prcessed
    /**
     *
     * @param is
     * @param delimiter
     * @return
     * @throws java.io.IOException
     */
    public static ArrayList<String> processHeader(BufferedReader is, String delimiter) throws IOException {
        String line;
        ArrayList<String> headerList = new ArrayList<String>();

        if (((line = is.readLine())) != null) {
            //System.out.println("header = `" + line + "'");
            StringTokenizer strTokenizer = new StringTokenizer(line, delimiter);
            while (strTokenizer.hasMoreElements()) {
                Object element = strTokenizer.nextElement();
                headerList.add(cleanupLine(element.toString()));
            }
        }
        return headerList;
    }

    private static String cleanupLine(String line) {
        String cleanLine = line.replace("&", "&amp");
        //Remove single quote on the front
        cleanLine = cleanLine.replaceAll("^\"", "");
        //Remove single quote at the EOL
        cleanLine = cleanLine.replaceAll("\"$", " ");
        //Replace double quotes with single quote
        cleanLine = cleanLine.replace("\"\"", "\"");
        cleanLine = cleanLine.replace(">", "&gt");
        cleanLine = cleanLine.replace("<", "&lt");
        return cleanLine;
    }
}