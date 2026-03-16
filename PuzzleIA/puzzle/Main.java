package puzzle;

import org.opencv.core.Core;
import javax.swing.SwingUtilities;

public class Main {
    
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    /**
     * Point d'entrée principal de l'application.
     * Initialise et affiche la fenêtre principale.
     * 
     * @param args arguments de la ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}