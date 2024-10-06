package puzzleai;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;

public class PuzzleSearch {
    public static Mat bufferedImageToMat(BufferedImage image) {
        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        int[] data = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), data, 0, image.getWidth());
        byte[] bytes = new byte[image.getWidth() * image.getHeight() * (int)mat.elemSize()];
        for (int i = 0; i < data.length; i++) {
            bytes[i * 3] = (byte)((data[i] >> 16) & 0xFF);
            bytes[i * 3 + 1] = (byte)((data[i] >> 8) & 0xFF);
            bytes[i * 3 + 2] = (byte)(data[i] & 0xFF);
        }
        mat.put(0, 0, bytes);
        return mat;
    }
    
    public static BufferedImage matToBufferedImage(Mat mat) {
    int type = BufferedImage.TYPE_BYTE_GRAY;
    if (mat.channels() > 1) {
        type = BufferedImage.TYPE_3BYTE_BGR;
    }
    int bufferSize = mat.channels() * mat.cols() * mat.rows();
    byte[] buffer = new byte[bufferSize];
    mat.get(0, 0, buffer); // get all the pixels
    BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
    final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
    System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);
    return image;
}


    public static void searchByShape(Mat mainImage, Mat puzzlePiece) {
        
        Mat mainGray = new Mat();
        Mat pieceGray = new Mat();
        Imgproc.cvtColor(mainImage, mainGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(puzzlePiece, pieceGray, Imgproc.COLOR_BGR2GRAY);

        
        Mat mainEdges = new Mat();
        Mat pieceEdges = new Mat();
        Imgproc.Canny(mainGray, mainEdges, 100, 200);
        Imgproc.Canny(pieceGray, pieceEdges, 100, 200);

        
        Mat result = new Mat();
        Imgproc.matchTemplate(mainEdges, pieceEdges, result, Imgproc.TM_CCOEFF);
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
        Point matchLoc = mmr.maxLoc;

        
        Imgproc.rectangle(mainImage, matchLoc, new Point(matchLoc.x + puzzlePiece.cols(), matchLoc.y + puzzlePiece.rows()), new Scalar(0, 0, 255), 2);
    }

    public static void searchByColor(Mat mainImage, Mat puzzlePiece) {
        
        Mat mainHSV = new Mat();
        Mat pieceHSV = new Mat();
        Imgproc.cvtColor(mainImage, mainHSV, Imgproc.COLOR_BGR2HSV);
        Imgproc.cvtColor(puzzlePiece, pieceHSV, Imgproc.COLOR_BGR2HSV);

        
        Mat pieceHist = new Mat();
        List<Mat> pieceHSVList = new ArrayList<>();
        pieceHSVList.add(pieceHSV);
        Imgproc.calcHist(pieceHSVList, new MatOfInt(0, 1), new Mat(), pieceHist, new MatOfInt(50, 60), new MatOfFloat(0, 256, 0, 256));
        Core.normalize(pieceHist, pieceHist, 0, 1, Core.NORM_MINMAX);

        
        Mat backProj = new Mat();
        List<Mat> mainHSVList = new ArrayList<>();
        mainHSVList.add(mainHSV);
        Imgproc.calcBackProject(mainHSVList, new MatOfInt(0, 1), pieceHist, backProj, new MatOfFloat(0, 256, 0, 256), 1);

        
        Imgproc.threshold(backProj, backProj, 127, 255, Imgproc.THRESH_BINARY);
        Imgproc.medianBlur(backProj, backProj, 15);

        
        Mat contours = new Mat();
        backProj.convertTo(contours, CvType.CV_8UC3);
        List<MatOfPoint> contourList = new ArrayList<>();
        Imgproc.findContours(contours, contourList, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        
        double maxArea = -1;
        Rect maxRect = null;
        for (MatOfPoint contour : contourList) {
            Rect rect = Imgproc.boundingRect(contour);
            double area = Imgproc.contourArea(contour);
            if (area > maxArea) {
                maxArea = area;
                maxRect = rect;
            }
        }

        
        if (maxRect != null) {
            Imgproc.rectangle(mainImage, maxRect.tl(), maxRect.br(), new Scalar(0, 0, 255), 2);
        }
    }

    public static void searchByPattern(Mat mainImage, Mat puzzlePiece) {
        
        Mat result = new Mat();
        Imgproc.matchTemplate(mainImage, puzzlePiece, result, Imgproc.TM_CCOEFF_NORMED);
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
        Point matchLoc = mmr.maxLoc;

        
        Imgproc.rectangle(mainImage, matchLoc, new Point(matchLoc.x + puzzlePiece.cols(), matchLoc.y + puzzlePiece.rows()), new Scalar(0, 0, 255), 2);
    }
}
