package s180009.entity;

import lombok.*;

import java.nio.file.Path;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class FilesFolder {
    private List<Path> files;
    private Path source;
}
