package com.dataury.soloJ.global.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CursorPageResponse<T> {
    private List<T> content;
    private String nextCursor;
    private boolean hasNext;
    private int size;
}