package com.swyp.saratang.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class FileDTO {
    private String originalFileName;  
    private String uploadFileName;  
    private String uploadFilePath;  
    private String uploadFileUrl; 
}
