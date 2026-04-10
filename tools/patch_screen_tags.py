"""One-off: insert screenTestTag + import after navController line in listed Kotlin files."""
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "java" / "com" / "example" / "app_my_university" / "ui" / "screens"

PATCHES = [
    ("NotificationsScreen.kt", "NOTIFICATIONS"),
    ("ProfileScreen.kt", "PROFILE"),
    ("TeacherHomeScreen.kt", "TEACHER_HOME"),
    ("RegistrationRequestsScreen.kt", "ADMIN_REQUESTS"),
    ("AdminAuditLogsScreen.kt", "ADMIN_AUDIT"),
    ("StudentPerformanceScreen.kt", "STUDENT_PERFORMANCE"),
    ("AdminStatisticsScreen.kt", "ADMIN_STATISTICS"),
    ("TeacherStatisticsScreen.kt", "TEACHER_STATISTICS"),
    ("UniversityManagementScreen.kt", "ADMIN_UNIVERSITIES"),
    ("SubjectInDirectionManagementScreen.kt", "ADMIN_SUBJECT_PLAN"),
    ("UniversityInstitutesScreen.kt", "ADMIN_UNIVERSITY_INSTITUTES"),
    ("AdminStructureHubScreen.kt", "ADMIN_STRUCTURE"),
    ("SubjectManagementScreen.kt", "ADMIN_SUBJECTS"),
    ("HomeScreen.kt", "STUDENT_HOME"),
    ("AdminMoreScreen.kt", "ADMIN_MORE"),
    ("AdminHomeScreen.kt", "ADMIN_HOME"),
    ("DirectionManagementScreen.kt", "ADMIN_DIRECTIONS"),
    ("AdminClassroomManagementScreen.kt", "ADMIN_CLASSROOMS"),
    ("GroupManagementScreen.kt", "ADMIN_GROUPS"),
    ("AdminTeacherAssignmentScreen.kt", "ADMIN_TEACHER_SUBJECTS"),
    ("UserManagementScreen.kt", "ADMIN_USERS"),
    ("ScheduleManagementScreen.kt", "ADMIN_SCHEDULE"),
    ("ChatContactsScreen.kt", "CHAT_CONTACTS"),
    ("MessagesScreen.kt", "DIALOGS"),
    ("TeacherGradesScreen.kt", "TEACHER_GRADES"),
    ("GradeBookScreen.kt", "GRADEBOOK"),
    ("ScheduleScreen.kt", "SCHEDULE"),
]

IMPORT_LINE = "import com.example.app_my_university.ui.test.UiTestTags\n"
INSERT = "        screenTestTag = UiTestTags.Screen.{tag},\n"

for name, tag in PATCHES:
    p = ROOT / name
    text = p.read_text(encoding="utf-8")
    if "screenTestTag" in text:
        continue
    if "navController = navController,\n        topBar" not in text:
        raise SystemExit(f"Pattern not found in {name}")
    text = text.replace(
        "navController = navController,\n        topBar",
        f"navController = navController,\n{INSERT.format(tag=tag)}        topBar",
        1,
    )
    if IMPORT_LINE not in text:
        # after package line, insert after first blank line after imports block — simple: after `package ...\n`
        m = re.search(r"^package .+\n\n", text, re.M)
        if not m:
            raise SystemExit(f"No package in {name}")
        insert_at = m.end()
        # find last import before first non-import
        lines = text.splitlines(keepends=True)
        last_imp = 0
        for i, ln in enumerate(lines):
            if ln.startswith("import "):
                last_imp = i
        lines.insert(last_imp + 1, IMPORT_LINE)
        text = "".join(lines)
    p.write_text(text, encoding="utf-8")
    print("patched", name)
