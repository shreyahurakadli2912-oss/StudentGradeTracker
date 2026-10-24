package com.codealpha.gradetracker.service;

import com.codealpha.gradetracker.dto.*;
import com.codealpha.gradetracker.model.StudentEntity;
import com.codealpha.gradetracker.repository.StudentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// PDFBox explicit imports for PDF text extraction
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

// OpenPDF explicit imports for PDF report generation
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

// Apache Commons CSV explicit imports
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

// Apache POI explicit imports
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@Transactional
public class StudentService {

    private final StudentRepository repository;
    private final GradeCalculator gradeCalculator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Blacklist keywords for address/header words that are NOT student IDs
    private static final Set<String> BLACKLISTED_IDS = Set.of(
            "CHOLANAGAR", "BANGALORE", "KARNATAKA", "COLLEGE", "UNIVERSITY",
            "DEPARTMENT", "SEMESTER", "SCHEME", "FACULTY", "PROFESSOR",
            "TOTAL", "SLNO", "SL.NO", "ADDRESS", "PINCODE", "TELEPHONE",
            "EMAIL", "HEADER", "FOOTER", "PAGE", "ACADEMIC", "BRANCH",
            "MARKS", "GRADE", "RESULT", "STATUS", "REMARKS", "INTERNAL",
            "EXTERNAL", "ATTENDANCE", "CREDIT", "CREDITS", "COURSE", "SUBJECT",
            "PROVISIONAL", "STATEMENT", "NOTIFICATION", "OFFICE", "INSTITUTE",
            "EDUCATION", "BOARD", "EXAMINATION", "EXAMINATIONS", "SRN", "USN"
    );

    @Autowired
    public StudentService(StudentRepository repository, GradeCalculator gradeCalculator) {
        this.repository = repository;
        this.gradeCalculator = gradeCalculator;
    }

    /**
     * Gets all students with optional search query, grade filter, pass/fail status filter, and sorting.
     */
    @Transactional(readOnly = true)
    public List<StudentDTO> getAllStudents(String query, String grade, String status, String sortBy, String sortOrder) {
        Specification<StudentEntity> spec = Specification.where(null);

        if (query != null && !query.trim().isEmpty()) {
            String q = "%" + query.trim().toLowerCase() + "%";
            spec = spec.and((root, criteriaQuery, cb) -> cb.or(
                    cb.like(cb.lower(root.get("studentId")), q),
                    cb.like(cb.lower(root.get("name")), q),
                    cb.like(cb.lower(root.get("className")), q)
            ));
        }

        if (grade != null && !grade.trim().isEmpty() && !"ALL".equalsIgnoreCase(grade)) {
            spec = spec.and((root, criteriaQuery, cb) -> cb.equal(cb.upper(root.get("grade")), grade.trim().toUpperCase()));
        }

        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            String s = status.trim().toUpperCase();
            if (s.startsWith("PASS")) {
                spec = spec.and((root, criteriaQuery, cb) -> cb.or(
                        cb.equal(cb.upper(root.get("status")), "PASSED"),
                        cb.equal(cb.upper(root.get("status")), "PASS")
                ));
            } else if (s.startsWith("FAIL")) {
                spec = spec.and((root, criteriaQuery, cb) -> cb.or(
                        cb.equal(cb.upper(root.get("status")), "FAILED"),
                        cb.equal(cb.upper(root.get("status")), "FAIL")
                ));
            } else {
                spec = spec.and((root, criteriaQuery, cb) -> cb.equal(cb.upper(root.get("status")), s));
            }
        }

        Sort sort = Sort.by(Sort.Direction.ASC, "studentId");
        if (sortBy != null && !sortBy.trim().isEmpty()) {
            boolean isExplicitDesc = "DESC".equalsIgnoreCase(sortOrder);
            boolean isExplicitAsc = "ASC".equalsIgnoreCase(sortOrder);
            switch (sortBy.toLowerCase()) {
                case "name":
                    sort = isExplicitDesc ? Sort.by(Sort.Direction.DESC, "name") : Sort.by(Sort.Direction.ASC, "name");
                    break;
                case "marks":
                    // Default to highest marks first (DESC) unless explicitly requested ASC
                    sort = isExplicitAsc ? Sort.by(Sort.Direction.ASC, "marks") : Sort.by(Sort.Direction.DESC, "marks");
                    break;
                case "grade":
                    if (isExplicitDesc) {
                        sort = Sort.by(Sort.Direction.DESC, "grade").and(Sort.by(Sort.Direction.ASC, "marks"));
                    } else {
                        sort = Sort.by(Sort.Direction.ASC, "grade").and(Sort.by(Sort.Direction.DESC, "marks"));
                    }
                    break;
                case "classname":
                case "class":
                    sort = isExplicitDesc ? Sort.by(Sort.Direction.DESC, "className") : Sort.by(Sort.Direction.ASC, "className");
                    break;
                default:
                    sort = isExplicitDesc ? Sort.by(Sort.Direction.DESC, "studentId") : Sort.by(Sort.Direction.ASC, "studentId");
            }
        }

        return repository.findAll(spec, sort).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StudentDTO getStudentById(String studentId) {
        return repository.findByStudentIdIgnoreCase(studentId)
                .map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Student with ID '" + studentId + "' not found."));
    }

    public StudentDTO createStudent(StudentDTO dto) {
        if (dto.getStudentId() == null || dto.getStudentId().trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be empty.");
        }
        String cleanId = dto.getStudentId().trim();
        if (repository.existsByStudentIdIgnoreCase(cleanId)) {
            throw new IllegalArgumentException("Student ID '" + cleanId + "' already exists.");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Student name cannot be empty.");
        }
        if (!gradeCalculator.isValidMarks(dto.getMarks())) {
            throw new IllegalArgumentException("Marks must be between 0 and 100 inclusive.");
        }

        StudentEntity entity = new StudentEntity();
        entity.setStudentId(cleanId);
        entity.setName(dto.getName().trim());
        entity.setClassName(dto.getClassName() != null && !dto.getClassName().trim().isEmpty() ? dto.getClassName().trim() : "General");
        entity.setMarks(dto.getMarks());
        entity.setGrade(dto.getGrade() != null && !dto.getGrade().trim().isEmpty() ? dto.getGrade().trim().toUpperCase() : gradeCalculator.calculateGrade(dto.getMarks()));
        
        String resolvedStatus;
        if (dto.getStatus() != null && !dto.getStatus().trim().isEmpty()) {
            String s = dto.getStatus().trim().toUpperCase();
            resolvedStatus = s.startsWith("FAIL") ? "FAILED" : (s.startsWith("PASS") ? "PASSED" : s);
        } else {
            resolvedStatus = gradeCalculator.calculateStatus(dto.getMarks());
        }
        entity.setStatus(resolvedStatus);
        entity.setSubjectBreakdown(dto.getSubjectBreakdown());

        StudentEntity saved = repository.save(entity);
        return toDTO(saved);
    }

    public StudentDTO updateStudent(String studentId, StudentDTO dto) {
        StudentEntity entity = repository.findByStudentIdIgnoreCase(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student with ID '" + studentId + "' not found."));

        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Student name cannot be empty.");
        }
        if (!gradeCalculator.isValidMarks(dto.getMarks())) {
            throw new IllegalArgumentException("Marks must be between 0 and 100 inclusive.");
        }

        entity.setName(dto.getName().trim());
        if (dto.getClassName() != null && !dto.getClassName().trim().isEmpty()) {
            entity.setClassName(dto.getClassName().trim());
        }
        entity.setMarks(dto.getMarks());
        entity.setGrade(dto.getGrade() != null && !dto.getGrade().trim().isEmpty() ? dto.getGrade().trim().toUpperCase() : gradeCalculator.calculateGrade(dto.getMarks()));
        
        if (dto.getStatus() != null && !dto.getStatus().trim().isEmpty()) {
            String s = dto.getStatus().trim().toUpperCase();
            entity.setStatus(s.startsWith("FAIL") ? "FAILED" : (s.startsWith("PASS") ? "PASSED" : s));
        } else {
            entity.setStatus(gradeCalculator.calculateStatus(dto.getMarks()));
        }

        if (dto.getSubjectBreakdown() != null) {
            entity.setSubjectBreakdown(dto.getSubjectBreakdown());
        }

        StudentEntity updated = repository.save(entity);
        return toDTO(updated);
    }

    public void deleteStudent(String studentId) {
        StudentEntity entity = repository.findByStudentIdIgnoreCase(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student with ID '" + studentId + "' not found."));
        repository.delete(entity);
    }

    public void deleteAllStudents() {
        repository.deleteAll();
    }

    @Transactional(readOnly = true)
    public ClassStatisticsDTO getStatistics() {
        List<StudentEntity> all = repository.findAll();
        ClassStatisticsDTO stats = new ClassStatisticsDTO();

        long total = all.size();
        stats.setTotalStudents(total);

        if (total == 0) {
            stats.setTotalMarks(0.0);
            stats.setAverageMarks(0.0);
            stats.setPassPercentage(0.0);
            stats.setPassedCount(0);
            stats.setFailedCount(0);
            stats.setHighestMarks(0.0);
            stats.setHighestScorers("None");
            stats.setLowestMarks(0.0);
            stats.setLowestScorers("None");
            Map<String, Long> dist = new LinkedHashMap<>();
            dist.put("A", 0L); dist.put("B", 0L); dist.put("C", 0L); dist.put("D", 0L); dist.put("F", 0L);
            stats.setGradeDistribution(dist);
            return stats;
        }

        double sum = 0.0;
        double maxMarks = Double.NEGATIVE_INFINITY;
        double minMarks = Double.POSITIVE_INFINITY;
        long passedCount = 0;
        long failedCount = 0;
        Map<String, Long> dist = new LinkedHashMap<>();
        dist.put("A", 0L); dist.put("B", 0L); dist.put("C", 0L); dist.put("D", 0L); dist.put("F", 0L);

        for (StudentEntity s : all) {
            double m = s.getMarks();
            sum += m;
            if (m > maxMarks) maxMarks = m;
            if (m < minMarks) minMarks = m;

            if ("PASSED".equalsIgnoreCase(s.getStatus())) passedCount++;
            else failedCount++;

            String g = s.getGrade();
            dist.put(g, dist.getOrDefault(g, 0L) + 1L);
        }

        stats.setTotalMarks(sum);
        stats.setAverageMarks(sum / total);
        stats.setPassedCount(passedCount);
        stats.setFailedCount(failedCount);
        stats.setPassPercentage(((double) passedCount / total) * 100.0);
        stats.setHighestMarks(maxMarks);
        stats.setLowestMarks(minMarks);
        stats.setGradeDistribution(dist);

        final double finalMax = maxMarks;
        final double finalMin = minMarks;
        List<String> highestList = all.stream()
                .filter(s -> Double.compare(s.getMarks(), finalMax) == 0)
                .map(s -> s.getName() + " (" + s.getStudentId() + ")")
                .collect(Collectors.toList());
        List<String> lowestList = all.stream()
                .filter(s -> Double.compare(s.getMarks(), finalMin) == 0)
                .map(s -> s.getName() + " (" + s.getStudentId() + ")")
                .collect(Collectors.toList());

        stats.setHighestScorers(String.join(", ", highestList));
        stats.setLowestScorers(String.join(", ", lowestList));

        return stats;
    }

    /**
     * UNIVERSAL FILE PARSER: Supports PDF, Excel (.xlsx/.xls), CSV, JSON, TXT, DOC, and ANY document format!
     */
    @Transactional(readOnly = true)
    public ImportPreviewResultDTO previewImport(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty.");
        }

        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "file";
        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read uploaded file: " + e.getMessage());
        }

        List<ImportRowResultDTO> rawRows = new ArrayList<>();

        try {
            if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
                rawRows = parseExcelFile(new ByteArrayInputStream(fileBytes));
            } else if (filename.endsWith(".pdf")) {
                rawRows = parsePdfFile(fileBytes);
            } else if (filename.endsWith(".json")) {
                rawRows = parseJsonFile(new ByteArrayInputStream(fileBytes));
            } else if (filename.endsWith(".zip")) {
                rawRows = parseZipArchive(fileBytes);
            } else {
                try {
                    rawRows = parseCsvFile(new ByteArrayInputStream(fileBytes));
                } catch (Exception csvException) {
                    rawRows = parseGenericTextFile(new ByteArrayInputStream(fileBytes));
                }
                if (rawRows.isEmpty()) {
                    rawRows = parseGenericTextFile(new ByteArrayInputStream(fileBytes));
                }
            }
        } catch (Exception e) {
            try {
                rawRows = parseGenericTextFile(new ByteArrayInputStream(fileBytes));
            } catch (Exception ex) {
                throw new IllegalArgumentException("Could not parse file '" + file.getOriginalFilename() + "': " + e.getMessage(), e);
            }
        }

        if (rawRows.isEmpty()) {
            throw new IllegalArgumentException("No valid student/subject records detected in '" + file.getOriginalFilename() + "'. Please ensure the file contains IDs, Names/Titles, and Marks.");
        }

        ImportPreviewResultDTO result = new ImportPreviewResultDTO();
        result.setTotalRows(rawRows.size());

        Set<String> fileSeenIds = new HashSet<>();
        int valid = 0;
        int invalid = 0;
        int duplicates = 0;

        for (ImportRowResultDTO row : rawRows) {
            if (!row.isValid()) {
                invalid++;
                result.getRows().add(row);
                continue;
            }

            String id = row.getStudentId().trim();
            if (fileSeenIds.contains(id.toLowerCase())) {
                row.setValid(false);
                row.setErrorReason("Duplicate ID '" + id + "' found within this uploaded file.");
                duplicates++;
                invalid++;
            } else if (repository.existsByStudentIdIgnoreCase(id)) {
                row.setValid(false);
                row.setErrorReason("Student ID '" + id + "' already exists in the database.");
                duplicates++;
                invalid++;
            } else {
                fileSeenIds.add(id.toLowerCase());
                if (row.getGrade() == null || row.getGrade().trim().isEmpty()) {
                    row.setGrade(gradeCalculator.calculateGrade(row.getMarks()));
                }
                if (row.getStatus() == null || row.getStatus().trim().isEmpty()) {
                    row.setStatus(gradeCalculator.calculateStatus(row.getMarks()));
                }
                valid++;
            }
            result.getRows().add(row);
        }

        result.setValidCount(valid);
        result.setInvalidCount(invalid);
        result.setDuplicateCount(duplicates);

        return result;
    }

    /**
     * BATCH UNIVERSAL FILE PARSER: Supports uploading multiple files at once.
     */
    @Transactional(readOnly = true)
    public ImportPreviewResultDTO previewBatchImport(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("No files were provided for upload.");
        }
        if (files.size() == 1) {
            return previewImport(files.get(0));
        }

        List<ImportRowResultDTO> rawRows = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();
        int rowNumber = 1;

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            try {
                ImportPreviewResultDTO singleResult = previewImport(file);
                for (ImportRowResultDTO r : singleResult.getRows()) {
                    String id = r.getStudentId() != null ? r.getStudentId().trim().toUpperCase() : "";
                    if (!id.isEmpty() && !seenIds.contains(id)) {
                        seenIds.add(id);
                        r.setRowNumber(rowNumber++);
                        rawRows.add(r);
                    }
                }
            } catch (Exception ignored) {
                // Continue with next file
            }
        }

        if (rawRows.isEmpty()) {
            throw new IllegalArgumentException("No valid student records could be extracted from the uploaded files.");
        }

        ImportPreviewResultDTO result = new ImportPreviewResultDTO();
        result.setTotalRows(rawRows.size());
        int valid = 0;
        int invalid = 0;
        int duplicates = 0;

        for (ImportRowResultDTO row : rawRows) {
            if (!row.isValid()) {
                invalid++;
            } else {
                valid++;
            }
            result.getRows().add(row);
        }

        result.setValidCount(valid);
        result.setInvalidCount(invalid);
        result.setDuplicateCount(duplicates);

        return result;
    }

    /**
     * Imports validated student records into database.
     */
    public int confirmImport(List<StudentDTO> studentsToImport) {
        if (studentsToImport == null || studentsToImport.isEmpty()) {
            return 0;
        }

        int importedCount = 0;
        for (StudentDTO dto : studentsToImport) {
            if (dto.getStudentId() != null && !repository.existsByStudentIdIgnoreCase(dto.getStudentId().trim())) {
                createStudent(dto);
                importedCount++;
            }
        }
        return importedCount;
    }

    // --- File Parsing Strategy Handlers ---

    private List<ImportRowResultDTO> parseCsvFile(InputStream inputStream) throws IOException {
        List<ImportRowResultDTO> rows = new ArrayList<>();
        byte[] bytes = inputStream.readAllBytes();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8));
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                .builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .build());

        int rowNum = 1;
        for (CSVRecord record : csvParser) {
            rowNum++;
            String id = getRecordValue(record, "studentId", "id", "usn", "rollno", "roll_no", "roll no", "regno", "reg_no", "student_id", "student id", "code");
            String name = getRecordValue(record, "name", "studentName", "student_name", "student name", "fullName", "full_name", "full name", "candidate_name", "candidate name", "candidate");
            String className = getRecordValue(record, "className", "class", "standard", "std", "grade", "section", "sec", "semester", "sem", "branch", "course", "program", "stream", "dept", "department");
            String marksStr = getRecordValue(record, "marks", "score", "percentage", "percent", "aggregate", "result", "marksobtained", "marks_obtained", "marks obtained", "totalmarks", "total_marks", "total marks", "total");

            // Positional fallback if headers did not match standard keywords
            if ((id == null || name == null || marksStr == null) && record.size() >= 3) {
                if (id == null) id = record.get(0);
                if (name == null) name = record.get(1);
                if (record.size() >= 4) {
                    if (className == null) className = record.get(2);
                    if (marksStr == null) marksStr = record.get(3);
                } else {
                    if (marksStr == null) marksStr = record.get(2);
                }
            }

            ImportRowResultDTO row = validateRow(rowNum, id, formatStudentName(name), className, marksStr);
            if (row != null && isNotBlacklistedId(row.getStudentId())) {
                rows.add(row);
            }
        }
        return rows;
    }

    private String getRecordValue(CSVRecord record, String... possibleHeaders) {
        if (record == null) return null;
        for (String header : possibleHeaders) {
            if (record.isMapped(header)) {
                String val = record.get(header);
                if (val != null && !val.trim().isEmpty()) {
                    return val.trim();
                }
            }
        }
        try {
            java.util.Map<String, Integer> headerMap = record.getParser().getHeaderMap();
            if (headerMap != null) {
                // Pass 1: exact normalized match
                for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
                    String cleanActual = entry.getKey().replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
                    if (cleanActual.isEmpty()) continue;
                    for (String expected : possibleHeaders) {
                        String cleanExpected = expected.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
                        if (cleanExpected.isEmpty()) continue;
                        if (cleanActual.equals(cleanExpected)) {
                            String val = record.get(entry.getValue());
                            if (val != null && !val.trim().isEmpty()) {
                                return val.trim();
                            }
                        }
                    }
                }
                // Pass 2: substring match (cleanExpected must be at least 4 characters to avoid false positives)
                for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
                    String cleanActual = entry.getKey().replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
                    if (cleanActual.isEmpty()) continue;
                    for (String expected : possibleHeaders) {
                        String cleanExpected = expected.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
                        if (cleanExpected.length() >= 4 && cleanActual.contains(cleanExpected)) {
                            String val = record.get(entry.getValue());
                            if (val != null && !val.trim().isEmpty()) {
                                return val.trim();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private List<ImportRowResultDTO> parseExcelFile(InputStream inputStream) throws IOException {
        List<ImportRowResultDTO> rows = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        int rowNum = 0;
        Row headerRow = null;
        int idCol = -1, nameCol = -1, classCol = -1, marksCol = -1;

        for (Row row : sheet) {
            rowNum++;
            if (headerRow == null) {
                for (Cell cell : row) {
                    String val = getCellValueAsString(cell).trim().toLowerCase();
                    if (val.contains("studentid") || val.equalsIgnoreCase("id") || val.contains("usn") || val.contains("code") || val.contains("roll")) {
                        idCol = cell.getColumnIndex();
                    } else if (val.contains("name") || val.contains("student name") || val.contains("title")) {
                        nameCol = cell.getColumnIndex();
                    } else if (val.contains("class") || val.contains("standard") || val.contains("std") || val.contains("section") || val.contains("sec") || val.contains("sem")) {
                        classCol = cell.getColumnIndex();
                    } else if (val.equals("%") || val.contains("percent") || val.contains("pct") || val.contains("aggregate")) {
                        marksCol = cell.getColumnIndex();
                    } else if (marksCol == -1 && (val.contains("marks") || val.contains("score") || val.contains("total"))) {
                        marksCol = cell.getColumnIndex();
                    }
                }
                if (idCol != -1 && nameCol != -1 && marksCol != -1) {
                    headerRow = row;
                    continue;
                }
                if (idCol == -1 || nameCol == -1 || marksCol == -1) {
                    idCol = 0; nameCol = 1; classCol = 2; marksCol = 3;
                }
                headerRow = row;
                continue;
            }

            String id = getCellString(row, idCol);
            String name = getCellString(row, nameCol);
            String className = getCellString(row, classCol);
            String marksStr = getCellString(row, marksCol);

            if ((id == null || id.isEmpty()) && (name == null || name.isEmpty()) && (marksStr == null || marksStr.isEmpty())) {
                continue;
            }

            ImportRowResultDTO rowResult = validateRow(rowNum, id, formatStudentName(name), className, marksStr);
            if (rowResult != null && isNotBlacklistedId(rowResult.getStudentId())) {
                rows.add(rowResult);
            }
        }
        workbook.close();
        return rows;
    }

    /**
     * PDF Text Extraction via Apache PDFBox
     */
    private List<ImportRowResultDTO> parsePdfFile(byte[] fileBytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(fileBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String pdfText = stripper.getText(document);
            return parseTextLines(pdfText);
        } catch (Exception e) {
            return parseGenericTextFile(new ByteArrayInputStream(fileBytes));
        }
    }

    /**
     * BULK ZIP ARCHIVE PARSER:
     * Unpacks a ZIP file containing multiple individual student marksheets, PDFs, or spreadsheets
     * in memory, parses each file, and aggregates all student records into a consolidated roster.
     */
    private List<ImportRowResultDTO> parseZipArchive(byte[] zipBytes) throws IOException {
        List<ImportRowResultDTO> aggregatedRows = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();
        int rowNumber = 1;

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    zis.closeEntry();
                    continue;
                }

                String entryName = entry.getName().toLowerCase();
                // Skip OS-generated metadata like __MACOSX, .DS_Store, thumbs.db
                if (entryName.contains("__macosx") || entryName.endsWith(".ds_store") || entryName.endsWith("thumbs.db")) {
                    zis.closeEntry();
                    continue;
                }

                ByteArrayOutputStream entryBaos = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    entryBaos.write(buffer, 0, len);
                }
                byte[] entryBytes = entryBaos.toByteArray();
                zis.closeEntry();

                if (entryBytes.length == 0) continue;

                List<ImportRowResultDTO> entryRows = new ArrayList<>();
                try {
                    if (entryName.endsWith(".xlsx") || entryName.endsWith(".xls")) {
                        entryRows = parseExcelFile(new ByteArrayInputStream(entryBytes));
                    } else if (entryName.endsWith(".pdf")) {
                        entryRows = parsePdfFile(entryBytes);
                    } else if (entryName.endsWith(".json")) {
                        entryRows = parseJsonFile(new ByteArrayInputStream(entryBytes));
                    } else if (entryName.endsWith(".csv")) {
                        entryRows = parseCsvFile(new ByteArrayInputStream(entryBytes));
                    } else {
                        // TXT or other text document
                        entryRows = parseGenericTextFile(new ByteArrayInputStream(entryBytes));
                    }
                } catch (Exception ignored) {
                    try {
                        entryRows = parseGenericTextFile(new ByteArrayInputStream(entryBytes));
                    } catch (Exception e) {
                        // Skip unparseable entry
                    }
                }

                for (ImportRowResultDTO r : entryRows) {
                    String id = r.getStudentId() != null ? r.getStudentId().trim().toUpperCase() : "";
                    if (!id.isEmpty() && !seenIds.contains(id)) {
                        seenIds.add(id);
                        r.setRowNumber(rowNumber++);
                        aggregatedRows.add(r);
                    }
                }
            }
        }
        return aggregatedRows;
    }

    /**
     * JSON File Parser
     */
    private List<ImportRowResultDTO> parseJsonFile(InputStream inputStream) throws IOException {
        List<ImportRowResultDTO> rows = new ArrayList<>();
        JsonNode root = objectMapper.readTree(inputStream);
        if (root.isObject() && root.has("students") && root.get("students").isArray()) {
            root = root.get("students");
        }
        if (root.isArray()) {
            int rowNum = 1;
            for (JsonNode node : root) {
                String id = node.has("studentId") ? node.get("studentId").asText() : (node.has("id") ? node.get("id").asText() : "");
                String name = node.has("name") ? node.get("name").asText() : "";
                String className = node.has("className") ? node.get("className").asText() : (node.has("class") ? node.get("class").asText() : "10A");
                String marksStr = node.has("marks") ? node.get("marks").asText() : (node.has("score") ? node.get("score").asText() : "");

                ImportRowResultDTO row = validateRow(rowNum++, id, formatStudentName(name), className, marksStr);
                if (row != null && isNotBlacklistedId(row.getStudentId())) {
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    /**
     * Generic Text & Document Parser
     */
    private List<ImportRowResultDTO> parseGenericTextFile(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return parseTextLines(sb.toString());
    }

    /**
     * UNIVERSAL MULTI-PATTERN RECORD PARSER:
     * Robustly handles:
     * 1. University/College Class Marksheet Tabular Results (e.g. Rajiv Gandhi Inst / VTU, Anna Univ, CBSE)
     * 2. Delimited Records (CSV, TSV, Pipe, Semicolon)
     * 3. Key-Value Formats (ID: S101 Name: Alice Marks: 94.5)
     * 4. Space-separated Heuristic Records
     * 5. Single-student course list fallback
     */
    private List<ImportRowResultDTO> parseTextLines(String rawText) {
        List<ImportRowResultDTO> rows = new ArrayList<>();
        if (rawText == null || rawText.trim().isEmpty()) {
            return rows;
        }

        String[] rawLines = rawText.split("\\r?\\n");

        // Multiline stitching for PDF / OCR tables where long names wrap onto 2-3 lines
        List<String> lines = new ArrayList<>();
        Pattern usnPrefixPattern = Pattern.compile("(?:^|\\s)(?:\\d{1,4}\\s+)?([0-9][A-Za-z]{2}[0-9]{2}[A-Za-z0-9]{4,6}|[A-Za-z]{1,4}\\d{2,8}[A-Za-z0-9]*)");
        Pattern passFailEndPattern = Pattern.compile("(?i)(?:PASS|FAIL)(?:\\s+[A-Za-z0-9]+)?\\s*$");

        int lineIdx = 0;
        while (lineIdx < rawLines.length) {
            String raw = rawLines[lineIdx].trim();
            if (raw.isEmpty()) {
                lineIdx++;
                continue;
            }

            Matcher mUsn = usnPrefixPattern.matcher(raw);
            if (mUsn.find() && !passFailEndPattern.matcher(raw).find()) {
                StringBuilder combined = new StringBuilder(raw);
                int j = lineIdx + 1;
                boolean foundFinish = false;
                while (j < Math.min(rawLines.length, lineIdx + 5)) {
                    String next = rawLines[j].trim();
                    if (next.isEmpty()) {
                        j++;
                        continue;
                    }
                    combined.append(" ").append(next);
                    if (passFailEndPattern.matcher(next).find()) {
                        foundFinish = true;
                        lineIdx = j;
                        break;
                    }
                    j++;
                }
                if (foundFinish) {
                    lines.add(combined.toString());
                    lineIdx++;
                    continue;
                }
            }

            lines.add(raw);
            lineIdx++;
        }

        // Pattern 1: College/University Student Result Row with multi-subject marks and PASS/FAIL
        // Example: "1 1RG23CS001 ABHISHEK BS 83 81 100 98 95 96 86 99 87 825 91.67 PASS FCD"
        // Example: "14 1RG23CS015 BHANU KIRAN 54 52 98 89 92 94 67 94 39 679 75.44 FAIL"
        Pattern studentResultRowPattern = Pattern.compile(
            "(?i)(?:^|\\s)(?:\\d{1,4}\\s+)?([0-9][A-Za-z]{2}[0-9]{2}[A-Za-z0-9]{4,6}|[A-Za-z]{1,4}\\d{2,8}[A-Za-z0-9]*)\\s+([A-Za-z\\s.'-]+?)\\s+(?:(?:\\d{1,4}(?:\\.\\d+)?)\\s+)+(\\d{1,3}(?:\\.\\d+)?)\\s+(PASS|FAIL)(?:\\s+[A-Za-z0-9]+)?.*$"
        );

        // Pattern 2: University Student Row ending with Percentage (No status word)
        Pattern studentResultNoStatusPattern = Pattern.compile(
            "(?i)(?:^|\\s)(?:\\d{1,4}\\s+)?([0-9][A-Za-z]{2}[0-9]{2}[A-Za-z0-9]{4,6})\\s+([A-Za-z\\s.'-]+?)\\s+(?:(?:\\d{1,4}(?:\\.\\d+)?)\\s+)+(\\d{1,3}(?:\\.\\d+)?)\\s*$"
        );

        // Pattern 3: Standard Delimited Line (ID, Name, Class, Marks)
        Pattern delimitedPattern = Pattern.compile(
            "(?i)(?:^|\\b)([A-Z0-9_-]{2,15})\\s*[,\\t;|]+\\s*([A-Za-z0-9\\s.'-]+?)\\s*[,\\t;|]+\\s*([A-Z0-9]+)?\\s*[,\\t;|]*\\s*(\\d{1,3}(?:\\.\\d+)?)\\b"
        );

        // Pattern 4: Key-Value Line (ID: S101 Name: Alice Marks: 94.5)
        Pattern keyValuePattern = Pattern.compile(
            "(?i)(?:id|usn|code|roll)\\s*[:=]\\s*([A-Z0-9_-]+).*?(?:name|title)\\s*[:=]\\s*([A-Za-z0-9\\s.'-]+).*?(?:marks|score|total|percentage|%)\\s*[:=]\\s*(\\d{1,3}(?:\\.\\d+)?)"
        );

        // Pattern 5: General Heuristic Line (ID at start, Name in middle, Marks at end)
        Pattern heuristicPattern = Pattern.compile(
            "(?i)^\\s*([A-Z0-9_-]{2,15})\\s+([A-Za-z\\s.'-]+?)\\s+(?:([A-Za-z0-9]{1,6})\\s+)?(\\d{1,3}(?:\\.\\d+)?)\\s*$"
        );

        String currentSem = "VI";
        String currentClass = "VI-A";
        Set<String> seenIds = new HashSet<>();
        int rowNum = 1;

        // --- PASS 1: PRIMARY EXTRACTION (Detect Real Students) ---
        for (String line : lines) {
            String clean = line.trim();
            if (clean.isEmpty()) continue;

            String lower = clean.toLowerCase();

            // Track Semester Header if present
            Matcher mSem = Pattern.compile("(?i)(?:sem|semester)\\s*[:\\-]?\\s*([ivx0-9]+)").matcher(clean);
            if (mSem.find()) {
                currentSem = mSem.group(1).toUpperCase();
            }

            // Track Section & Class Headers with flexible whitespace
            Matcher mSec = Pattern.compile("(?i)(?:section|sec|sec\\.)\\s*[:\\-]?\\s*([a-z0-9]+)").matcher(clean);
            if (mSec.find()) {
                String sec = mSec.group(1).toUpperCase();
                currentClass = currentSem != null ? currentSem + "-" + sec : "Section " + sec;
                continue;
            }

            // Skip Summary / Topper / Faculty analysis tables to prevent duplicate USNs
            if (lower.contains("first 5 toppers") || lower.contains("first 10 toppers") || lower.contains("subject wise analysis")
                    || lower.contains("subjectwise analysis") || lower.contains("faculty coordinator")
                    || lower.contains("subject code subject name")) {
                continue;
            }

            if (isHeaderOrAddressLine(clean)) {
                continue;
            }

            // Strategy 1: University/College Multi-Subject Student Result Row
            Matcher mUniv = studentResultRowPattern.matcher(clean);
            if (mUniv.find()) {
                String id = mUniv.group(1).toUpperCase();
                String rawName = mUniv.group(2).trim();
                String marksStr = mUniv.group(3);
                String statusStr = mUniv.group(4).toUpperCase();

                if (isNotBlacklistedId(id) && !seenIds.contains(id)) {
                    seenIds.add(id);
                    ImportRowResultDTO row = validateRow(rowNum++, id, formatStudentName(rawName), currentClass, marksStr);
                    if (row != null) {
                        if ("FAIL".equalsIgnoreCase(statusStr)) {
                            row.setStatus("FAILED");
                        }
                        rows.add(row);
                        continue;
                    }
                }
            }

            // Strategy 2: University Student Row ending with Percentage (No status word)
            Matcher mUnivNoStatus = studentResultNoStatusPattern.matcher(clean);
            if (mUnivNoStatus.find()) {
                String id = mUnivNoStatus.group(1).toUpperCase();
                String rawName = mUnivNoStatus.group(2).trim();
                String marksStr = mUnivNoStatus.group(3);

                if (isNotBlacklistedId(id) && !seenIds.contains(id)) {
                    seenIds.add(id);
                    ImportRowResultDTO row = validateRow(rowNum++, id, formatStudentName(rawName), currentClass, marksStr);
                    if (row != null) {
                        rows.add(row);
                        continue;
                    }
                }
            }

            // Strategy 3: Delimited Pattern (CSV, Tab, Pipe)
            Matcher mDelim = delimitedPattern.matcher(clean);
            if (mDelim.find()) {
                String id = mDelim.group(1).toUpperCase();
                String name = mDelim.group(2).trim();
                String className = mDelim.group(3) != null ? mDelim.group(3).trim() : currentClass;
                String marksStr = mDelim.group(4);

                if (isNotBlacklistedId(id) && !seenIds.contains(id)) {
                    seenIds.add(id);
                    ImportRowResultDTO row = validateRow(rowNum++, id, formatStudentName(name), className, marksStr);
                    if (row != null) {
                        rows.add(row);
                        continue;
                    }
                }
            }

            // Strategy 4: Key-Value Pattern
            Matcher mKeyVal = keyValuePattern.matcher(clean);
            if (mKeyVal.find()) {
                String id = mKeyVal.group(1).toUpperCase();
                String name = mKeyVal.group(2).trim();
                String marksStr = mKeyVal.group(3);

                if (isNotBlacklistedId(id) && !seenIds.contains(id)) {
                    seenIds.add(id);
                    ImportRowResultDTO row = validateRow(rowNum++, id, formatStudentName(name), currentClass, marksStr);
                    if (row != null) {
                        rows.add(row);
                        continue;
                    }
                }
            }

            // Strategy 5: Heuristic Pattern
            Matcher mHeuristic = heuristicPattern.matcher(clean);
            if (mHeuristic.find()) {
                String id = mHeuristic.group(1).toUpperCase();
                String name = mHeuristic.group(2).trim();
                String className = mHeuristic.group(3) != null ? mHeuristic.group(3).trim() : currentClass;
                String marksStr = mHeuristic.group(4);

                if (isNotBlacklistedId(id) && !seenIds.contains(id) && name.length() >= 2) {
                    seenIds.add(id);
                    ImportRowResultDTO row = validateRow(rowNum++, id, formatStudentName(name), className, marksStr);
                    if (row != null) {
                        rows.add(row);
                        continue;
                    }
                }
            }
        }

        // If primary extraction succeeded in finding students, return them!
        if (!rows.isEmpty()) {
            return rows;
        }

        // Strategy 6: Individual Student Marks Card (e.g. individual PDF result / scorecard)
        ImportRowResultDTO singleCard = extractIndividualStudentCard(rawText, currentClass, rowNum);
        if (singleCard != null && !seenIds.contains(singleCard.getStudentId())) {
            rows.add(singleCard);
            return rows;
        }

        // --- PASS 2: FALLBACK (Single Student Course List / Transcript only if 0 students found) ---
        Pattern courseLinePattern = Pattern.compile("(?i)^\\s*([A-Z0-9_-]{3,12})\\s+(.+?)\\s+(\\d{1,3})\\s+(\\d{1,3})\\s+(\\d{1,3})\\s*$");
        for (String line : lines) {
            String clean = line.trim();
            if (clean.isEmpty() || isHeaderOrAddressLine(clean)) continue;

            Matcher mCourse = courseLinePattern.matcher(clean);
            if (mCourse.find()) {
                String subCode = mCourse.group(1).toUpperCase();
                String nameAndProf = mCourse.group(2).trim();
                double cie = Double.parseDouble(mCourse.group(3));
                double see = Double.parseDouble(mCourse.group(5));

                if (isNotBlacklistedId(subCode) && !seenIds.contains(subCode)) {
                    seenIds.add(subCode);
                    String cleanSubject = cleanSubjectTitle(nameAndProf);
                    double totalMarks = cie + see;
                    if (totalMarks > 100.0) totalMarks = 100.0;

                    ImportRowResultDTO row = validateRow(rowNum++, subCode, cleanSubject, currentClass, String.format(Locale.US, "%.2f", totalMarks));
                    if (row != null) {
                        rows.add(row);
                    }
                }
            }
        }

        return rows;
    }

    /**
     * Extracts a student record from an individual marksheet / scorecard document (e.g., individual PDF result).
     */
    private ImportRowResultDTO extractIndividualStudentCard(String rawText, String defaultClass, int rowNum) {
        if (rawText == null || rawText.trim().isEmpty()) {
            return null;
        }

        // Must find an explicit USN or Student ID label
        Pattern idLabelPattern = Pattern.compile("(?i)(?:USN|Student\\s*ID|Roll\\s*No\\.?|Reg\\.?\\s*No\\.?)\\s*[:=]\\s*([0-9][A-Za-z]{2}[0-9]{2}[A-Za-z0-9]{4,6}|[A-Za-z0-9_-]{3,15})");
        Matcher mId = idLabelPattern.matcher(rawText);
        if (!mId.find()) {
            return null;
        }
        String id = mId.group(1).trim().toUpperCase();
        if (!isNotBlacklistedId(id)) {
            return null;
        }

        // Must find Student Name
        Pattern namePattern = Pattern.compile("(?i)(?:Student\\s*Name|Name\\s*of\\s*(?:the\\s*)?Student|Candidate\\s*Name|Name)\\s*[:=]\\s*([A-Za-z\\s.'-]+?)(?:\\r?\\n|$)");
        Matcher mName = namePattern.matcher(rawText);
        String name = "";
        if (mName.find()) {
            name = formatStudentName(mName.group(1).trim());
        }
        if (name.isEmpty() || name.length() < 2) {
            return null;
        }

        // Find Semester or Class
        String className = defaultClass != null ? defaultClass : "VI-A";
        Pattern classPattern = Pattern.compile("(?i)(?:Sem(?:ester)?|Class|Section)\\s*[:=]?\\s*([A-Za-z0-9-]+)");
        Matcher mClass = classPattern.matcher(rawText);
        if (mClass.find()) {
            String semVal = mClass.group(1).trim().toUpperCase();
            if (semVal.length() <= 8) {
                className = semVal;
            }
        }

        // Find Marks / Percentage
        String marksStr = null;
        // Priority 1: Percentage
        Pattern pctPattern = Pattern.compile("(?i)(?:Percentage|Aggregate|Percent|%|Marks\\s*Percentage)\\s*[:=]?\\s*(\\d{1,3}(?:\\.\\d+)?)");
        Matcher mPct = pctPattern.matcher(rawText);
        if (mPct.find()) {
            marksStr = mPct.group(1).trim();
        } else {
            // Priority 2: Total Marks / Max Marks ratio (e.g. 825 / 900)
            Pattern totalPattern = Pattern.compile("(?i)(?:Total\\s*Marks|Grand\\s*Total|Total)\\s*[:=]?\\s*(\\d{2,4}(?:\\.\\d+)?)\\s*[/]\\s*(\\d{2,4}(?:\\.\\d+)?)");
            Matcher mTotal = totalPattern.matcher(rawText);
            if (mTotal.find()) {
                double obtained = Double.parseDouble(mTotal.group(1));
                double max = Double.parseDouble(mTotal.group(2));
                if (max > 0) {
                    double pct = (obtained / max) * 100.0;
                    marksStr = String.format(Locale.US, "%.2f", pct);
                }
            } else {
                // Priority 3: Simple Total (if <= 100)
                Pattern simpleTotal = Pattern.compile("(?i)(?:Marks|Score|Total)\\s*[:=]?\\s*(\\d{1,3}(?:\\.\\d+)?)");
                Matcher mSimple = simpleTotal.matcher(rawText);
                if (mSimple.find()) {
                    marksStr = mSimple.group(1).trim();
                }
            }
        }

        if (marksStr == null) {
            return null;
        }

        ImportRowResultDTO row = validateRow(rowNum, id, name, className, marksStr);
        if (row != null) {
            // Check status (PASS / FAIL)
            Pattern statusPattern = Pattern.compile("(?i)(?:Result|Status)\\s*[:=]?\\s*(PASS|FAIL|FAILED)");
            Matcher mStatus = statusPattern.matcher(rawText);
            if (mStatus.find()) {
                String st = mStatus.group(1).toUpperCase();
                if (st.contains("FAIL")) {
                    row.setStatus("FAILED");
                } else if (st.contains("PASS")) {
                    row.setStatus("PASSED");
                }
            }
            return row;
        }
        return null;
    }

    private boolean isHeaderOrAddressLine(String line) {
        if (line == null) return true;
        String lower = line.toLowerCase().trim();
        if (lower.isEmpty()) return true;

        return lower.startsWith("studentid") || lower.startsWith("student id")
                || lower.startsWith("row") || lower.startsWith("#")
                || lower.contains("cholanagar") || lower.contains("bangalore")
                || lower.contains("karnataka") || lower.contains("pincode")
                || lower.contains("academic year") || lower.contains("university")
                || lower.contains("rajiv gandhi institute") || lower.contains("department of computer science")
                || lower.contains("college of engineering") || lower.contains("provisional marks")
                || lower.contains("statement of marks") || lower.contains("autonomous institute")
                || lower.contains("affiliated to") || lower.contains("rt nagar")
                || lower.contains("post, bangalore") || lower.contains("560032")
                || lower.contains("5600") || lower.startsWith("sl.no")
                || lower.startsWith("sl no") || lower.startsWith("usn name")
                || lower.startsWith("course code") || lower.contains("visvesvaraya")
                || lower.contains("result analysis") || lower.contains("result anaylsis")
                || lower.contains("subject wise analysis") || lower.contains("subjectwise analysis")
                || lower.contains("overall analysis") || lower.contains("overall analusis")
                || lower.contains("faculty coordinator") || lower.contains("faculty incharge")
                || lower.contains("principal") || lower.contains("cse hod")
                || lower.contains("first 5 toppers") || lower.contains("toppers");
    }

    private boolean isNotBlacklistedId(String id) {
        if (id == null || id.trim().length() < 2) return false;
        String clean = id.trim().toUpperCase();
        if (BLACKLISTED_IDS.contains(clean)) return false;
        if (clean.matches("^\\d{1,6}$")) return false;
        return true;
    }

    private String formatStudentName(String name) {
        if (name == null) return "";
        String clean = name.trim();
        clean = clean.replaceAll("^[^A-Za-z]+|[^A-Za-z.]+$", "").trim();
        if (clean.isEmpty()) return name.trim();

        if (clean.equals(clean.toUpperCase()) && clean.length() > 2) {
            String[] words = clean.split("\\s+");
            StringBuilder sb = new StringBuilder();
            for (String w : words) {
                if (w.length() <= 2) {
                    sb.append(w.toUpperCase()).append(" ");
                } else {
                    sb.append(Character.toUpperCase(w.charAt(0)))
                      .append(w.substring(1).toLowerCase()).append(" ");
                }
            }
            return sb.toString().trim();
        }
        return clean;
    }

    private String cleanSubjectTitle(String title) {
        if (title == null) return "";
        String clean = title.trim();
        clean = clean.replaceAll("(?i)\\b(Prof|Dr|Mr|Mrs|Ms|Asst\\.?\\s*Prof|Assoc\\.?\\s*Prof)\\b.*$", "").trim();
        clean = clean.replaceAll("(?i)\\b(Prof|Dr|Mr|Mrs|Ms)\\.[A-Za-z0-9\\s.'-]+", "").trim();
        clean = clean.replaceAll("\\s+\\d+(\\s+\\d+)*$", "").trim();
        if (clean.isEmpty()) return title.trim();

        if (clean.equals(clean.toUpperCase()) && clean.length() > 3) {
            String[] words = clean.split("\\s+");
            StringBuilder sb = new StringBuilder();
            for (String w : words) {
                if (w.length() <= 3 && !w.equalsIgnoreCase("lab") && !w.equalsIgnoreCase("web")) {
                    sb.append(w.toUpperCase()).append(" ");
                } else {
                    sb.append(Character.toUpperCase(w.charAt(0)))
                      .append(w.substring(1).toLowerCase()).append(" ");
                }
            }
            return sb.toString().trim();
        }
        return clean;
    }

    private String getCellString(Row row, int colIdx) {
        if (colIdx < 0 || row == null) return null;
        Cell cell = row.getCell(colIdx);
        return getCellValueAsString(cell);
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) return cell.getDateCellValue().toString();
                double d = cell.getNumericCellValue();
                if (d == (long) d) return String.format("%d", (long) d);
                return String.format(Locale.US, "%.2f", d);
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA: return cell.getCellFormula();
            default: return "";
        }
    }

    private ImportRowResultDTO validateRow(int rowNum, String id, String name, String className, String marksStr) {
        if (!isNotBlacklistedId(id)) {
            return null;
        }

        ImportRowResultDTO row = new ImportRowResultDTO();
        row.setRowNumber(rowNum);
        row.setStudentId(id);
        row.setName(name);

        String finalClass = className != null ? className.trim() : "";
        if (finalClass.isEmpty() || "10A".equalsIgnoreCase(finalClass)) {
            if (id != null && (id.matches("(?i)^1[A-Z]{2}\\d{2}[A-Z0-9]+$") || id.toLowerCase().contains("vtu"))) {
                finalClass = "VTU Engineering";
            } else if (id != null && (id.toLowerCase().contains("puc") || id.toLowerCase().contains("12th") || id.toLowerCase().contains("11th"))) {
                finalClass = "PUC";
            } else if (finalClass.isEmpty()) {
                finalClass = "General";
            }
        }
        row.setClassName(finalClass);

        if (id == null || id.trim().isEmpty()) {
            row.setValid(false);
            row.setErrorReason("Student ID is required.");
            return row;
        }

        if (name == null || name.trim().isEmpty()) {
            row.setValid(false);
            row.setErrorReason("Student Name is required.");
            return row;
        }

        if (marksStr == null || marksStr.trim().isEmpty()) {
            row.setValid(false);
            row.setErrorReason("Marks value is required.");
            return row;
        }

        try {
            String cleanMarks = marksStr.trim().replace("%", "").trim();
            double marks;
            if (cleanMarks.contains("/")) {
                String[] parts = cleanMarks.split("/");
                double obt = Double.parseDouble(parts[0].trim());
                double max = Double.parseDouble(parts[1].trim());
                if (max <= 0) {
                    row.setValid(false);
                    row.setErrorReason("Maximum marks must be greater than 0.");
                    return row;
                }
                marks = (obt / max) * 100.0;
            } else {
                marks = Double.parseDouble(cleanMarks);
                // If marks is total marks > 100 (e.g. 540 out of 600 or 560 out of 625), normalize to percentage
                if (marks > 100.0) {
                    boolean isSslc = (finalClass.toLowerCase().contains("state") || finalClass.toLowerCase().contains("sslc") || (id != null && id.toLowerCase().contains("sslc")));
                    if (isSslc && marks <= 625.0) {
                        marks = (marks / 625.0) * 100.0;
                    } else if (marks <= 150.0) {
                        marks = (marks / 150.0) * 100.0;
                    } else if (marks <= 200.0) {
                        marks = (marks / 200.0) * 100.0;
                    } else if (marks <= 300.0) {
                        marks = (marks / 300.0) * 100.0;
                    } else if (marks <= 400.0) {
                        marks = (marks / 400.0) * 100.0;
                    } else if (marks <= 500.0) {
                        marks = (marks / 500.0) * 100.0;
                    } else if (marks <= 600.0) {
                        marks = (marks / 600.0) * 100.0;
                    } else if (marks <= 625.0) {
                        marks = (marks / 625.0) * 100.0;
                    } else if (marks <= 800.0) {
                        marks = (marks / 800.0) * 100.0;
                    } else if (marks <= 900.0) {
                        marks = (marks / 900.0) * 100.0;
                    } else if (marks <= 1000.0) {
                        marks = (marks / 1000.0) * 100.0;
                    }
                }
            }
            marks = Math.round(marks * 100.0) / 100.0;

            if (!gradeCalculator.isValidMarks(marks)) {
                row.setValid(false);
                row.setErrorReason("Marks must be between 0 and 100 (Found: " + marks + ").");
                return row;
            }
            row.setMarks(marks);
            row.setGrade(gradeCalculator.calculateGrade(marks));
            row.setStatus(gradeCalculator.calculateStatus(marks));
            row.setValid(true);
        } catch (NumberFormatException e) {
            row.setValid(false);
            row.setErrorReason("Invalid numeric marks format ('" + marksStr + "').");
        }

        return row;
    }

    // --- Export Generators ---

    public byte[] generateSampleCsvTemplate() {
        String csvContent = "studentId,name,className,marks\n" +
                "S101,Alice Johnson,10A,94.5\n" +
                "S102,Bob Smith,10A,82.0\n" +
                "S103,Charlie Brown,10A,74.0\n" +
                "S104,David Miller,10A,65.5\n" +
                "S105,Eva Green,10A,52.0\n";
        return csvContent.getBytes(StandardCharsets.UTF_8);
    }

    public byte[] generateRejectedRowsCsv(List<ImportRowResultDTO> rejectedRows) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder().setHeader("RowNumber", "StudentId", "Name", "Class", "Marks", "ErrorReason").build())) {
            for (ImportRowResultDTO r : rejectedRows) {
                printer.printRecord(r.getRowNumber(), r.getStudentId(), r.getName(), r.getClassName(), r.getMarks(), r.getErrorReason());
            }
            printer.flush();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate rejected rows CSV", e);
        }
        return out.toByteArray();
    }

    public byte[] exportToCsv(List<StudentDTO> students) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder().setHeader("Student ID", "Student Name", "Class/Section", "Marks", "Letter Grade", "Status").build())) {
            for (StudentDTO s : students) {
                printer.printRecord(s.getStudentId(), s.getName(), s.getClassName(), s.getMarks(), s.getGrade(), s.getStatus());
            }
            printer.flush();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate CSV export", e);
        }
        return out.toByteArray();
    }

    public byte[] exportToExcel(List<StudentDTO> students) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Student Grade Report");

            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font font = workbook.createFont();
            font.setBold(true);
            font.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.INDIGO.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"Student ID", "Student Name", "Class/Section", "Marks", "Grade", "Status"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (StudentDTO s : students) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(s.getStudentId());
                row.createCell(1).setCellValue(s.getName());
                row.createCell(2).setCellValue(s.getClassName());
                row.createCell(3).setCellValue(s.getMarks());
                row.createCell(4).setCellValue(s.getGrade());
                row.createCell(5).setCellValue(s.getStatus());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel export", e);
        }
    }

    public byte[] exportToPdf(List<StudentDTO> students) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, java.awt.Color.DARK_GRAY);
            Paragraph title = new Paragraph("STUDENT GRADE REPORT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 3.0f, 1.5f, 1.5f, 1.2f, 1.5f});

            String[] headers = {"ID", "Name", "Class", "Marks", "Grade", "Status"};
            com.lowagie.text.Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, java.awt.Color.WHITE);

            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headFont));
                cell.setBackgroundColor(new java.awt.Color(99, 102, 241));
                cell.setPadding(6);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            com.lowagie.text.Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
            for (StudentDTO s : students) {
                table.addCell(new Phrase(s.getStudentId(), dataFont));
                table.addCell(new Phrase(s.getName(), dataFont));
                table.addCell(new Phrase(s.getClassName(), dataFont));
                table.addCell(new Phrase(String.format("%.2f", s.getMarks()), dataFont));
                table.addCell(new Phrase(s.getGrade(), dataFont));
                table.addCell(new Phrase(s.getStatus(), dataFont));
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF export", e);
        }
    }

    private StudentDTO toDTO(StudentEntity entity) {
        StudentDTO dto = new StudentDTO();
        dto.setId(entity.getId());
        dto.setStudentId(entity.getStudentId());
        dto.setName(entity.getName());
        dto.setClassName(entity.getClassName());
        dto.setMarks(entity.getMarks());
        dto.setGrade(entity.getGrade());
        dto.setStatus(entity.getStatus());
        dto.setSubjectBreakdown(entity.getSubjectBreakdown());
        return dto;
    }
}
