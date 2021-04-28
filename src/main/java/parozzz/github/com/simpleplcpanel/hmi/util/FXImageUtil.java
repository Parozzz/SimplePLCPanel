package parozzz.github.com.simpleplcpanel.hmi.util;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

public final class FXImageUtil
{
    public static Image removeWhiteBackground(Image inputImage, int tolerance)
    {
        int width = (int) inputImage.getWidth();
        int height = (int) inputImage.getHeight();

        var inputReader = inputImage.getPixelReader();

        var outputImage = new WritableImage(width, height);
        var outputWrite = outputImage.getPixelWriter();
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                int argb = inputReader.getArgb(x, y);

                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                if (r >= tolerance
                        && g >= tolerance
                        && b >= tolerance)
                {
                    argb &= 0x00FFFFFF;
                }

                outputWrite.setArgb(x, y, argb);
            }
        }

        return outputImage;
    }

    private FXImageUtil()
    {
    }
}
