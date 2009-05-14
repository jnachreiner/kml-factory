    package kmlconverter;

    import java.io.File;
    import javax.swing.filechooser.FileFilter;

    /* ImageFilter.java is used by KMLConverterView.java. */
    /**
     *
     * @author kiril
     */
    public class KMLFileFilter extends FileFilter {

        private static final String kml = "kml";

        //Accept all directories and all kml files.
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            String extension = getExtension(f);
            if (extension != null) {
                if (extension.equals(kml)) {
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        }

        //The description of this filter
        public String getDescription() {
            return "KML files (*.kml)";
        }

        private static String getExtension(File f) {
            String ext = null;
            String s = f.getName();
            int i = s.lastIndexOf('.');

            if (i > 0 && i < s.length() - 1) {
                ext = s.substring(i + 1).toLowerCase();
            }
            return ext;
        }

    }
