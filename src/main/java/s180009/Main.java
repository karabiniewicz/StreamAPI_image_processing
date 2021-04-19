package s180009;

import s180009.initialize.InitializeData;
import s180009.entity.FilesFolder;
import s180009.repository.ParallelStreamRepository;
import s180009.repository.SingleStreamRepository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws IOException {
        String sInput = args[0];
        String sOutput = args[1];

        FilesFolder filesFolder = FilesFolder.builder()
                .source(Path.of(sInput))
                .build();

        InitializeData initializeData = InitializeData.builder()
                .filesFolder(filesFolder)
                .build();
        initializeData.init();

        SingleStreamRepository singleStreamRepository = SingleStreamRepository.builder()
                .filesFolder(filesFolder)
                .build();

        long start1 = System.currentTimeMillis();

        Stream streamLoaded = singleStreamRepository.loadFilesAndSetToStream();
        Stream streamTransformed = singleStreamRepository.transformFilesAndSetToStream(streamLoaded);
        singleStreamRepository.writeToDirectory(streamTransformed, sOutput);

        streamLoaded.close();
        streamTransformed.close();

        System.out.println("NormalStream time: " + (System.currentTimeMillis() - start1) + " ms");

        for (int i = 1; i < 17; i++) {
            ParallelStreamRepository parallelStreamRepository = ParallelStreamRepository.builder()
                    .pool(new ForkJoinPool(i))
                    .filesFolder(filesFolder)
                    .build();

            long start = System.currentTimeMillis();

            Stream loadedFilesToStream = parallelStreamRepository.loadFilesAndSetToStream();
            Stream transformedFileToStream = parallelStreamRepository.transformFilesAndSetToStream(loadedFilesToStream);
            parallelStreamRepository.writeToDirectory(transformedFileToStream, sOutput);

            loadedFilesToStream.close();
            transformedFileToStream.close();
            parallelStreamRepository.shutdownPool();

            System.out.println("ParallelStream for " + i + " threads time: " + (System.currentTimeMillis() - start) + " ms");
        }
    }
}
