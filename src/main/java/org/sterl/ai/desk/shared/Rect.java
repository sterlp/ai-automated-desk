package org.sterl.ai.desk.shared;

import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;

public record Rect(float width, float height) {
    public static Rect of(PDImage in) {
        return new Rect(in.getWidth(), in.getHeight());
    }
    public static Rect of(PDRectangle in) {
        return new Rect(in.getWidth(), in.getHeight());
    }
    
    public boolean isLandscape() {
        return width > height;
    }
    /**
     * Scales this rect to fit the given boundary
     * 
     * @return the scaled rect
     */
    public Rect scaleTo(Rect boundary) {
        float originalWidth = this.width();
        float originalHeight = this.height();
        float boundWidth = boundary.width();
        float boundHeight = boundary.height();
        float newWidth = originalWidth;
        float newHeight = originalHeight;

        if (newWidth > boundWidth) {
            newWidth = boundWidth;
            newHeight = newWidth * originalHeight / originalWidth;
        }
        if (newHeight > boundHeight) {
            newHeight = boundHeight;
            newWidth = originalWidth * newHeight / originalHeight;
        }
        return new Rect(newWidth, newHeight);
    }
}