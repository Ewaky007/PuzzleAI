package puzzle;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class MainFrame extends JFrame {
    
    private BufferedImage mainImage;
    private BufferedImage originalImage;
    private Point startPoint;
    private Point endPoint;
    private Point imageStartPoint;
    private Point imageEndPoint;
    private final JLabel imageLabel;
    private boolean dragging;
    
    public JComboBox<String> searchMethodSelector;
    public JCheckBox highlightBestMatchOnly;
    public JSlider thresholdSlider;
    private JLabel statusLabel;
    private JButton clearHighlightsButton;
    private JButton resetButton;

    /**
     * Constructeur de la fenêtre principale.
     * Initialise l'interface utilisateur.
     */
    public MainFrame() {
        imageLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (dragging && startPoint != null && endPoint != null) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setColor(new Color(255, 0, 0, 50));
                    g2d.setStroke(new BasicStroke(2));
                    
                    int x = Math.min(startPoint.x, endPoint.x);
                    int y = Math.min(startPoint.y, endPoint.y);
                    int width = Math.abs(endPoint.x - startPoint.x);
                    int height = Math.abs(endPoint.y - startPoint.y);
                    
                    g2d.fillRect(x, y, width, height);
                    g2d.setColor(Color.RED);
                    g2d.drawRect(x, y, width, height);
                }
            }
        };
        
        initializeUI();
    }
    
    /**
     * Initialise tous les composants de l'interface.
     */
    private void initializeUI() {
        setTitle("Puzzle Search AI - L1 Project");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        JScrollPane scrollPane = new JScrollPane(imageLabel);
        add(scrollPane, BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new GridLayout(2, 4, 10, 5));
        topPanel.setBorder(BorderFactory.createTitledBorder("Search Controls"));
        
        searchMethodSelector = new JComboBox<>(new String[]{
            "Pattern Matching", 
            "Shape Matching", 
            "Combined Search"
        });
        
        highlightBestMatchOnly = new JCheckBox("Show Best Match Only", true);
        
        thresholdSlider = new JSlider(0, 100, 70);
        thresholdSlider.setMajorTickSpacing(20);
        thresholdSlider.setMinorTickSpacing(5);
        thresholdSlider.setPaintTicks(true);
        thresholdSlider.setPaintLabels(true);
        thresholdSlider.setToolTipText("Matching threshold (higher = more strict)");
        
        JLabel thresholdLabel = new JLabel("Threshold: " + thresholdSlider.getValue() + "%");
        thresholdSlider.addChangeListener(e -> 
            thresholdLabel.setText("Threshold: " + thresholdSlider.getValue() + "%")
        );
        
        clearHighlightsButton = new JButton("Clear Highlights");
        clearHighlightsButton.addActionListener(e -> clearHighlights());
        
        topPanel.add(new JLabel("Search Method:"));
        topPanel.add(searchMethodSelector);
        topPanel.add(thresholdLabel);
        topPanel.add(thresholdSlider);
        topPanel.add(highlightBestMatchOnly);
        topPanel.add(clearHighlightsButton);
        topPanel.add(new JLabel(""));
        topPanel.add(new JLabel(""));
        
        add(topPanel, BorderLayout.NORTH);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton loadImageButton = new JButton("Load Image");
        loadImageButton.setFont(new Font("Arial", Font.BOLD, 12));
        loadImageButton.addActionListener(e -> loadImage());
        buttonPanel.add(loadImageButton);
        
        JButton saveResultButton = new JButton("Save Result");
        saveResultButton.setFont(new Font("Arial", Font.BOLD, 12));
        saveResultButton.addActionListener(e -> saveResult());
        buttonPanel.add(saveResultButton);
        
        resetButton = new JButton("Reset Selection");
        resetButton.setFont(new Font("Arial", Font.BOLD, 12));
        resetButton.addActionListener(e -> resetSelection());
        buttonPanel.add(resetButton);
        
        JButton restoreButton = new JButton("Restore Original");
        restoreButton.setFont(new Font("Arial", Font.BOLD, 12));
        restoreButton.addActionListener(e -> restoreOriginalImage());
        buttonPanel.add(restoreButton);
        
        statusLabel = new JLabel("Ready - Load an image and drag to select a puzzle piece");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        bottomPanel.add(buttonPanel, BorderLayout.NORTH);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);
        
        add(bottomPanel, BorderLayout.SOUTH);

        imageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (mainImage == null) {
                    JOptionPane.showMessageDialog(MainFrame.this, 
                        "Please load an image first!", "No Image", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                startPoint = e.getPoint();
                imageStartPoint = convertToImageCoordinates(e.getPoint());
                if (imageStartPoint != null) {
                    dragging = true;
                    statusLabel.setText("Drag to select area...");
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (dragging && imageStartPoint != null) {
                    endPoint = e.getPoint();
                    imageEndPoint = convertToImageCoordinates(e.getPoint());
                    dragging = false;
                    
                    if (imageEndPoint != null) {
                        extractSelectedArea();
                    }
                    
                    startPoint = null;
                    endPoint = null;
                    imageStartPoint = null;
                    imageEndPoint = null;
                    imageLabel.repaint();
                }
            }
        });

        imageLabel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragging) {
                    endPoint = e.getPoint();
                    imageLabel.repaint();
                }
            }
        });
    }

    /**
     * Convertit les coordonnées de la souris en coordonnées réelles de l'image.
     * 
     * @param mousePoint point dans le repère du JLabel
     * @return point dans le repère de l'image, ou null si hors de l'image
     */
    private Point convertToImageCoordinates(Point mousePoint) {
        if (mainImage == null || imageLabel.getIcon() == null) return null;
        
        Icon icon = imageLabel.getIcon();
        int iconWidth = icon.getIconWidth();
        int iconHeight = icon.getIconHeight();
        
        int labelWidth = imageLabel.getWidth();
        int labelHeight = imageLabel.getHeight();
        
        int xOffset = (labelWidth - iconWidth) / 2;
        int yOffset = (labelHeight - iconHeight) / 2;
        
        if (mousePoint.x < xOffset || mousePoint.x > xOffset + iconWidth ||
            mousePoint.y < yOffset || mousePoint.y > yOffset + iconHeight) {
            return null;
        }
        
        int imageX = mousePoint.x - xOffset;
        int imageY = mousePoint.y - yOffset;
        
        double scaleX = (double) mainImage.getWidth() / iconWidth;
        double scaleY = (double) mainImage.getHeight() / iconHeight;
        
        int originalX = (int) (imageX * scaleX);
        int originalY = (int) (imageY * scaleY);
        
        originalX = Math.max(0, Math.min(originalX, mainImage.getWidth() - 1));
        originalY = Math.max(0, Math.min(originalY, mainImage.getHeight() - 1));
        
        return new Point(originalX, originalY);
    }

    /**
     * Ouvre un dialogue pour charger une image.
     */
    private void loadImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose an image");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Image files", "jpg", "jpeg", "png", "bmp", "gif"));
            
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                originalImage = ImageIO.read(selectedFile);
                
                if (originalImage.getWidth() > 1200 || originalImage.getHeight() > 800) {
                    mainImage = resizeImage(originalImage, 1200, 800);
                    originalImage = copyImage(mainImage);
                } else {
                    mainImage = copyImage(originalImage);
                }
                
                ImageIcon icon = new ImageIcon(mainImage);
                imageLabel.setIcon(icon);
                imageLabel.setSize(icon.getIconWidth(), icon.getIconHeight());
                
                startPoint = null;
                endPoint = null;
                statusLabel.setText("Image loaded. Drag to select a puzzle piece.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Failed to load image: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Crée une copie d'une image.
     * 
     * @param source image source
     * @return copie de l'image
     */
    private BufferedImage copyImage(BufferedImage source) {
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        Graphics2D g2d = copy.createGraphics();
        g2d.drawImage(source, 0, 0, null);
        g2d.dispose();
        return copy;
    }
    
    /**
     * Redimensionne une image aux dimensions maximales spécifiées.
     * 
     * @param original image originale
     * @param maxWidth largeur maximale
     * @param maxHeight hauteur maximale
     * @return image redimensionnée
     */
    private BufferedImage resizeImage(BufferedImage original, int maxWidth, int maxHeight) {
        int newWidth = original.getWidth();
        int newHeight = original.getHeight();
        
        if (newWidth > maxWidth) {
            newHeight = (newHeight * maxWidth) / newWidth;
            newWidth = maxWidth;
        }
        
        if (newHeight > maxHeight) {
            newWidth = (newWidth * maxHeight) / newHeight;
            newHeight = maxHeight;
        }
        
        Image tmp = original.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        
        return resized;
    }

    /**
     * Extrait la zone sélectionnée pour créer une pièce de puzzle.
     */
    private void extractSelectedArea() {
        if (imageStartPoint == null || imageEndPoint == null) return;
        
        int x1 = imageStartPoint.x;
        int y1 = imageStartPoint.y;
        int x2 = imageEndPoint.x;
        int y2 = imageEndPoint.y;
        
        Rectangle rect = new Rectangle(Math.min(x1, x2), Math.min(y1, y2),
                                      Math.abs(x2 - x1), Math.abs(y2 - y1));
        
        if (rect.width < 10 || rect.height < 10) {
            JOptionPane.showMessageDialog(this, 
                "Selected area is too small! Please select a larger area.",
                "Invalid Selection", JOptionPane.WARNING_MESSAGE);
            statusLabel.setText("Selection too small. Please try again.");
            return;
        }

        try {
            BufferedImage selectedArea = mainImage.getSubimage(rect.x, rect.y, rect.width, rect.height);
            PuzzlePieceFrame pieceFrame = new PuzzlePieceFrame(selectedArea, mainImage, this, rect);
            pieceFrame.setVisible(true);
            statusLabel.setText("Puzzle piece extracted. Use the new window to search.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error extracting area: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Met à jour l'image principale affichée.
     * 
     * @param updatedImage nouvelle image à afficher
     */
    public void updateMainImage(BufferedImage updatedImage) {
        this.mainImage = updatedImage;
        imageLabel.setIcon(new ImageIcon(updatedImage));
        statusLabel.setText("Search completed!");
    }
    
    /**
     * Efface les surlignages en restaurant l'image originale.
     */
    private void clearHighlights() {
        if (mainImage != null && originalImage != null) {
            mainImage = copyImage(originalImage);
            imageLabel.setIcon(new ImageIcon(mainImage));
            statusLabel.setText("Highlights cleared.");
        } else {
            statusLabel.setText("No image to clear.");
        }
    }
    
    /**
     * Réinitialise la sélection en cours.
     */
    private void resetSelection() {
        startPoint = null;
        endPoint = null;
        imageStartPoint = null;
        imageEndPoint = null;
        imageLabel.repaint();
        statusLabel.setText("Selection reset. Drag to select a new area.");
    }
    
    /**
     * Restaure l'image originale sans aucune modification.
     */
    private void restoreOriginalImage() {
        if (originalImage != null) {
            mainImage = copyImage(originalImage);
            imageLabel.setIcon(new ImageIcon(mainImage));
            statusLabel.setText("Original image restored.");
        } else {
            statusLabel.setText("No original image to restore.");
        }
    }
    
    /**
     * Sauvegarde l'image actuelle dans un fichier.
     */
    private void saveResult() {
        if (mainImage == null) {
            JOptionPane.showMessageDialog(this, "No image to save!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Result Image");
        fileChooser.setSelectedFile(new File("puzzle_result.png"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File outputFile = fileChooser.getSelectedFile();
                String extension = outputFile.getName().substring(outputFile.getName().lastIndexOf('.') + 1);
                ImageIO.write(mainImage, extension, outputFile);
                JOptionPane.showMessageDialog(this, "Image saved successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error saving image: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}