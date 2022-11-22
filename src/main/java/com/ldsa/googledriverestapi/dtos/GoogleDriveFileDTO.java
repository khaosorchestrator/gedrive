package com.ldsa.googledriverestapi.dtos;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class GoogleDriveFileDTO implements Serializable {

    private String id;
    private String name;
    private String link;
    private String size;
    private String thumbnailLink;
    private boolean shared;
}
