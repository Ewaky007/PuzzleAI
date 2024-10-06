package puzzleai;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class MainFrame extends JFrame {
    private BufferedImage mainImage;
    private ArrayList<Point> selectedPoints = new ArrayList<>();
    private final JLabel imageLabel;

    public MainFrame() {
        // Initialize components
        setTitle("Puzzle Search");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        imageLabel = new JLabel();
        JScrollPane scrollPane = new JScrollPane(imageLabel);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        JButton loadImageButton = new JButton("Load Image");
        bottomPanel.add(loadImageButton);
        add(bottomPanel, BorderLayout.SOUTH);

        loadImageButton.addActionListener(e -> loadImage());

        imageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (selectedPoints.size() < 2) {
                    selectedPoints.add(e.getPoint());
                    if (selectedPoints.size() == 2) {
                        extractSelectedArea();
                    }
                }
            }
        });
    }

    private void loadImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose an image");
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                mainImage = ImageIO.read(selectedFile);
                imageLabel.setIcon(new ImageIcon(mainImage));
                selectedPoints.clear();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to load image", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void extractSelectedArea() {
        Point p1 = selectedPoints.get(0);
        Point p2 = selectedPoints.get(1);
        Rectangle rect = new Rectangle(p1);
        rect.add(p2);

        BufferedImage selectedArea = mainImage.getSubimage(rect.x, rect.y, rect.width, rect.height);
        PuzzlePieceFrame pieceFrame = new PuzzlePieceFrame(selectedArea, mainImage, this);
        pieceFrame.setVisible(true);

        selectedPoints.clear();
    }

    public void updateMainImage(BufferedImage updatedImage) {
        this.mainImage = updatedImage;
        imageLabel.setIcon(new ImageIcon(updatedImage));
    }
}
