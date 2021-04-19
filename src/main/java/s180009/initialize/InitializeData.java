package s180009.initialize;

import lombok.*;
import s180009.entity.FilesFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class InitializeData {
    FilesFolder filesFolder;

    public void init() {
        try (Stream<Path> stream = Files.list(filesFolder.getSource())) {
            filesFolder.setFiles(stream.collect(Collectors.toList()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
