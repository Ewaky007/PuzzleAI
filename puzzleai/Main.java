package puzzleai;

import org.opencv.core.Core;
import javax.swing.SwingUtilities;

public class Main {
    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); } // Load OpenCV library

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}
