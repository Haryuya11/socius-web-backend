package org.socius.sociuswebbackend.model.dtos.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileMetadataDto {
    private String originalFilename;
    private String contentType;
    private long size;
    private String uploadDate;
}
