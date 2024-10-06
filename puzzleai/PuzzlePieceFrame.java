package puzzleai;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import org.opencv.core.Mat;

public class PuzzlePieceFrame extends JFrame {
    private final BufferedImage puzzlePiece;
    private BufferedImage mainImage;
    private final MainFrame mainFrame;

    public PuzzlePieceFrame(BufferedImage puzzlePiece, BufferedImage mainImage, MainFrame mainFrame) {
        this.puzzlePiece = puzzlePiece;
        this.mainImage = mainImage;
        this.mainFrame = mainFrame;
        setTitle("Puzzle Piece");
        setSize(300, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JLabel pieceLabel = new JLabel(new ImageIcon(puzzlePiece));
        add(pieceLabel, BorderLayout.CENTER);

        // Add search button
        JButton searchButton = new JButton("Search Puzzle Piece");
        searchButton.addActionListener(e -> searchPuzzlePiece());
        add(searchButton, BorderLayout.SOUTH);
    }

    private void searchPuzzlePiece() {
        Mat mainMat = PuzzleSearch.bufferedImageToMat(mainImage);
        Mat pieceMat = PuzzleSearch.bufferedImageToMat(puzzlePiece);

        System.out.println("Starting shape search...");
        PuzzleSearch.searchByShape(mainMat, pieceMat);
        System.out.println("Shape search completed.");

        System.out.println("Starting color search...");
        PuzzleSearch.searchByColor(mainMat, pieceMat);
        System.out.println("Color search completed.");

        System.out.println("Starting pattern search...");
        PuzzleSearch.searchByPattern(mainMat, pieceMat);
        System.out.println("Pattern search completed.");

        mainImage = PuzzleSearch.matToBufferedImage(mainMat);
        mainFrame.updateMainImage(mainImage);
    }
}
