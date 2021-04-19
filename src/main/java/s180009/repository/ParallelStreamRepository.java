package s180009.repository;

import lombok.*;
import org.apache.commons.lang3.tuple.Pair;
import s180009.entity.FilesFolder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ParallelStreamRepository {
    FilesFolder filesFolder;
    ForkJoinPool pool;

    private Stream<Path> getStream() {
        return filesFolder.getFiles().stream().parallel();
    }

    public Stream loadFilesAndSetToStream() {
        Stream<Pair<String, BufferedImage>> pairStream = null;
        try {
            pairStream = pool.submit(() -> {
                Stream<Pair<String, BufferedImage>> stream = getStream().map(value -> {
                    try {
                        return Pair.of(value.getFileName().toString(), ImageIO.read(value.toFile()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return Pair.of(value.getFileName().toString(), null);
                });
                return stream;
            }).get();

        } catch (InterruptedException | ExecutionException e) {
        }
        return pairStream;
    }

    public Stream transformFilesAndSetToStream(Stream<Pair<String, BufferedImage>> streamLoaded) {
        Stream<Pair<String, BufferedImage>> pairStream = null;
        try {
            pairStream = pool.submit(() -> {
                Stream<Pair<String, BufferedImage>> stream = streamLoaded.map(value -> {
                    if (value.getRight() == null) {
                        System.out.println("Not found: " + value.getLeft());
                        return value;
                    }
                    BufferedImage image = value.getRight();
                    for (int i = 0; i < image.getWidth(); i++) {
                        for (int j = 0; j < image.getHeight(); j++) {
                            int rgb = image.getRGB(i, j);
                            Color color = new Color(rgb);
                            int red = (int) (0.299 * color.getRed());
                            int green = (int) (0.587 * color.getGreen());
                            int blue = (int) (0.114 * color.getBlue());
                            Color outColor = new Color(red + green + blue, red + green + blue, red + green + blue);
                            int outRgb = outColor.getRGB();
                            image.setRGB(i, j, outRgb);
                        }
                    }
                    return Pair.of(value.getLeft(), image);
                });
                return stream;
            }).get();
        } catch (InterruptedException | ExecutionException ex) {
        }
        return pairStream;
    }

    public void writeToDirectory(Stream<Pair<String, BufferedImage>> stream2, String path) {
        try {
            pool.submit(() -> {
                stream2.forEach(value -> {
                    File outputFile = new File(path + "/" + value.getLeft() + "_Transformed_P.jpg");
                    try {
                        ImageIO.write(value.getRight(), "jpg", outputFile);
                    } catch (IOException ex) {
                    }
                });
            }).get();
        } catch (InterruptedException | ExecutionException ex) {
        }
    }

    public void shutdownPool() {
        if (!pool.isShutdown()) {
            pool.shutdown();
        }
    }

}
