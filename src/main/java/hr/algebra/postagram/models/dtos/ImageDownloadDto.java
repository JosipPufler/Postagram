package hr.algebra.postagram.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageDownloadDto {
    private Boolean sepia;
    private Boolean blur;
    private String format;
    private Integer width;
    private Integer height;
}
