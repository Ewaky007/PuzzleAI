package puzzle;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class PuzzleSearch {
    
    /**
     * Convertit une BufferedImage en Mat OpenCV.
     * 
     * @param image l'image à convertir
     * @return l'image au format Mat
     */
    public static Mat bufferedImageToMat(BufferedImage image) {
        BufferedImage convertedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        Graphics g = convertedImage.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        
        Mat mat = new Mat(convertedImage.getHeight(), convertedImage.getWidth(), CvType.CV_8UC3);
        byte[] data = ((DataBufferByte) convertedImage.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);
        return mat;
    }
    
    /**
     * Convertit un Mat OpenCV en BufferedImage.
     * 
     * @param mat l'image au format Mat
     * @return l'image au format BufferedImage
     */
    public static BufferedImage matToBufferedImage(Mat mat) {
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), BufferedImage.TYPE_3BYTE_BGR);
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        mat.get(0, 0, data);
        return image;
    }

    /**
     * Recherche par template matching avec limitation et sans chevauchement.
     * 
     * @param mainImage l'image principale
     * @param puzzlePiece la pièce à rechercher
     * @param threshold le seuil de confiance minimum
     * @param maxMatches le nombre maximum de matches
     * @return liste des matches trouvés
     */
    public static List<MatchedRegion> searchByPattern(Mat mainImage, Mat puzzlePiece, double threshold, int maxMatches) {
        List<MatchedRegion> matches = new ArrayList<>();
        
        Mat result = new Mat();
        Imgproc.matchTemplate(mainImage, puzzlePiece, result, Imgproc.TM_CCOEFF_NORMED);
        
        List<MatchedRegion> allMatches = new ArrayList<>();
        
        while (true) {
            Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
            if (mmr.maxVal < threshold) break;
            
            Point matchLoc = mmr.maxLoc;
            allMatches.add(new MatchedRegion(
                new Rect((int)matchLoc.x, (int)matchLoc.y, puzzlePiece.cols(), puzzlePiece.rows()),
                mmr.maxVal
            ));
            
            Imgproc.rectangle(result, 
                new Point(matchLoc.x - puzzlePiece.cols()/2, matchLoc.y - puzzlePiece.rows()/2),
                new Point(matchLoc.x + puzzlePiece.cols()/2, matchLoc.y + puzzlePiece.rows()/2),
                new Scalar(0), -1);
        }
        
        matches = filterNonOverlapping(allMatches, maxMatches);
        
        return matches;
    }

    /**
     * Recherche par forme (contours) avec limitation et sans chevauchement.
     * 
     * @param mainImage l'image principale
     * @param puzzlePiece la pièce à rechercher
     * @param threshold le seuil de confiance minimum
     * @param maxMatches le nombre maximum de matches
     * @return liste des matches trouvés
     */
    public static List<MatchedRegion> searchByShapeAdvanced(Mat mainImage, Mat puzzlePiece, double threshold, int maxMatches) {
        List<MatchedRegion> matches = new ArrayList<>();
        
        Mat mainGray = new Mat();
        Mat pieceGray = new Mat();
        Imgproc.cvtColor(mainImage, mainGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(puzzlePiece, pieceGray, Imgproc.COLOR_BGR2GRAY);

        Mat mainEdges = new Mat();
        Mat pieceEdges = new Mat();
        Imgproc.Canny(mainGray, mainEdges, 50, 150);
        Imgproc.Canny(pieceGray, pieceEdges, 50, 150);

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.dilate(mainEdges, mainEdges, kernel);
        Imgproc.dilate(pieceEdges, pieceEdges, kernel);

        Mat result = new Mat();
        Imgproc.matchTemplate(mainEdges, pieceEdges, result, Imgproc.TM_CCOEFF_NORMED);
        
        List<MatchedRegion> allMatches = new ArrayList<>();
        
        while (true) {
            Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
            if (mmr.maxVal < threshold) break;
            
            Point matchLoc = mmr.maxLoc;
            allMatches.add(new MatchedRegion(
                new Rect((int)matchLoc.x, (int)matchLoc.y, puzzlePiece.cols(), puzzlePiece.rows()),
                mmr.maxVal
            ));
            
            Imgproc.rectangle(result, 
                new Point(matchLoc.x - puzzlePiece.cols()/2, matchLoc.y - puzzlePiece.rows()/2),
                new Point(matchLoc.x + puzzlePiece.cols()/2, matchLoc.y + puzzlePiece.rows()/2),
                new Scalar(0), -1);
        }
        
        matches = filterNonOverlapping(allMatches, maxMatches);
        
        return matches;
    }

    /**
     * Filtre une liste de matches pour éviter les chevauchements.
     * 
     * @param matches la liste complète des matches
     * @param maxMatches le nombre maximum à conserver
     * @return liste filtrée sans chevauchement
     */
    private static List<MatchedRegion> filterNonOverlapping(List<MatchedRegion> matches, int maxMatches) {
        if (matches.isEmpty()) return matches;
        
        Collections.sort(matches, (m1, m2) -> Double.compare(m2.getConfidence(), m1.getConfidence()));
        
        List<MatchedRegion> filtered = new ArrayList<>();
        
        for (MatchedRegion candidate : matches) {
            boolean overlaps = false;
            
            for (MatchedRegion selected : filtered) {
                if (rectsOverlap(candidate.getRegion(), selected.getRegion(), 0.1)) {
                    overlaps = true;
                    break;
                }
            }
            
            if (!overlaps) {
                filtered.add(candidate);
                if (filtered.size() >= maxMatches) {
                    break;
                }
            }
        }
        
        return filtered;
    }

    /**
     * Vérifie si deux rectangles se chevauchent au-delà d'un certain seuil.
     * 
     * @param r1 premier rectangle
     * @param r2 second rectangle
     * @param maxOverlapRatio ratio de chevauchement maximum autorisé
     * @return true si les rectangles se chevauchent trop
     */
    private static boolean rectsOverlap(Rect r1, Rect r2, double maxOverlapRatio) {
        int x1 = Math.max(r1.x, r2.x);
        int y1 = Math.max(r1.y, r2.y);
        int x2 = Math.min(r1.x + r1.width, r2.x + r2.width);
        int y2 = Math.min(r1.y + r1.height, r2.y + r2.height);
        
        if (x2 <= x1 || y2 <= y1) return false;
        
        int intersectionWidth = x2 - x1;
        int intersectionHeight = y2 - y1;
        double intersectionArea = intersectionWidth * intersectionHeight;
        
        double minArea = Math.min(r1.area(), r2.area());
        
        return (intersectionArea / minArea) > maxOverlapRatio;
    }

    /**
     * Recherche multi-échelle avec limitation et sans chevauchement.
     * 
     * @param mainImage l'image principale
     * @param puzzlePiece la pièce à rechercher
     * @param scaleStart échelle de début
     * @param scaleEnd échelle de fin
     * @param scaleStep pas d'échelle
     * @param maxMatches nombre maximum de matches
     * @return liste des matches trouvés
     */
    public static List<MatchedRegion> searchMultiScale(Mat mainImage, Mat puzzlePiece, 
                                                       double scaleStart, double scaleEnd, 
                                                       double scaleStep, int maxMatches) {
        List<MatchedRegion> allMatches = new ArrayList<>();
        
        for (double scale = scaleStart; scale <= scaleEnd; scale += scaleStep) {
            Mat scaledPiece = new Mat();
            Size scaledSize = new Size((int)(puzzlePiece.cols() * scale), (int)(puzzlePiece.rows() * scale));
            Imgproc.resize(puzzlePiece, scaledPiece, scaledSize);
            
            Mat result = new Mat();
            Imgproc.matchTemplate(mainImage, scaledPiece, result, Imgproc.TM_CCOEFF_NORMED);
            
            while (true) {
                Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
                if (mmr.maxVal < 0.7) break;
                
                Point matchLoc = mmr.maxLoc;
                Rect scaledRect = new Rect((int)matchLoc.x, (int)matchLoc.y, 
                                           scaledPiece.cols(), scaledPiece.rows());
                allMatches.add(new MatchedRegion(scaledRect, mmr.maxVal));
                
                Imgproc.rectangle(result, 
                    new Point(matchLoc.x - scaledPiece.cols()/2, matchLoc.y - scaledPiece.rows()/2),
                    new Point(matchLoc.x + scaledPiece.cols()/2, matchLoc.y + scaledPiece.rows()/2),
                    new Scalar(0), -1);
            }
            
            scaledPiece.release();
        }
        
        return filterNonOverlapping(allMatches, maxMatches);
    }

    /**
     * Dessine les matches sur l'image.
     * 
     * @param image l'image sur laquelle dessiner
     * @param matches liste des matches à dessiner
     * @param bestOnly indique si seulement le meilleur match doit être dessiné
     */
    public static void drawMatches(Mat image, List<MatchedRegion> matches, boolean bestOnly) {
        if (bestOnly && !matches.isEmpty()) {
            MatchedRegion best = matches.get(0);
            Imgproc.rectangle(image, best.getRegion().tl(), best.getRegion().br(), 
                             new Scalar(0, 0, 255), 3);
            
            String confidence = String.format("%.2f%%", best.getConfidence() * 100);
            Point textPos = new Point(best.getRegion().x, best.getRegion().y - 5);
            Imgproc.putText(image, confidence, textPos,
                Imgproc.FONT_HERSHEY_SIMPLEX, 0.6, new Scalar(0, 0, 255), 2);
        } else {
            for (int i = 0; i < matches.size(); i++) {
                MatchedRegion match = matches.get(i);
                java.awt.Color color = match.getColor();
                
                Imgproc.rectangle(image, match.getRegion().tl(), match.getRegion().br(),
                    new Scalar(color.getBlue(), color.getGreen(), color.getRed()), 2);
                
                String text = String.format("#%d: %.1f%%", i+1, match.getConfidence() * 100);
                Point textPos = new Point(match.getRegion().x, match.getRegion().y - 5);
                Imgproc.putText(image, text, textPos,
                    Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, 
                    new Scalar(color.getBlue(), color.getGreen(), color.getRed()), 1);
            }
        }
    }

    /**
     * Recherche combinée (pattern + shape) avec limitation et sans chevauchement.
     * 
     * @param mainImage l'image principale
     * @param puzzlePiece la pièce à rechercher
     * @param threshold le seuil de confiance minimum
     * @param maxMatches le nombre maximum de matches
     * @return liste des matches trouvés
     */
    public static List<MatchedRegion> searchAllAndCombine(Mat mainImage, Mat puzzlePiece, 
                                                          double threshold, int maxMatches) {
        List<MatchedRegion> allMatches = new ArrayList<>();
        
        allMatches.addAll(searchByPattern(mainImage, puzzlePiece, threshold, maxMatches * 2));
        allMatches.addAll(searchByShapeAdvanced(mainImage, puzzlePiece, threshold, maxMatches * 2));
        
        allMatches.sort((m1, m2) -> Double.compare(m2.getConfidence(), m1.getConfidence()));
        
        return filterNonOverlapping(allMatches, maxMatches);
    }
}