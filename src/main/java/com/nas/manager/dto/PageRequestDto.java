package com.nas.manager.dto;

import lombok.Data;

@Data
public class PageRequestDto {
    private int page = 0;
    private int size = 20;
    private String sortBy = "lastAccessed";
    private String direction = "DESC";
}