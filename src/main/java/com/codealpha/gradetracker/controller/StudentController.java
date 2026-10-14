package com.codealpha.gradetracker.controller;

import com.codealpha.gradetracker.dto.*;
import com.codealpha.gradetracker.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentController {

    private final StudentService studentService;

    @Autowired
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * GET /api/students
     * Retrieves all student records with optional search, grade, status filters and sorting.
     */
    @GetMapping
    public ResponseEntity<List<StudentDTO>> getAllStudents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder) {
        List<StudentDTO> list = studentService.getAllStudents(search, grade, status, sortBy, sortOrder);
        return ResponseEntity.ok(list);
    }

    /**
     * GET /api/students/{studentId}
     * Retrieves a single student by business Student ID.
     */
    @GetMapping("/{studentId}")
    public ResponseEntity<StudentDTO> getStudentById(@PathVariable String studentId) {
        StudentDTO dto = studentService.getStudentById(studentId);
        return ResponseEntity.ok(dto);
    }

    /**
     * POST /api/students
     * Creates a new student record.
     */
    @PostMapping
    public ResponseEntity<?> createStudent(@Valid @RequestBody StudentDTO dto) {
        try {
            StudentDTO created = studentService.createStudent(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/students/{studentId}
     * Updates an existing student record.
     */
    @PutMapping("/{studentId}")
    public ResponseEntity<?> updateStudent(@PathVariable String studentId, @Valid @RequestBody StudentDTO dto) {
        try {
            StudentDTO updated = studentService.updateStudent(studentId, dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/students/{studentId}
     * Deletes a student record by ID.
     */
    @DeleteMapping("/{studentId}")
    public ResponseEntity<?> deleteStudent(@PathVariable String studentId) {
        try {
            studentService.deleteStudent(studentId);
            return ResponseEntity.ok(Map.of("message", "Student ID '" + studentId + "' deleted successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/students/all
     * Clears all students from the database.
     */
    @DeleteMapping("/all")
    public ResponseEntity<?> deleteAllStudents() {
        studentService.deleteAllStudents();
        return ResponseEntity.ok(Map.of("message", "All student records cleared successfully."));
    }

    /**
     * GET /api/students/stats
     * Calculates class performance statistics.
     */
    @GetMapping("/stats")
    public ResponseEntity<ClassStatisticsDTO> getStatistics() {
        return ResponseEntity.ok(studentService.getStatistics());
    }

    /**
     * POST /api/students/import/preview
     * Parses uploaded files (single file, multi-file batch, or ZIP archive) and returns validation preview WITHOUT committing to DB.
     */
    @PostMapping("/import/preview")
    public ResponseEntity<?> previewImport(@RequestParam(value = "file", required = false) MultipartFile file,
                                           @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        try {
            if (files != null && !files.isEmpty()) {
                ImportPreviewResultDTO preview = studentService.previewBatchImport(files);
                return ResponseEntity.ok(preview);
            }
            if (file != null && !file.isEmpty()) {
                ImportPreviewResultDTO preview = studentService.previewImport(file);
                return ResponseEntity.ok(preview);
            }
            return ResponseEntity.badRequest().body(Map.of("error", "No file uploaded. Please select a file or ZIP archive."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/students/import/confirm
     * Batch imports validated student records into actual database.
     */
    @PostMapping("/import/confirm")
    public ResponseEntity<?> confirmImport(@RequestBody ImportConfirmRequestDTO request) {
        int importedCount = studentService.confirmImport(request.getStudentsToImport());
        return ResponseEntity.ok(Map.of("importedCount", importedCount, "message", importedCount + " student records imported successfully!"));
    }

    /**
     * POST /api/students/import/rejected-csv
     * Downloads CSV file containing rejected import rows and their error reasons.
     */
    @PostMapping("/import/rejected-csv")
    public ResponseEntity<byte[]> downloadRejectedRowsCsv(@RequestBody List<ImportRowResultDTO> rejectedRows) {
        byte[] csvData = studentService.generateRejectedRowsCsv(rejectedRows);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"rejected_students.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }

    /**
     * GET /api/students/sample-template
     * Downloads sample CSV import template file.
     */
    @GetMapping("/sample-template")
    public ResponseEntity<byte[]> downloadSampleTemplate() {
        byte[] template = studentService.generateSampleCsvTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"sample_student_import_template.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(template);
    }

    /**
     * GET /api/students/export/csv
     * Exports filtered / all students to CSV format.
     */
    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportToCsv(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String status) {
        List<StudentDTO> students = studentService.getAllStudents(search, grade, status, "studentId", "ASC");
        byte[] csvData = studentService.exportToCsv(students);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"student_grade_report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }

    /**
     * GET /api/students/export/excel
     * Exports filtered / all students to Excel (.xlsx) format.
     */
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportToExcel(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String status) {
        List<StudentDTO> students = studentService.getAllStudents(search, grade, status, "studentId", "ASC");
        byte[] excelData = studentService.exportToExcel(students);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"student_grade_report.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelData);
    }

    /**
     * GET /api/students/export/pdf
     * Exports filtered / all students to PDF report format.
     */
    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportToPdf(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String status) {
        List<StudentDTO> students = studentService.getAllStudents(search, grade, status, "studentId", "ASC");
        byte[] pdfData = studentService.exportToPdf(students);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"student_grade_report.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfData);
    }
}
