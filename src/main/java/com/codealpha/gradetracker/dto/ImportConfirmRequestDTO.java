package com.codealpha.gradetracker.dto;

import java.util.ArrayList;
import java.util.List;

public class ImportConfirmRequestDTO {

    private List<StudentDTO> studentsToImport = new ArrayList<>();

    public ImportConfirmRequestDTO() {
    }

    public List<StudentDTO> getStudentsToImport() {
        return studentsToImport;
    }

    public void setStudentsToImport(List<StudentDTO> studentsToImport) {
        this.studentsToImport = studentsToImport;
    }
}
