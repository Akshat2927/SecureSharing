package com.backend.SecureSharing.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DownloadResponseDTO {
    private String fileName;
    private byte[] fileData;
}
