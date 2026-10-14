package com.codealpha.gradetracker.dto;

import java.util.ArrayList;
import java.util.List;

public class ImportPreviewResultDTO {

    private int totalRows;
    private int validCount;
    private int invalidCount;
    private int duplicateCount;
    private List<ImportRowResultDTO> rows = new ArrayList<>();

    public ImportPreviewResultDTO() {
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getValidCount() {
        return validCount;
    }

    public void setValidCount(int validCount) {
        this.validCount = validCount;
    }

    public int getInvalidCount() {
        return invalidCount;
    }

    public void setInvalidCount(int invalidCount) {
        this.invalidCount = invalidCount;
    }

    public int getDuplicateCount() {
        return duplicateCount;
    }

    public void setDuplicateCount(int duplicateCount) {
        this.duplicateCount = duplicateCount;
    }

    public List<ImportRowResultDTO> getRows() {
        return rows;
    }

    public void setRows(List<ImportRowResultDTO> rows) {
        this.rows = rows;
    }
}
