import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class UnsharpMaskingFilter {

    public static void main(String[] args) {
        String inputImagePath = "src/photos/1666152190_18-mykaleidoscope-ru-p-sereznii-kot-krasivo-21.jpg";
        //String inputImagePath = "src/photos/278428-frederika.jpg";
        String outputImagePath = "src/photos/photoForUnsharpMaskingOutTestNew.jpg";

        BufferedImage inputImage = loadInputImage(inputImagePath);

        // Шаг 1: Создание размытой копии изображения
        BufferedImage blurredImage = applyGaussianBlur(inputImage, 2);

        // Шаг 2: Вычитание размытой копии из оригинального изображения
        BufferedImage differenceImage = subtractImages(inputImage, blurredImage);

        // Шаг 3: Усиление контраста путем добавления различия к оригиналу
        BufferedImage outputImage = addImages(inputImage, differenceImage);

        try {
            ImageIO.write(outputImage, "jpg", new File(outputImagePath));
            System.out.println("Результат сохранен в " + outputImagePath);
        } catch (IOException e) {
            System.out.println("Ошибка при сохранении изображения: " + e.getMessage());
        }
    }

    private static BufferedImage loadInputImage(String inputImagePath) {
        BufferedImage inputImage = null;
        try {
            inputImage = ImageIO.read(new File(inputImagePath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return inputImage;
    }
    private static BufferedImage applyGaussianBlur(BufferedImage image, int radius) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage blurredImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Создание матрицы Гаусса
        double[][] gaussianMatrix = generateGaussianMatrix(radius);

        // Применение фильтра Гаусса к каждому пикселю изображения
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color newColor = applyGaussianKernel(image, x, y, gaussianMatrix);
                blurredImage.setRGB(x, y, newColor.getRGB());
            }
        }

        return blurredImage;
    }

    private static double[][] generateGaussianMatrix(int radius) {
        int size = 2 * radius + 1;
        double[][] matrix = new double[size][size];
        double sigma = radius / 3.0;

        double sum = 0.0;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                double exponent = -(x*x + y*y) / (2 * sigma * sigma);
                matrix[x + radius][y + radius] = Math.exp(exponent) / (2 * Math.PI * sigma * sigma);
                sum += matrix[x + radius][y + radius];
            }
        }

        // Нормализация матрицы
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] /= sum;
            }
        }

        return matrix;
    }
    private static Color applyGaussianKernel(BufferedImage image, int x, int y, double[][] gaussianMatrix) {
        int radius = gaussianMatrix.length / 2;
        int width = image.getWidth();
        int height = image.getHeight();
        double redSum = 0.0;
        double greenSum = 0.0;
        double blueSum = 0.0;

        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                int pixelX = Math.min(Math.max(x + i, 0), width - 1);
                int pixelY = Math.min(Math.max(y + j, 0), height - 1);
                Color color = new Color(image.getRGB(pixelX, pixelY));
                double weight = gaussianMatrix[i + radius][j + radius];
                redSum += color.getRed() * weight;
                greenSum += color.getGreen() * weight;
                blueSum += color.getBlue() * weight;
            }
        }

        int red = (int) Math.round(redSum);
        int green = (int) Math.round(greenSum);
        int blue = (int) Math.round(blueSum);

        // Ограничение значений каналов RGB в диапазоне [0, 255]
        red = Math.min(255, Math.max(0, red));
        green = Math.min(255, Math.max(0, green));
        blue = Math.min(255, Math.max(0, blue));

        return new Color(red, green, blue);
    }

    private static BufferedImage subtractImages(BufferedImage image1, BufferedImage image2) {
        int width = image1.getWidth();
        int height = image1.getHeight();
        BufferedImage differenceImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color1 = new Color(image1.getRGB(x, y));
                Color color2 = new Color(image2.getRGB(x, y));

                int diffR = color1.getRed() - color2.getRed();
                int diffG = color1.getGreen() - color2.getGreen();
                int diffB = color1.getBlue() - color2.getBlue();

                // Коррекция значений компонентов цвета, чтобы они находились в диапазоне 0-255
                diffR = Math.min(255, Math.max(0, diffR));
                diffG = Math.min(255, Math.max(0, diffG));
                diffB = Math.min(255, Math.max(0, diffB));

                Color newColor = new Color(diffR, diffG, diffB);

                differenceImage.setRGB(x, y, newColor.getRGB());
            }
        }

        return differenceImage;
    }

    private static BufferedImage addImages(BufferedImage originalImage, BufferedImage differenceImage) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color originalColor = new Color(originalImage.getRGB(x, y));
                Color differenceColor = new Color(differenceImage.getRGB(x, y));

                int newR = originalColor.getRed() + differenceColor.getRed();
                int newG = originalColor.getGreen() + differenceColor.getGreen();
                int newB = originalColor.getBlue() + differenceColor.getBlue();

                // Коррекция значений компонентов цвета, чтобы они находились в диапазоне 0-255
                newR = Math.min(255, Math.max(0, newR));
                newG = Math.min(255, Math.max(0, newG));
                newB = Math.min(255, Math.max(0, newB));

                Color newColor = new Color(newR, newG, newB);

                outputImage.setRGB(x, y, newColor.getRGB());
            }
        }

        return outputImage;
    }
}
