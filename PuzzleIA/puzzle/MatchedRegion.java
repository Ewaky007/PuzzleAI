package puzzle;

import org.opencv.core.Rect;
import java.awt.Color;

public class MatchedRegion {
    
    private Rect region;
    private double confidence;
    private String method;
    
    /**
     * Constructeur d'une région correspondante.
     * 
     * @param region le rectangle de la région
     * @param confidence le score de confiance (entre 0 et 1)
     */
    public MatchedRegion(Rect region, double confidence) {
        this.region = region;
        this.confidence = confidence;
    }
    
    /**
     * Retourne le rectangle de la région.
     * 
     * @return le rectangle
     */
    public Rect getRegion() { 
        return region; 
    }
    
    /**
     * Retourne le score de confiance.
     * 
     * @return la confiance (entre 0 et 1)
     */
    public double getConfidence() { 
        return confidence; 
    }
    
    /**
     * Définit la méthode de recherche utilisée.
     * 
     * @param method le nom de la méthode
     */
    public void setMethod(String method) { 
        this.method = method; 
    }
    
    /**
     * Retourne la méthode de recherche utilisée.
     * 
     * @return le nom de la méthode
     */
    public String getMethod() {
        return method;
    }
    
    /**
     * Retourne une couleur basée sur le score de confiance.
     * Rouge (>80%), Orange (60-80%), Jaune (40-60%), Gris (<40%).
     * 
     * @return la couleur associée à la confiance
     */
    public Color getColor() {
        if (confidence > 0.8) return Color.RED;
        if (confidence > 0.6) return Color.ORANGE;
        if (confidence > 0.4) return Color.YELLOW;
        return Color.GRAY;
    }
    
    @Override
    public String toString() {
        return String.format("Match [x=%d, y=%d, w=%d, h=%d, confidence=%.2f, method=%s]",
            region.x, region.y, region.width, region.height, confidence, method);
    }
}