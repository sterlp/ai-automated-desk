package org.sterl.ai.desk.shared;

import java.util.ArrayList;
import java.util.List;

public class VectorHelper {

    /**
     * Gerader Abstand im Raum für
     * Abstände, Clustering, geometrische Nähe
     * @return [0,∞)
     */
    public static double euclideanDistance(float[] a, float[] b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            double d = a[i] - b[i];
            sum += d * d;
        }
        return Math.sqrt(sum);
    }
    
    /**
     * Winkel zwischen den Vektoren für
     * Semantische Ähnlichkeit, Text‑Embeddings
     * @return [−1,1]
     */
    public static double cosineSimilarity(float[] a, float[] b) {
        double dot = 0.0, na = 0.0, nb = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        if (na == 0 || nb == 0) return 0.0;
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }
    
    public static List<Float> toList(float[] value) {
        var result = new ArrayList<Float>(value.length);
        for (var f : value) result.add(f);
        return result;
    }
}
