package com.dataury.soloJ.domain.report.dto.admin;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class AdvancedSearchDto {
    private Map<String, Object> filters;
    private String sortBy;
    private String sortOrder;
    
    @Getter
    @Setter
    public static class Filters {
        private DateRange dateRange;
        private List<String> status;
        private List<String> reason;
        private List<String> contentType;
    }
    
    @Getter
    @Setter
    public static class DateRange {
        private String start;
        private String end;
    }
}