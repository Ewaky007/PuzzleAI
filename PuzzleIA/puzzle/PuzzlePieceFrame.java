package puzzle;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import java.util.List;
import java.text.DecimalFormat;

public class PuzzlePieceFrame extends JFrame {
    
    private final BufferedImage puzzlePiece;
    private BufferedImage mainImage;
    private final MainFrame mainFrame;
    private final Rectangle selectionRect;
    private JButton searchButton;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JTextArea infoArea;
    private JTextArea resultArea;
    private JLabel imageInfoLabel;
    private JLabel confidenceLabel;
    private JSlider maxMatchesSlider;
    private JLabel maxMatchesLabel;
    
    private static final DecimalFormat df = new DecimalFormat("#.##");

    /**
     * Constructeur de la fenêtre d'analyse de pièce.
     * 
     * @param puzzlePiece l'image de la pièce sélectionnée
     * @param mainImage l'image principale
     * @param mainFrame référence vers la fenêtre principale
     * @param selectionRect rectangle de sélection dans l'image principale
     */
    public PuzzlePieceFrame(BufferedImage puzzlePiece, BufferedImage mainImage, 
                           MainFrame mainFrame, Rectangle selectionRect) {
        this.puzzlePiece = puzzlePiece;
        this.mainImage = mainImage;
        this.mainFrame = mainFrame;
        this.selectionRect = selectionRect;
        
        initializeUI();
        updateInfo();
    }
    
    /**
     * Initialise l'interface utilisateur de la fenêtre.
     */
    private void initializeUI() {
        setTitle("Puzzle Piece Analysis - Detail de la piece");
        setSize(950, 750);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(mainFrame);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        
        JLabel titleLabel = new JLabel("Analyse de la piece selectionnee", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(70, 130, 180));
        topPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        
        JPanel leftColumn = new JPanel(new BorderLayout(10, 10));
        
        JPanel previewCard = new JPanel(new BorderLayout());
        previewCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                "Apercu de la piece",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                new Color(70, 130, 180)
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        Image scaledImage = puzzlePiece.getScaledInstance(300, 200, Image.SCALE_SMOOTH);
        ImageIcon icon = new ImageIcon(scaledImage);
        JLabel pieceImageLabel = new JLabel(icon);
        pieceImageLabel.setHorizontalAlignment(JLabel.CENTER);
        previewCard.add(pieceImageLabel, BorderLayout.CENTER);
        
        imageInfoLabel = new JLabel("", JLabel.CENTER);
        imageInfoLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        previewCard.add(imageInfoLabel, BorderLayout.SOUTH);
        
        leftColumn.add(previewCard, BorderLayout.NORTH);
        
        JPanel infoCard = new JPanel(new BorderLayout());
        infoCard.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            "Informations detaillees",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        
        infoArea = new JTextArea(15, 25);
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        infoArea.setBackground(new Color(245, 245, 250));
        infoArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane infoScroll = new JScrollPane(infoArea);
        infoScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        infoCard.add(infoScroll, BorderLayout.CENTER);
        
        leftColumn.add(infoCard, BorderLayout.CENTER);
        
        JPanel rightColumn = new JPanel(new BorderLayout(10, 10));
        
        JPanel controlCard = new JPanel(new GridBagLayout());
        controlCard.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(34, 139, 34), 1),
            "Controles de recherche",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14),
            new Color(34, 139, 34)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        
        searchButton = new JButton("LANCER LA RECHERCHE");
        searchButton.setFont(new Font("Arial", Font.BOLD, 16));
        searchButton.setBackground(new Color(70, 130, 180));
        searchButton.setForeground(Color.WHITE);
        searchButton.setPreferredSize(new Dimension(250, 50));
        searchButton.setFocusPainted(false);
        searchButton.setBorder(BorderFactory.createRaisedBevelBorder());
        controlCard.add(searchButton, gbc);
        
        JPanel maxMatchesPanel = new JPanel(new BorderLayout(5, 5));
        maxMatchesPanel.setBorder(BorderFactory.createTitledBorder("Nombre max de matches"));
        
        maxMatchesSlider = new JSlider(1, 10, 5);
        maxMatchesSlider.setMajorTickSpacing(1);
        maxMatchesSlider.setPaintTicks(true);
        maxMatchesSlider.setPaintLabels(true);
        maxMatchesSlider.setSnapToTicks(true);
        
        maxMatchesLabel = new JLabel("5 matches", JLabel.CENTER);
        maxMatchesLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        maxMatchesSlider.addChangeListener(e -> {
            maxMatchesLabel.setText(maxMatchesSlider.getValue() + " matches");
        });
        
        maxMatchesPanel.add(maxMatchesLabel, BorderLayout.NORTH);
        maxMatchesPanel.add(maxMatchesSlider, BorderLayout.CENTER);
        
        controlCard.add(maxMatchesPanel, gbc);
        
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(250, 20));
        controlCard.add(progressBar, gbc);
        
        statusLabel = new JLabel("Pret a chercher");
        statusLabel.setHorizontalAlignment(JLabel.CENTER);
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 13));
        statusLabel.setForeground(new Color(100, 100, 100));
        controlCard.add(statusLabel, gbc);
        
        confidenceLabel = new JLabel("Meilleur match: -", JLabel.CENTER);
        confidenceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        confidenceLabel.setForeground(new Color(204, 0, 0));
        controlCard.add(confidenceLabel, gbc);
        
        rightColumn.add(controlCard, BorderLayout.NORTH);
        
        JPanel resultCard = new JPanel(new BorderLayout());
        resultCard.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(255, 140, 0), 1),
            "Resultats de la recherche",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14),
            new Color(255, 140, 0)
        ));
        
        resultArea = new JTextArea(15, 30);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultArea.setBackground(Color.WHITE);
        resultArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        resultCard.add(resultScroll, BorderLayout.CENTER);
        
        rightColumn.add(resultCard, BorderLayout.CENTER);
        
        centerPanel.add(leftColumn);
        centerPanel.add(rightColumn);
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Fermer");
        closeButton.setFont(new Font("Arial", Font.PLAIN, 12));
        closeButton.addActionListener(e -> dispose());
        bottomPanel.add(closeButton);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        searchButton.addActionListener(e -> searchPuzzlePiece());
        getRootPane().setDefaultButton(searchButton);
    }
    
    /**
     * Met à jour les informations affichées sur la pièce.
     */
    private void updateInfo() {
        StringBuilder info = new StringBuilder();
        
        info.append("INFORMATIONS DE BASE\n");
        info.append("====================\n\n");
        
        info.append("POSITION DANS L'IMAGE PRINCIPALE:\n");
        info.append(String.format("  X: %d pixels\n", selectionRect.x));
        info.append(String.format("  Y: %d pixels\n\n", selectionRect.y));
        
        info.append("DIMENSIONS DE LA PIECE:\n");
        info.append(String.format("  Largeur: %d px\n", puzzlePiece.getWidth()));
        info.append(String.format("  Hauteur: %d px\n", puzzlePiece.getHeight()));
        info.append(String.format("  Surface: %d px\n", puzzlePiece.getWidth() * puzzlePiece.getHeight()));
        info.append(String.format("  Ratio: %.2f\n\n", 
            (float)puzzlePiece.getWidth() / puzzlePiece.getHeight()));
        
        info.append("CARACTERISTIQUES VISUELLES:\n");
        
        long totalR = 0, totalG = 0, totalB = 0;
        int pixels = puzzlePiece.getWidth() * puzzlePiece.getHeight();
        
        for (int y = 0; y < puzzlePiece.getHeight(); y++) {
            for (int x = 0; x < puzzlePiece.getWidth(); x++) {
                Color pixel = new Color(puzzlePiece.getRGB(x, y));
                totalR += pixel.getRed();
                totalG += pixel.getGreen();
                totalB += pixel.getBlue();
            }
        }
        
        int avgR = (int)(totalR / pixels);
        int avgG = (int)(totalG / pixels);
        int avgB = (int)(totalB / pixels);
        
        info.append(String.format("  RGB moyen: (%d, %d, %d)\n", avgR, avgG, avgB));
        info.append(String.format("  Teinte dominante: %s\n\n", getColorName(avgR, avgG, avgB)));
        
        info.append("PARAMETRES DE RECHERCHE:\n");
        info.append("  Methode: " + mainFrame.searchMethodSelector.getSelectedItem() + "\n");
        info.append("  Seuil: " + mainFrame.thresholdSlider.getValue() + "%\n");
        info.append("  Best match only: " + (mainFrame.highlightBestMatchOnly.isSelected() ? "Oui" : "Non") + "\n");
        info.append("  Max matches: " + maxMatchesSlider.getValue() + "\n");
        
        imageInfoLabel.setText(String.format("Image: %d x %d pixels", 
            puzzlePiece.getWidth(), puzzlePiece.getHeight()));
        
        infoArea.setText(info.toString());
    }
    
    /**
     * Détermine le nom de la couleur dominante à partir des composantes RGB.
     * 
     * @param r composante rouge
     * @param g composante verte
     * @param b composante bleue
     * @return nom de la couleur
     */
    private String getColorName(int r, int g, int b) {
        if (r > 200 && g > 200 && b > 200) return "Blanc";
        if (r < 50 && g < 50 && b < 50) return "Noir";
        if (r > 200 && g < 100 && b < 100) return "Rouge";
        if (r < 100 && g > 200 && b < 100) return "Vert";
        if (r < 100 && g < 100 && b > 200) return "Bleu";
        if (r > 200 && g > 200 && b < 100) return "Jaune";
        if (r > 200 && g < 100 && b > 200) return "Magenta";
        if (r < 100 && g > 200 && b > 200) return "Cyan";
        return "Multicolore";
    }

    /**
     * Lance la recherche de la pièce dans l'image principale.
     */
    private void searchPuzzlePiece() {
        searchButton.setEnabled(false);
        progressBar.setVisible(true);
        statusLabel.setText("Recherche en cours...");
        confidenceLabel.setText("Recherche...");
        
        int maxMatches = maxMatchesSlider.getValue();
        boolean bestOnly = mainFrame.highlightBestMatchOnly.isSelected();
        
        resultArea.setText("RECHERCHE EN COURS\n" +
                          "===================\n" +
                          "Methode: " + mainFrame.searchMethodSelector.getSelectedItem() + "\n" +
                          "Max matches: " + (bestOnly ? "1 (best only)" : maxMatches) + "\n" +
                          "Seuil: " + mainFrame.thresholdSlider.getValue() + "%\n" +
                          "Veuillez patienter...\n");
        
        new Thread(() -> {
            try {
                long startTime = System.currentTimeMillis();
                
                Mat mainMat = PuzzleSearch.bufferedImageToMat(mainImage);
                Mat pieceMat = PuzzleSearch.bufferedImageToMat(puzzlePiece);
                
                String selectedMethod = (String) mainFrame.searchMethodSelector.getSelectedItem();
                int threshold = mainFrame.thresholdSlider.getValue();
                
                List<MatchedRegion> matches = null;
                
                int effectiveMaxMatches = bestOnly ? 1 : maxMatches;
                
                switch (selectedMethod) {
                    case "Pattern Matching":
                        matches = PuzzleSearch.searchByPattern(mainMat, pieceMat, threshold/100.0, effectiveMaxMatches);
                        break;
                    case "Shape Matching":
                        matches = PuzzleSearch.searchByShapeAdvanced(mainMat, pieceMat, threshold/100.0, effectiveMaxMatches);
                        break;
                    case "Combined Search":
                        matches = PuzzleSearch.searchAllAndCombine(mainMat, pieceMat, threshold/100.0, effectiveMaxMatches);
                        break;
                }
                
                long elapsedTime = System.currentTimeMillis() - startTime;
                
                if (matches != null && !matches.isEmpty()) {
                    double bestConfidence = matches.get(0).getConfidence();
                    if (bestConfidence > 0.9999) bestConfidence = 0.9999;
                    String confidenceStr = df.format(bestConfidence * 100);
                    confidenceLabel.setText(String.format("Meilleur match: %s%%", confidenceStr));
                }
                
                if (matches != null) {
                    PuzzleSearch.drawMatches(mainMat, matches, bestOnly);
                }
                
                final List<MatchedRegion> finalMatches = matches;
                final long finalTime = elapsedTime;
                
                SwingUtilities.invokeLater(() -> {
                    displayResults(resultArea, finalMatches, finalTime, bestOnly, effectiveMaxMatches);
                    mainImage = PuzzleSearch.matToBufferedImage(mainMat);
                    mainFrame.updateMainImage(mainImage);
                    statusLabel.setText("Recherche terminee en " + finalTime + " ms");
                    progressBar.setVisible(false);
                    searchButton.setEnabled(true);
                    updateInfo();
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Erreur pendant la recherche");
                    progressBar.setVisible(false);
                    searchButton.setEnabled(true);
                    confidenceLabel.setText("Erreur");
                    resultArea.setText("ERREUR\n======\n" + e.getMessage());
                    e.printStackTrace();
                });
            }
        }).start();
    }
    
    /**
     * Affiche les résultats de la recherche dans la zone de texte.
     * 
     * @param resultArea zone de texte pour les résultats
     * @param matches liste des correspondances trouvées
     * @param time temps d'exécution en millisecondes
     * @param bestOnly indique si seulement le meilleur match est affiché
     * @param maxMatches nombre maximum de matches demandé
     */
    private void displayResults(JTextArea resultArea, List<MatchedRegion> matches, long time, 
                                boolean bestOnly, int maxMatches) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("RECHERCHE TERMINEE\n");
        sb.append("==================\n\n");
        
        sb.append(String.format("Temps: %d ms\n", time));
        sb.append(String.format("Correspondances: %d", matches != null ? matches.size() : 0));
        
        if (bestOnly) {
            sb.append(" (mode best only)\n");
        } else {
            sb.append(" (max demande: " + maxMatches + ")\n");
        }
        sb.append("\n");
        
        if (matches == null || matches.isEmpty()) {
            sb.append("AUCUNE CORRESPONDANCE TROUVEE\n");
            sb.append("Suggestions:\n");
            sb.append("  Baisser le seuil (" + mainFrame.thresholdSlider.getValue() + "% -> plus bas)\n");
            sb.append("  Changer de methode de recherche\n");
            sb.append("  Selectionner une zone plus caracteristique\n");
        } else {
            sb.append("DETAIL DES MATCHES\n");
            sb.append("==================\n\n");
            
            sb.append("Les matches ne se chevauchent pas\n\n");
            
            for (int i = 0; i < matches.size(); i++) {
                MatchedRegion match = matches.get(i);
                double confidence = match.getConfidence();
                if (confidence > 0.9999) confidence = 0.9999;
                
                sb.append(String.format("MATCH #%d\n", i+1));
                sb.append(String.format("  Confiance: %.2f%%\n", confidence * 100));
                sb.append(String.format("  Position: (%d, %d)\n", 
                    match.getRegion().x, match.getRegion().y));
                sb.append(String.format("  Dimensions: %d x %d\n", 
                    match.getRegion().width, match.getRegion().height));
                
                sb.append("  Qualite: ");
                if (confidence > 0.9) sb.append("Excellent\n");
                else if (confidence > 0.8) sb.append("Tres bon\n");
                else if (confidence > 0.7) sb.append("Bon\n");
                else if (confidence > 0.6) sb.append("Moyen\n");
                else if (confidence > 0.5) sb.append("Faible\n");
                else sb.append("Tres faible\n");
                
                if (i < matches.size() - 1) sb.append("  ----------\n");
            }
        }
        
        resultArea.setText(sb.toString());
        resultArea.setCaretPosition(0);
    }
}