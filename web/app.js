/**
 * Student Grade Tracker - Web App Logic
 * Connected to Java Spring Boot REST API
 */

const API_BASE_URL = '/api/students';

// App State
let students = [];
let parsedPreviewData = null;
let activeTab = 'dashboard';
let studentToDelete = null;

// DOM Initialization
document.addEventListener('DOMContentLoaded', () => {
    initTabs();
    initModals();
    initForms();
    initSearchFilter();
    initBulkImport();
    initClearAllButton();

    // Initial Data Fetch from Java Backend
    loadStudentsFromBackend();
    loadStatsFromBackend();
});

function initClearAllButton() {
    const btn = document.getElementById('btn-clear-all');
    if (!btn) return;
    btn.addEventListener('click', async () => {
        if (confirm("Are you sure you want to delete ALL student records from the database? This cannot be undone.")) {
            try {
                const resp = await fetch(`${API_BASE_URL}/all`, { method: 'DELETE' });
                if (resp.ok) {
                    showToast("All student records have been cleared from the database.", "success");
                    loadStudentsFromBackend();
                } else {
                    showToast("Failed to clear database.", "error");
                }
            } catch (e) {
                showToast("Error connecting to server.", "error");
            }
        }
    });
}

// Navigation Tabs
function initTabs() {
    const navButtons = document.querySelectorAll('.nav-btn');
    navButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const tabName = btn.dataset.tab;
            switchTab(tabName);
        });
    });

    document.getElementById('btn-view-all-redirect')?.addEventListener('click', () => {
        switchTab('students');
    });
}

function switchTab(tabName) {
    activeTab = tabName;
    document.querySelectorAll('.nav-btn').forEach(b => {
        b.classList.toggle('active', b.dataset.tab === tabName);
    });
    document.querySelectorAll('.tab-content').forEach(c => {
        c.classList.toggle('active', c.id === `tab-${tabName}`);
    });

    const titles = {
        'dashboard': { title: 'Dashboard Overview', subtitle: 'Manage student marks, track performance, and analyze statistics.' },
        'students': { title: 'All Registered Students', subtitle: 'Complete list of students with live search, filter, edit, and delete options.' },
        'import': { title: 'Bulk Student Import', subtitle: 'Upload CSV or Excel (.xlsx) files to preview, validate, and import multiple student records.' },
        'report': { title: 'Student Grade Report', subtitle: 'Official class grade breakdown and export to CSV, Excel, or PDF.' },
        'analytics': { title: 'Grade Scale & Information', subtitle: 'Reference for standard grade letter mappings and backend parameters.' }
    };

    if (titles[tabName]) {
        document.getElementById('page-title').textContent = titles[tabName].title;
        document.getElementById('page-subtitle').textContent = titles[tabName].subtitle;
    }

    if (tabName === 'students' || tabName === 'report' || tabName === 'dashboard') {
        loadStudentsFromBackend();
    }
}

// Fetch Students from Spring Boot API
async function loadStudentsFromBackend() {
    try {
        const searchQuery = document.getElementById('input-search')?.value || '';
        const gradeFilter = document.getElementById('select-grade-filter')?.value || 'ALL';
        const statusFilter = document.getElementById('select-status-filter')?.value || 'ALL';
        const sortBy = document.getElementById('select-sort-by')?.value || 'studentId';

        const params = new URLSearchParams();
        if (searchQuery) params.append('search', searchQuery);
        if (gradeFilter && gradeFilter !== 'ALL') params.append('grade', gradeFilter);
        if (statusFilter && statusFilter !== 'ALL') params.append('status', statusFilter);
        if (sortBy) params.append('sortBy', sortBy);

        const response = await fetch(`${API_BASE_URL}?${params.toString()}`);
        if (!response.ok) throw new Error('Failed to connect to Java backend');

        students = await response.json();
        updateServerStatus(true);
        renderApp();
        updateExportLinks(params.toString());
    } catch (err) {
        console.warn("Backend API connection fallback:", err);
        updateServerStatus(false);
    }
}

async function loadStatsFromBackend() {
    try {
        const response = await fetch(`${API_BASE_URL}/stats`);
        if (!response.ok) return;
        const stats = await response.json();
        renderStatsFromDTO(stats);
    } catch (e) {
        // Handled gracefully
    }
}

function updateServerStatus(online) {
    const pill = document.getElementById('server-status-pill');
    const text = document.getElementById('server-status-text');
    if (!pill || !text) return;

    if (online) {
        pill.className = 'server-status-pill online';
        text.textContent = 'Backend Connected (Java 21)';
    } else {
        pill.className = 'server-status-pill offline';
        text.textContent = 'Backend Connecting...';
    }
}

function updateExportLinks(queryString) {
    const csvBtn = document.getElementById('btn-export-csv');
    const excelBtn = document.getElementById('btn-export-excel');
    const pdfBtn = document.getElementById('btn-export-pdf');

    if (csvBtn) csvBtn.href = `${API_BASE_URL}/export/csv?${queryString}`;
    if (excelBtn) excelBtn.href = `${API_BASE_URL}/export/excel?${queryString}`;
    if (pdfBtn) pdfBtn.href = `${API_BASE_URL}/export/pdf?${queryString}`;
}

// Render Application UI
function renderApp() {
    renderDashboardPreview();
    renderStudentsTable();
    renderGradeReport();
    loadStatsFromBackend();
}

function renderStatsFromDTO(stats) {
    document.getElementById('stat-total-students').textContent = stats.totalStudents;
    document.getElementById('stat-class-average').textContent = `${stats.averageMarks.toFixed(2)}%`;
    document.getElementById('stat-highest-marks').textContent = stats.highestMarks.toFixed(2);
    document.getElementById('stat-lowest-marks').textContent = stats.lowestMarks.toFixed(2);

    document.getElementById('stat-highest-student').textContent = stats.highestScorers || 'None';
    document.getElementById('stat-lowest-student').textContent = stats.lowestScorers || 'None';

    const dist = stats.gradeDistribution || { A: 0, B: 0, C: 0, D: 0, F: 0 };
    const total = stats.totalStudents;

    ['A', 'B', 'C', 'D', 'F'].forEach(grade => {
        const count = dist[grade] || 0;
        const pct = total > 0 ? ((count / total) * 100).toFixed(1) : 0;
        document.getElementById(`count-grade-${grade}`).textContent = `${count} (${pct}%)`;
        document.getElementById(`bar-grade-${grade}`).style.width = `${pct}%`;
    });
}

function renderDashboardPreview() {
    const tbody = document.getElementById('tbody-dashboard-preview');
    tbody.innerHTML = '';

    if (students.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted">No students found.</td></tr>`;
        return;
    }

    const recent = students.slice(0, 5);
    recent.forEach(s => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>${escapeHtml(s.studentId)}</strong></td>
            <td>${escapeHtml(s.name)}</td>
            <td>${escapeHtml(s.className || '10A')}</td>
            <td>${parseFloat(s.marks).toFixed(2)}</td>
            <td><span class="badge ${getGradeBadgeClass(s.grade)}">${s.grade}</span></td>
            <td>
                <button class="btn-icon-action edit" onclick="openEditModal('${escapeHtml(s.studentId)}')"><i class="fa-solid fa-pen"></i></button>
                <button class="btn-icon-action delete" onclick="openDeleteModal('${escapeHtml(s.studentId)}')"><i class="fa-solid fa-trash"></i></button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function renderStudentsTable() {
    const tbody = document.getElementById('tbody-students-full');
    tbody.innerHTML = '';

    if (students.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--text-muted); padding: 2rem;">No matching students found.</td></tr>`;
        return;
    }

    students.forEach(s => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>${escapeHtml(s.studentId)}</strong></td>
            <td>${escapeHtml(s.name)}</td>
            <td>${escapeHtml(s.className || '10A')}</td>
            <td>${parseFloat(s.marks).toFixed(2)}</td>
            <td><span class="badge ${getGradeBadgeClass(s.grade)}">Grade ${s.grade}</span></td>
            <td>${s.status === 'PASSED' ? '<span style="color: #34d399; font-weight: 600;"><i class="fa-solid fa-circle-check"></i> Passed</span>' : '<span style="color: #fb7185; font-weight: 600;"><i class="fa-solid fa-triangle-exclamation"></i> Failed</span>'}</td>
            <td>
                <button class="btn-icon-action edit" onclick="openEditModal('${escapeHtml(s.studentId)}')"><i class="fa-solid fa-pen"></i></button>
                <button class="btn-icon-action delete" onclick="openDeleteModal('${escapeHtml(s.studentId)}')"><i class="fa-solid fa-trash"></i></button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function renderGradeReport() {
    const tbody = document.getElementById('tbody-grade-report');
    tbody.innerHTML = '';

    const total = students.length;
    document.getElementById('report-total').textContent = total;

    if (total === 0) {
        document.getElementById('report-average').textContent = '0.00';
        document.getElementById('report-highest').textContent = '0.00';
        document.getElementById('report-lowest').textContent = '0.00';
        tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--text-muted); padding: 2rem;">No students registered.</td></tr>`;
        return;
    }

    let sum = 0, max = -Infinity, min = Infinity;
    students.forEach(s => {
        const m = parseFloat(s.marks);
        sum += m;
        if (m > max) max = m;
        if (m < min) min = m;
    });

    document.getElementById('report-average').textContent = (sum / total).toFixed(2);
    document.getElementById('report-highest').textContent = max.toFixed(2);
    document.getElementById('report-lowest').textContent = min.toFixed(2);

    students.forEach((s, idx) => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${idx + 1}</td>
            <td><strong>${escapeHtml(s.studentId)}</strong></td>
            <td>${escapeHtml(s.name)}</td>
            <td>${escapeHtml(s.className || '10A')}</td>
            <td>${parseFloat(s.marks).toFixed(2)}</td>
            <td><span class="badge ${getGradeBadgeClass(s.grade)}">Grade ${s.grade}</span></td>
            <td>${s.status === 'PASSED' ? '<span style="color: #34d399;">Passed</span>' : '<span style="color: #fb7185;">Failed</span>'}</td>
        `;
        tbody.appendChild(tr);
    });
}

// Bulk Import File Handler (FEATURE #5)
function initBulkImport() {
    const dropzone = document.getElementById('upload-dropzone');
    const fileInput = document.getElementById('file-import-input');

    if (!dropzone || !fileInput) return;

    ['dragenter', 'dragover'].forEach(eventName => {
        dropzone.addEventListener(eventName, (e) => {
            e.preventDefault();
            dropzone.classList.add('dragover');
        });
    });

    ['dragleave', 'drop'].forEach(eventName => {
        dropzone.addEventListener(eventName, (e) => {
            e.preventDefault();
            dropzone.classList.remove('dragover');
        });
    });

    dropzone.addEventListener('drop', (e) => {
        const dt = e.dataTransfer;
        const files = dt.files;
        if (files.length > 0) {
            handleFileUpload(files);
        }
    });

    fileInput.addEventListener('change', (e) => {
        if (e.target.files.length > 0) {
            handleFileUpload(e.target.files);
        }
    });

    document.getElementById('btn-cancel-import')?.addEventListener('click', () => {
        parsedPreviewData = null;
        document.getElementById('import-preview-section').style.display = 'none';
        fileInput.value = '';
        showToast('Import cancelled. No records were saved.', 'info');
    });

    document.getElementById('btn-confirm-import')?.addEventListener('click', async () => {
        if (!parsedPreviewData || !parsedPreviewData.rows) return;
        const validRows = parsedPreviewData.rows.filter(r => r.valid).map(r => ({
            studentId: r.studentId,
            name: r.name,
            className: r.className || '10A',
            marks: parseFloat(r.marks),
            grade: r.grade,
            status: r.status
        }));

        if (validRows.length === 0) {
            showToast('No valid rows available to import.', 'error');
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/import/confirm`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ studentsToImport: validRows })
            });

            if (!response.ok) throw new Error('Failed to save imported records');
            const data = await response.json();

            showToast(`Success! ${data.importedCount} student records imported!`, 'success');
            document.getElementById('import-preview-section').style.display = 'none';
            fileInput.value = '';
            parsedPreviewData = null;
            loadStudentsFromBackend();
            switchTab('students');
        } catch (err) {
            showToast(err.message, 'error');
        }
    });
}

async function handleFileUpload(fileInputSource) {
    let files = [];
    if (fileInputSource instanceof FileList || Array.isArray(fileInputSource)) {
        files = Array.from(fileInputSource);
    } else if (fileInputSource instanceof File) {
        files = [fileInputSource];
    }
    if (files.length === 0) return;

    const formData = new FormData();
    if (files.length === 1) {
        const single = files[0];
        formData.append('file', single);
        if (single.name.toLowerCase().endsWith('.zip')) {
            showToast(`Extracting and aggregating records from ${single.name}...`, 'info');
        } else {
            showToast(`Uploading and validating ${single.name}...`, 'info');
        }
    } else {
        files.forEach(f => formData.append('files', f));
        showToast(`Uploading and aggregating ${files.length} files in batch...`, 'info');
    }

    try {
        const response = await fetch(`${API_BASE_URL}/import/preview`, {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            const errData = await response.json();
            throw new Error(errData.error || 'Failed to parse upload file(s)');
        }

        parsedPreviewData = await response.json();
        renderImportPreview(parsedPreviewData);
    } catch (err) {
        showToast(`Import Error: ${err.message}`, 'error');
    }
}

function renderImportPreview(data) {
    document.getElementById('import-preview-section').style.display = 'block';
    document.getElementById('chip-total-rows').textContent = data.totalRows;
    document.getElementById('chip-valid-rows').textContent = data.validCount;
    document.getElementById('chip-invalid-rows').textContent = data.invalidCount;
    document.getElementById('chip-duplicate-rows').textContent = data.duplicateCount;

    const tbody = document.getElementById('tbody-import-preview');
    tbody.innerHTML = '';

    const rejectedRows = data.rows.filter(r => !r.valid);
    const downloadRejectedBtn = document.getElementById('btn-download-rejected-csv');
    if (rejectedRows.length > 0 && downloadRejectedBtn) {
        downloadRejectedBtn.style.display = 'inline-flex';
        downloadRejectedBtn.onclick = () => downloadRejectedRows(rejectedRows);
    } else if (downloadRejectedBtn) {
        downloadRejectedBtn.style.display = 'none';
    }

    data.rows.forEach(r => {
        const tr = document.createElement('tr');
        if (!r.valid) tr.style.background = 'rgba(244, 63, 94, 0.08)';

        tr.innerHTML = `
            <td>${r.rowNumber}</td>
            <td><strong>${escapeHtml(r.studentId || '--')}</strong></td>
            <td>${escapeHtml(r.name || '--')}</td>
            <td>${escapeHtml(r.className || '10A')}</td>
            <td>${r.marks !== null ? parseFloat(r.marks).toFixed(2) : '--'}</td>
            <td>${r.grade ? `<span class="badge ${getGradeBadgeClass(r.grade)}">Grade ${r.grade}</span>` : '--'}</td>
            <td>
                ${r.valid 
                    ? '<span class="badge grade-A"><i class="fa-solid fa-check"></i> Valid</span>' 
                    : `<span class="badge grade-F" title="${escapeHtml(r.errorReason)}"><i class="fa-solid fa-xmark"></i> ${escapeHtml(r.errorReason)}</span>`
                }
            </td>
        `;
        tbody.appendChild(tr);
    });

    document.getElementById('import-preview-section').scrollIntoView({ behavior: 'smooth' });
}

async function downloadRejectedRows(rejectedRows) {
    try {
        const response = await fetch(`${API_BASE_URL}/import/rejected-csv`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(rejectedRows)
        });
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'rejected_students.csv';
        a.click();
    } catch (e) {
        showToast('Failed to download rejected rows CSV', 'error');
    }
}

// Search and Filter Listeners
function initSearchFilter() {
    const gradeSelect = document.getElementById('select-grade-filter');
    const statusSelect = document.getElementById('select-status-filter');
    const sortSelect = document.getElementById('select-sort-by');
    const searchInput = document.getElementById('input-search');

    statusSelect?.addEventListener('change', () => {
        // Automatically resolve impossible conflicting filter: Grade F cannot be Passed
        if (statusSelect.value === 'PASSED' && gradeSelect?.value === 'F') {
            gradeSelect.value = 'ALL';
        }
        loadStudentsFromBackend();
    });

    gradeSelect?.addEventListener('change', () => {
        // Automatically resolve impossible conflicting filter: Grade F cannot be Passed
        if (gradeSelect.value === 'F' && statusSelect?.value === 'PASSED') {
            statusSelect.value = 'ALL';
        }
        loadStudentsFromBackend();
    });

    sortSelect?.addEventListener('change', loadStudentsFromBackend);

    searchInput?.addEventListener('input', () => {
        loadStudentsFromBackend();
    });
    searchInput?.addEventListener('change', loadStudentsFromBackend);
}

// Modals Setup
function initModals() {
    const modalAdd = document.getElementById('modal-add-student');
    const modalEdit = document.getElementById('modal-edit-student');
    const modalDelete = document.getElementById('modal-delete-confirm');

    document.getElementById('btn-open-add-modal')?.addEventListener('click', () => {
        resetAddForm();
        modalAdd.classList.add('show');
    });

    document.getElementById('btn-close-add-modal')?.addEventListener('click', () => modalAdd.classList.remove('show'));
    document.getElementById('btn-cancel-add-modal')?.addEventListener('click', () => modalAdd.classList.remove('show'));

    document.getElementById('btn-close-edit-modal')?.addEventListener('click', () => modalEdit.classList.remove('show'));
    document.getElementById('btn-cancel-edit-modal')?.addEventListener('click', () => modalEdit.classList.remove('show'));

    document.getElementById('btn-close-delete-modal')?.addEventListener('click', () => modalDelete.classList.remove('show'));
    document.getElementById('btn-cancel-delete')?.addEventListener('click', () => modalDelete.classList.remove('show'));

    document.getElementById('btn-confirm-delete')?.addEventListener('click', async () => {
        if (studentToDelete) {
            try {
                const response = await fetch(`${API_BASE_URL}/${encodeURIComponent(studentToDelete.studentId)}`, {
                    method: 'DELETE'
                });
                if (!response.ok) throw new Error('Failed to delete student');
                modalDelete.classList.remove('show');
                showToast(`Student ${studentToDelete.name} deleted successfully!`, 'success');
                studentToDelete = null;
                loadStudentsFromBackend();
            } catch (err) {
                showToast(err.message, 'error');
            }
        }
    });

    // Realtime Grade Previews
    document.getElementById('add-marks')?.addEventListener('input', (e) => {
        updateGradePreview(e.target.value, 'add-grade-preview');
    });
    document.getElementById('edit-marks')?.addEventListener('input', (e) => {
        updateGradePreview(e.target.value, 'edit-grade-preview');
    });
}

function updateGradePreview(val, elemId) {
    const preview = document.getElementById(elemId);
    if (!preview) return;
    const marks = parseFloat(val);
    if (!isNaN(marks) && marks >= 0 && marks <= 100) {
        let g = 'F';
        if (marks >= 90) g = 'A';
        else if (marks >= 80) g = 'B';
        else if (marks >= 70) g = 'C';
        else if (marks >= 60) g = 'D';
        preview.textContent = `Grade ${g}`;
        preview.className = `badge ${getGradeBadgeClass(g)}`;
    } else {
        preview.textContent = '--';
        preview.className = 'badge grade-placeholder';
    }
}

// Form Handlers
function initForms() {
    document.getElementById('form-add-student')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = document.getElementById('add-id').value.trim();
        const name = document.getElementById('add-name').value.trim();
        const className = document.getElementById('add-class').value.trim() || '10A';
        const marks = parseFloat(document.getElementById('add-marks').value);

        try {
            const response = await fetch(API_BASE_URL, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ studentId: id, name, className, marks })
            });

            if (!response.ok) {
                const errData = await response.json();
                throw new Error(errData.error || 'Failed to add student');
            }

            document.getElementById('modal-add-student').classList.remove('show');
            showToast(`Student '${name}' added successfully!`, 'success');
            loadStudentsFromBackend();
        } catch (err) {
            showToast(err.message, 'error');
        }
    });

    document.getElementById('form-edit-student')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = document.getElementById('edit-id').value;
        const name = document.getElementById('edit-name').value.trim();
        const className = document.getElementById('edit-class').value.trim() || '10A';
        const marks = parseFloat(document.getElementById('edit-marks').value);

        try {
            const response = await fetch(`${API_BASE_URL}/${encodeURIComponent(id)}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ studentId: id, name, className, marks })
            });

            if (!response.ok) {
                const errData = await response.json();
                throw new Error(errData.error || 'Failed to update student');
            }

            document.getElementById('modal-edit-student').classList.remove('show');
            showToast(`Student record for ${id} updated successfully!`, 'success');
            loadStudentsFromBackend();
        } catch (err) {
            showToast(err.message, 'error');
        }
    });
}

function openEditModal(studentId) {
    const s = students.find(item => item.studentId === studentId);
    if (!s) return;

    document.getElementById('edit-id').value = s.studentId;
    document.getElementById('edit-name').value = s.name;
    document.getElementById('edit-class').value = s.className || '10A';
    document.getElementById('edit-marks').value = s.marks;

    updateGradePreview(s.marks, 'edit-grade-preview');
    document.getElementById('modal-edit-student').classList.add('show');
}

function openDeleteModal(studentId) {
    const s = students.find(item => item.studentId === studentId);
    if (!s) return;

    studentToDelete = s;
    document.getElementById('delete-target-name').textContent = s.name;
    document.getElementById('delete-target-id').textContent = s.studentId;
    document.getElementById('modal-delete-confirm').classList.add('show');
}

function resetAddForm() {
    document.getElementById('form-add-student')?.reset();
    document.getElementById('add-grade-preview').textContent = '--';
    document.getElementById('add-grade-preview').className = 'badge grade-placeholder';
}

function getGradeBadgeClass(grade) {
    switch (grade) {
        case 'A': return 'grade-A';
        case 'B': return 'grade-B';
        case 'C': return 'grade-C';
        case 'D': return 'grade-D';
        default: return 'grade-F';
    }
}

function showToast(msg, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `<i class="fa-solid fa-circle-info"></i> <span>${escapeHtml(msg)}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

function escapeHtml(str) {
    return String(str || '').replace(/[&<>"']/g, function(m) {
        return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' }[m];
    });
}
