package kmlconverter;

/*
 * TableDemo.java requires no other files.
 */
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.HashMap;

class KMLjTableModel extends AbstractTableModel {

    private ArrayList<String> columnNames;
    private ArrayList<ArrayList> data;
    private HashMap<Integer, Object> longValues;

    // Constructor to be used when table is empty and there is nothing to display
    public KMLjTableModel() {
        columnNames = new ArrayList<String>();
        data = new ArrayList<ArrayList>();
        longValues = new HashMap<Integer, Object>();
    }

    // Constructor used when data to be diplayed exists
    public KMLjTableModel(ArrayList<String> columnNames, ArrayList<ArrayList> data) {
        this.columnNames = new ArrayList<String>();
        this.data = new ArrayList<ArrayList>();
        longValues = new HashMap<Integer, Object>();


        //Add column names to the relevant table variable
        for (String str : columnNames) {
            this.columnNames.add(str);
        }

        //process each element in arraylist of arraylists
        //if arraylist size matches number of colum in the table
        //add line to jTable
        //input array does not contain first line from csv file
        for (ArrayList line : data) {
            if (line.size() == this.columnNames.size()) {
                this.data.add(line);
            }
        }
        findLongValues(data);
    }

    public int getColumnCount() {
        return columnNames.size();
    }

    public int getRowCount() {
        return data.size();
    }

    @Override
    public String getColumnName(int col) {
        return columnNames.get(col);
    }

    public Object getValueAt(int row, int col) {
        ArrayList colArrayList = data.get(row);
        return colArrayList.get(col);
    }

    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c) == null ? String.class
                : getValueAt(0, c).getClass();
    }

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
            return true;
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        ArrayList colArrayList = data.get(row);
        colArrayList.remove(col);
        colArrayList.add(col, value);
        fireTableCellUpdated(row, col);
    }

    Object getColumnLongestValue(int i) {
        return longValues.get(i);
    }

    private synchronized void findLongValues(ArrayList<ArrayList> data) {
        int numberOfColumns = columnNames.size();
        ArrayList<Integer> columnLengths = new ArrayList<Integer>();
        //set inital column size to 0 for all columns
        for (int i = 0; i < numberOfColumns; i++) {
            columnLengths.add(i, 0);
        }
        //process each LINE in the table data array in roder to find lingest values in each column
        for (ArrayList tableLine : data) {
            int index = 0;
            //process each ELEMENT in the line comparing it length with the biggest value for it's column
            for (Object value : tableLine) {
                if (value != null) {
                    int thisElementLength = value.toString().length();
                    if (thisElementLength > columnLengths.get(index)) {
                        longValues.put(index, value);
                        columnLengths.add(index, thisElementLength);
                    }
                }
                index++;
            }
        }
    }

}