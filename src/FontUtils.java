import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;

public class FontUtils {

    /**
     * Loads a custom font from the specified path with the given size.
     *
     * @param fontPath The path to the font file within the resources folder.
     * @param size     The desired font size.
     * @return The loaded Font object, or a default font if the custom font cannot be loaded.
     */
    public static Font loadFont(String fontPath, float size) {
        try (InputStream fontStream = FontUtils.class.getResourceAsStream(fontPath)) {
            if (fontStream != null) {
                return Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(size);
            } else {
                System.err.println("Font file not found: " + fontPath + ". Using default font.");
            }
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
        return new Font("Arial", Font.PLAIN, (int) size); // Fallback to default font
    }
}
