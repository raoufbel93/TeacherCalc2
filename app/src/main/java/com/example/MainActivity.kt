package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Student
import com.example.ui.MainViewModel
import com.example.ui.PdfExporter
import com.example.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                // Ensure RTL Layout Direction for Arabic locale alignment
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigationContainer()
                    }
                }
            }
        }
    }
}

enum class ActiveTab {
    CALCULATOR, STUDENTS, STATS, DATA
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigationContainer(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(ActiveTab.CALCULATOR) }
    var showSettings by remember { mutableStateOf(false) }

    // Settings States from viewmodel
    val teacherName by viewModel.teacherName.collectAsStateWithLifecycle()
    val subject by viewModel.subject.collectAsStateWithLifecycle()
    val institution by viewModel.institution.collectAsStateWithLifecycle()
    val academicYear by viewModel.academicYear.collectAsStateWithLifecycle()
    val semester by viewModel.semester.collectAsStateWithLifecycle()
    val maxScore by viewModel.maxScore.collectAsStateWithLifecycle()
    val studentsList by viewModel.students.collectAsStateWithLifecycle()

    // Temporary values inside editing sliders
    var tempTeacher by remember(teacherName) { mutableStateOf(teacherName) }
    var tempSubject by remember(subject) { mutableStateOf(subject) }
    var tempInstitution by remember(institution) { mutableStateOf(institution) }
    var tempYear by remember(academicYear) { mutableStateOf(academicYear) }
    var tempSemester by remember(semester) { mutableStateOf(semester) }
    var tempMax by remember(maxScore) { mutableStateOf(maxScore) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "مصحح الامتحانات",
                        fontWeight = FontWeight.Bold,
                        color = AccentBlue,
                        fontSize = 20.sp
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showSettings = !showSettings },
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "الإعدادات",
                            tint = AccentBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = TabBg
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = TabBg,
                modifier = Modifier.testTag("bottom_navigation")
            ) {
                NavigationBarItem(
                    selected = activeTab == ActiveTab.CALCULATOR,
                    onClick = { activeTab = ActiveTab.CALCULATOR },
                    icon = { Icon(Icons.Default.Calculate, contentDescription = "الحاسبة") },
                    label = { Text("الحاسبة", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TabBg,
                        selectedTextColor = AccentBlue,
                        indicatorColor = AccentBlue,
                        unselectedIconColor = SoftGrey,
                        unselectedTextColor = SoftGrey
                    )
                )
                NavigationBarItem(
                    selected = activeTab == ActiveTab.STUDENTS,
                    onClick = { activeTab = ActiveTab.STUDENTS },
                    icon = { Icon(Icons.Default.People, contentDescription = "الطلاب") },
                    label = { Text("الطلاب", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TabBg,
                        selectedTextColor = AccentBlue,
                        indicatorColor = AccentBlue,
                        unselectedIconColor = SoftGrey,
                        unselectedTextColor = SoftGrey
                    )
                )
                NavigationBarItem(
                    selected = activeTab == ActiveTab.STATS,
                    onClick = { activeTab = ActiveTab.STATS },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "الإحصائيات") },
                    label = { Text("الإحصائيات", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TabBg,
                        selectedTextColor = AccentBlue,
                        indicatorColor = AccentBlue,
                        unselectedIconColor = SoftGrey,
                        unselectedTextColor = SoftGrey
                    )
                )
                NavigationBarItem(
                    selected = activeTab == ActiveTab.DATA,
                    onClick = { activeTab = ActiveTab.DATA },
                    icon = { Icon(Icons.Default.Storage, contentDescription = "البيانات") },
                    label = { Text("البيانات", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TabBg,
                        selectedTextColor = AccentBlue,
                        indicatorColor = AccentBlue,
                        unselectedIconColor = SoftGrey,
                        unselectedTextColor = SoftGrey
                    )
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBg)
        ) {
            // Sliding Settings Panel
            AnimatedVisibility(
                visible = showSettings,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TabBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "معلومات الأستاذ والإعدادات",
                            style = MaterialTheme.typography.titleMedium,
                            color = AccentBlue,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = tempTeacher,
                            onValueChange = { tempTeacher = it },
                            label = { Text("اسم الأستاذ", color = SoftGrey) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentBlue,
                                unfocusedBorderColor = SoftCard
                            )
                        )

                        OutlinedTextField(
                            value = tempSubject,
                            onValueChange = { tempSubject = it },
                            label = { Text("المادة (مثل: الرياضيات)", color = SoftGrey) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentBlue,
                                unfocusedBorderColor = SoftCard
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = tempInstitution,
                                onValueChange = { tempInstitution = it },
                                label = { Text("المؤسسة التعليمية", color = SoftGrey) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentBlue,
                                    unfocusedBorderColor = SoftCard
                                )
                            )
                            OutlinedTextField(
                                value = tempYear,
                                onValueChange = { tempYear = it },
                                label = { Text("السنة الدراسية", color = SoftGrey) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentBlue,
                                    unfocusedBorderColor = SoftCard
                                )
                            )
                        }

                        // Semester selector
                        Text("الفصل الدراسي", color = SoftGrey, fontSize = 12.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("1" to "الفصل الأول", "2" to "الفصل الثاني", "3" to "الفصل الثالث").forEach { (valStr, label) ->
                                val selected = tempSemester == valStr
                                Button(
                                    onClick = { tempSemester = valStr },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selected) AccentBlue else SoftCard,
                                        contentColor = if (selected) Color.Black else Color.White
                                    ),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Max Score configuration slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("العلامة الكاملة للمقرر", color = SoftGrey, fontSize = 12.sp)
                                Slider(
                                    value = tempMax,
                                    onValueChange = { tempMax = it.toInt().toFloat() },
                                    valueRange = 5f..40f,
                                    steps = 35,
                                    colors = SliderDefaults.colors(
                                        thumbColor = AccentBlue,
                                        activeTrackColor = AccentBlue,
                                        inactiveTrackColor = SoftCard
                                    )
                                )
                            }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SoftCard),
                                modifier = Modifier.size(54.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        tempMax.toInt().toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = AccentBlue
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.saveSettings(
                                    tempTeacher,
                                    tempSubject,
                                    tempInstitution,
                                    tempYear,
                                    tempSemester,
                                    tempMax
                                )
                                showSettings = false
                                Toast.makeText(context, "تم حفظ الإعدادات بنجاح!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue, contentColor = Color.Black),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_settings_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("حفظ الإعدادات", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Adaptive layout depending on Screen Width (Responsive design check)
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isExpanded = maxWidth > 650.dp

                if (isExpanded) {
                    // Split content side-by-side for Tablet & Landscape experiences
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1.2f)
                                .fillMaxHeight()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = TabBg),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            ActiveTabScreen(activeTab, viewModel)
                        }

                        Card(
                            modifier = Modifier
                                .weight(0.8f)
                                .fillMaxHeight()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = TabBg),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "الطلاب المسجلون حالياً",
                                    fontWeight = FontWeight.Bold,
                                    color = AccentBlue,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                StudentListContent(studentsList, viewModel)
                            }
                        }
                    }
                } else {
                    // Standard Handheld view
                    Box(modifier = Modifier.fillMaxSize()) {
                        ActiveTabScreen(activeTab, viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveTabScreen(tab: ActiveTab, viewModel: MainViewModel) {
    when (tab) {
        ActiveTab.CALCULATOR -> CalculatorScreen(viewModel)
        ActiveTab.STUDENTS -> StudentsScreen(viewModel)
        ActiveTab.STATS -> StatsScreen(viewModel)
        ActiveTab.DATA -> DataControlScreen(viewModel)
    }
}

@Composable
fun CalculatorScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentSum by viewModel.currentSum.collectAsStateWithLifecycle()
    val operations by viewModel.operations.collectAsStateWithLifecycle()
    val lastAdded by viewModel.lastAdded.collectAsStateWithLifecycle()
    val maxScore by viewModel.maxScore.collectAsStateWithLifecycle()

    var showSaveDialog by remember { mutableStateOf(false) }
    var saveStudentName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Main Display Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftCard),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("المجموع", fontSize = 16.sp, color = SoftGrey, fontWeight = FontWeight.Bold)
                    Text(
                        String.format(Locale.US, "%.2f", currentSum),
                        fontSize = 38.sp,
                        color = AccentBlue,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Horizontal Progress Bar
                val progress = if (maxScore > 0f) (currentSum / maxScore).toFloat() else 0f
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .background(color = TabBg, shape = RoundedCornerShape(5.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(color = AccentBlue, shape = RoundedCornerShape(5.dp))
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "آخر نقطة: " + (if (lastAdded != null) String.format(Locale.US, "+%.2f", lastAdded) else "--"),
                        color = SoftGrey,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "العمليات: ${operations.size}",
                        color = SoftGrey,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Tapping grid 4x5
        val points = listOf(
            0.25, 0.50, 0.75, 1.00,
            1.25, 1.50, 1.75, 2.00,
            2.25, 2.50, 2.75, 3.00,
            3.25, 3.50, 4.00, 4.50,
            5.00, 6.00, 7.00, 8.00
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (row in 0 until 5) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (col in 0 until 4) {
                        val index = row * 4 + col
                        val valDouble = points[index]
                        Button(
                            onClick = { viewModel.addValue(valDouble) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SoftCard,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .testTag("btn_${valDouble.toString().replace(".", "_")}"),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = String.format(Locale.US, "+%.2f", valDouble),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Under button action panel (Undo, Reset, Save)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.undo() },
                colors = ButtonDefaults.buttonColors(containerColor = UndoGrey, contentColor = Color.White),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("undo_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "تراجع", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("تراجع", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Button(
                onClick = { viewModel.resetCalculator() },
                colors = ButtonDefaults.buttonColors(containerColor = ResetRed, contentColor = YellowLabel),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("reset_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "تصفير", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("تصفير", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Button(
                onClick = {
                    if (currentSum <= 0.0) {
                        Toast.makeText(context, "الرجاء احتساب النقطة أولاً بالتأشير!", Toast.LENGTH_SHORT).show()
                    } else {
                        saveStudentName = ""
                        showSaveDialog = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1), contentColor = AccentBlue),
                modifier = Modifier
                    .weight(1.2f)
                    .height(48.dp)
                    .testTag("save_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = "حفظ", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("حفظ العلامة", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }

    // Modal Save Student Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text(
                    "حفظ نتيجة الطالب",
                    fontWeight = FontWeight.Bold,
                    color = AccentBlue
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "النقطة النهائية المستحقة: ${String.format(Locale.US, "%.2f", currentSum)} / ${maxScore.toInt()}",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    OutlinedTextField(
                        value = saveStudentName,
                        onValueChange = { saveStudentName = it },
                        label = { Text("اسم الطالب الكامل") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("student_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = SoftCard
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveStudent(saveStudentName)
                        showSaveDialog = false
                        Toast.makeText(context, "تم حفظ علامة الطالب بنجاح!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue, contentColor = Color.Black),
                    modifier = Modifier.testTag("dialog_confirm_save")
                ) {
                    Text("تأكيد الحفظ", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSaveDialog = false },
                    modifier = Modifier.testTag("dialog_cancel_save")
                ) {
                    Text("إلغاء", color = SoftGrey)
                }
            },
            containerColor = TabBg
        )
    }
}

enum class SortMode {
    DATE, SCORE, NAME
}

@Composable
fun StudentsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val studentsList by viewModel.students.collectAsStateWithLifecycle()
    val teacherName by viewModel.teacherName.collectAsStateWithLifecycle()
    val subject by viewModel.subject.collectAsStateWithLifecycle()
    val institution by viewModel.institution.collectAsStateWithLifecycle()
    val academicYear by viewModel.academicYear.collectAsStateWithLifecycle()
    val semester by viewModel.semester.collectAsStateWithLifecycle()
    val maxScore by viewModel.maxScore.collectAsStateWithLifecycle()

    var sortBy by remember { mutableStateOf(SortMode.DATE) }

    val sortedStudents = remember(studentsList, sortBy) {
        when (sortBy) {
            SortMode.DATE -> studentsList.sortedByDescending { it.timestamp }
            SortMode.SCORE -> studentsList.sortedByDescending { it.score }
            SortMode.NAME -> studentsList.sortedBy { it.name.lowercase() }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Sorter row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                SortMode.DATE to "📅 التاريخ",
                SortMode.SCORE to "📊 النقطة",
                SortMode.NAME to "🔤 الاسم"
            ).forEach { (mode, label) ->
                val active = sortBy == mode
                Button(
                    onClick = { sortBy = mode },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (active) AccentBlue else SoftCard,
                        contentColor = if (active) Color.Black else Color.White
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Student Card List Content
        Box(modifier = Modifier.weight(1f)) {
            StudentListContent(sortedStudents, viewModel)
        }

        // Footer Export buttons
        Button(
            onClick = {
                PdfExporter.exportToPdf(
                    context = context,
                    students = sortedStudents,
                    teacherName = teacherName.ifEmpty { "المصحح" },
                    subject = subject.ifEmpty { "غير محددة" },
                    institution = institution.ifEmpty { "مؤسسة تعليمية" },
                    academicYear = academicYear,
                    semester = semester,
                    maxScore = maxScore.toDouble(),
                    reportType = "students"
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = TabBg, contentColor = AccentBlue),
            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(AccentBlue)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("export_pdf_button"),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF Report")
            Spacer(modifier = Modifier.width(6.dp))
            Text("تصدير كشف النقاط (PDF)", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StudentListContent(students: List<Student>, viewModel: MainViewModel) {
    if (students.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لا توجد بيانات مسجلة للطلاب حتى الآن.", color = SoftGrey, textAlign = TextAlign.Center)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(students, key = { it.id }) { student ->
                var showDeleteConfirm by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftCard),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp, 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                student.name,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                            val simpleDate = remember(student.timestamp) {
                                SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(student.timestamp))
                            }
                            Text(
                                "تاريخ الرصد: $simpleDate",
                                color = SoftGrey,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                String.format(Locale.US, "%.2f", student.score) + " / " + student.maxScore.toInt(),
                                fontWeight = FontWeight.Bold,
                                color = AccentBlue,
                                fontSize = 16.sp
                            )
                            IconButton(onClick = { showDeleteConfirm = true }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "حذف علامة الطالب",
                                    tint = DangerRed
                                )
                            }
                        }
                    }
                }

                // Delete Student Confirmation Dialog
                if (showDeleteConfirm) {
                    AlertDialog(
                        onDismissRequest = { showDeleteConfirm = false },
                        title = { Text("حذف علامة الطالب", fontWeight = FontWeight.Bold, color = DangerRed) },
                        text = { Text("هل أنت متأكد من رغبتك في حذف علامة الطالب '${student.name}' نهائياً؟", color = Color.White) },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.deleteStudent(student)
                                    showDeleteConfirm = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = Color.White)
                            ) {
                                Text("حذف", fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteConfirm = false }) {
                                Text("إلغاء", color = SoftGrey)
                            }
                        },
                        containerColor = TabBg
                    )
                }
            }
        }
    }
}

@Composable
fun StatsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val studentsList by viewModel.students.collectAsStateWithLifecycle()
    val teacherName by viewModel.teacherName.collectAsStateWithLifecycle()
    val subject by viewModel.subject.collectAsStateWithLifecycle()
    val institution by viewModel.institution.collectAsStateWithLifecycle()
    val academicYear by viewModel.academicYear.collectAsStateWithLifecycle()
    val semester by viewModel.semester.collectAsStateWithLifecycle()
    val maxScore by viewModel.maxScore.collectAsStateWithLifecycle()

    val totalCount = studentsList.size
    val passedCount = studentsList.count { it.score >= (it.maxScore / 2) }
    val passRatePercentage = if (totalCount > 0) (passedCount.toDouble() / totalCount * 100) else 0.0

    // Normalized stats safely
    val normalizedHighest = if (totalCount > 0) studentsList.maxOf { it.score } else 0.0
    val normalizedLowest = if (totalCount > 0) studentsList.minOf { it.score } else 0.0
    val averageScore = if (totalCount > 0) studentsList.map { it.score / it.maxScore }.average() * maxScore else 0.0

    // Tally up categories
    val excellent = studentsList.count { it.score >= it.maxScore * 0.85 }
    val veryGood = studentsList.count { it.score >= it.maxScore * 0.70 && it.score < it.maxScore * 0.85 }
    val good = studentsList.count { it.score >= it.maxScore * 0.50 && it.score < it.maxScore * 0.70 }
    val acceptable = studentsList.count { it.score >= it.maxScore * 0.40 && it.score < it.maxScore * 0.50 }
    val weak = studentsList.count { it.score < it.maxScore * 0.40 }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Grid level results cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatBox(
                        title = "عدد المسجلين",
                        value = "$totalCount طالب",
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        title = "معدل المجموعة",
                        value = String.format(Locale.US, "%.2f/%.1f", averageScore, maxScore),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatBox(
                        title = "أعلى نقطة",
                        value = String.format(Locale.US, "%.2f", normalizedHighest),
                        modifier = Modifier.weight(1.0f)
                    )
                    StatBox(
                        title = "أدنى نقطة",
                        value = String.format(Locale.US, "%.2f", normalizedLowest),
                        modifier = Modifier.weight(1.0f)
                    )
                    StatBox(
                        title = "نسبة النجاح",
                        value = String.format(Locale.US, "%.1f%%", passRatePercentage),
                        modifier = Modifier.weight(1.2f),
                        valueColor = SuccessGreen
                    )
                }
            }
        }

        // Draw visual Canvas distribution chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftCard)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "مخطط توزيع تقديرات الطلبة",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentBlue
                    )

                    DistributionBarChart(
                        excellent = excellent,
                        veryGood = veryGood,
                        good = good,
                        acceptable = acceptable,
                        weak = weak
                    )
                }
            }
        }

        // Range Stats tabular configuration
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftCard)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "التفاصيل وجرد الفئات المستحقة",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentBlue
                    )

                    val rows = listOf(
                        Triple("ممتاز (A)", excellent, AccentBlue),
                        Triple("جيد جداً (B)", veryGood, SuccessGreen),
                        Triple("جيد (C)", good, YellowLabel),
                        Triple("مقبول (D)", acceptable, WarningOrange),
                        Triple("ضعيف (F)", weak, DangerRed)
                    )

                    rows.forEach { (catLabel, catCount, catColor) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .background(catColor, RoundedCornerShape(3.dp))
                                )
                                Text(catLabel, color = Color.White, fontSize = 12.sp)
                            }
                            Text(
                                "$catCount طلبة",
                                color = AccentBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Save Stats report
        item {
            Button(
                onClick = {
                    PdfExporter.exportToPdf(
                        context = context,
                        students = studentsList,
                        teacherName = teacherName.ifEmpty { "المصحح" },
                        subject = subject.ifEmpty { "غير محددة" },
                        institution = institution.ifEmpty { "مؤسسة تعليمية" },
                        academicYear = academicYear,
                        semester = semester,
                        maxScore = maxScore.toDouble(),
                        reportType = "stats"
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = TabBg, contentColor = SuccessGreen),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(SuccessGreen)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("export_stats_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Analytics, contentDescription = "PDF Statistics")
                Spacer(modifier = Modifier.width(6.dp))
                Text("تصدير تقرير الإحصائيات (PDF)", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StatBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = AccentBlue
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SoftCard),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = valueColor, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, fontSize = 10.sp, color = SoftGrey, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun DistributionBarChart(
    excellent: Int,
    veryGood: Int,
    good: Int,
    acceptable: Int,
    weak: Int
) {
    val maxCount = maxOf(1, excellent, veryGood, good, acceptable, weak)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        val width = size.width
        val height = size.height

        val categories = listOf(
            Triple(excellent, "ممتاز", AccentBlue),
            Triple(veryGood, "جيد جداً", SuccessGreen),
            Triple(good, "جيد", YellowLabel),
            Triple(acceptable, "مقبول", WarningOrange),
            Triple(weak, "ضعيف", DangerRed)
        )

        val columnWidth = width / 5
        val barWidth = columnWidth * 0.55f

        categories.forEachIndexed { idx, (count, label, color) ->
            val fractionHeight = count.toFloat() / maxCount.toFloat()
            val barHeight = topBarHeightScaling(height, fractionHeight)

            val xPos = idx * columnWidth + (columnWidth - barWidth) / 2
            val yPos = height - barHeight - 20f

            // Draw Bar Rect
            drawRoundRect(
                color = color,
                topLeft = Offset(xPos, yPos),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )

            // Draw texts (Count and Category key)
            drawIntoCanvas { canvas ->
                val textPaint = android.graphics.Paint().apply {
                    this.color = android.graphics.Color.WHITE
                    this.textSize = 10.sp.toPx()
                    this.textAlign = android.graphics.Paint.Align.CENTER
                }

                val countPaint = android.graphics.Paint().apply {
                    this.color = android.graphics.Color.parseColor("#64B5F6")
                    this.textSize = 10.sp.toPx()
                    this.typeface = android.graphics.Typeface.DEFAULT_BOLD
                    this.textAlign = android.graphics.Paint.Align.CENTER
                }

                // Draw Count Text
                canvas.nativeCanvas.drawText(
                    count.toString(),
                    xPos + barWidth / 2,
                    yPos - 8f,
                    countPaint
                )

                // Draw Category short bottom labels
                canvas.nativeCanvas.drawText(
                    label,
                    xPos + barWidth / 2,
                    height + 2f,
                    textPaint
                )
            }
        }
    }
}

private fun topBarHeightScaling(canvasHeight: Float, fractionHeight: Float): Float {
    // Leave some margin space out of canvas height
    val maxPossibleHeight = canvasHeight - 35f
    return maxOf(10f, maxPossibleHeight * fractionHeight)
}

@Composable
fun DataControlScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val studentsList by viewModel.students.collectAsStateWithLifecycle()
    val totalCount = studentsList.size

    var confirmDeleteAllDialog by remember { mutableStateOf(false) }
    var confirmNewSemesterDialog by remember { mutableStateOf(false) }
    var confirmNewYearDialog by remember { mutableStateOf(false) }

    // Activity Contract to pick external backup JSON files
    val fileImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    val jsonString = stream.reader().readText()
                    val success = viewModel.importBackupJson(jsonString)
                    if (success) {
                        Toast.makeText(context, "تمت استعادة البيانات بنجاح بنسبة 100%!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "الملف غير صالح أو فارغ!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "عذراً، فشل استقدام ملف الباكاب!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats Status
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftCard)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("إجمالي العناصر في النظام", fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    text = "$totalCount صفاً",
                    color = AccentBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        // Danger Panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = TabBg),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, WarningOrange)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "⚠️ منطقة الخطر والتهيئة الإدارية",
                    style = MaterialTheme.typography.titleMedium,
                    color = WarningOrange,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { confirmNewSemesterDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftCard, contentColor = WarningOrange),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("new_semester_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("فصل دراسي جديد", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { confirmNewYearDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftCard, contentColor = WarningOrange),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("new_year_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("سنة دراسية جديدة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = { confirmDeleteAllDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ResetRed, contentColor = YellowLabel),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("delete_all_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = "ลบ")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("حذف جميع البيانات بالكامل", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Import & Export Configuration Panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftCard)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "💾 تصدير واستيراد النسخ الاحتياطية",
                    style = MaterialTheme.typography.titleMedium,
                    color = AccentBlue,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "يمكنك مشاركة وحفظ قواعد بيانات طلابك لتجنب الضياع العرضي أو لمصادمة النتائج.",
                    fontSize = 11.sp,
                    color = SoftGrey
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // JSON Export Backups
                    Button(
                        onClick = {
                            val backupString = viewModel.exportBackupJson()
                            try {
                                val file = File(context.cacheDir, "exam_corrector_backup.json")
                                file.writeText(backupString)
                                val uri = androidx.core.content.FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/json"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "حفظ نسخة احتياطية عبر:"))
                            } catch (e: Exception) {
                                Toast.makeText(context, "فشل تصدير النسخة الاحتياطية", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TabBg, contentColor = AccentBlue),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(AccentBlue)),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("backup_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Backup")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("نسخ احتياطي", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // JSON Import Recoveries
                    Button(
                        onClick = { fileImportLauncher.launch("application/json") },
                        colors = ButtonDefaults.buttonColors(containerColor = TabBg, contentColor = SuccessGreen),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(SuccessGreen)),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("restore_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = "Restore")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("استعادة نسخة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // New Semester Confirmation Dialog
    if (confirmNewSemesterDialog) {
        AlertDialog(
            onDismissRequest = { confirmNewSemesterDialog = false },
            title = { Text("هل تريد تصفير الفصل الدراسي؟", fontWeight = FontWeight.Bold, color = WarningOrange) },
            text = { Text("سيتم حذف كشف علامات الطلاب للفصل والأكاديمية المحددين حالياً فقط.", color = Color.White) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearSemester()
                        confirmNewSemesterDialog = false
                        Toast.makeText(context, "تمت تهيئة الفصل بنجاح!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarningOrange, contentColor = Color.Black)
                ) {
                    Text("تهيئة وتصفير", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmNewSemesterDialog = false }) {
                    Text("إلغاء", color = SoftGrey)
                }
            },
            containerColor = TabBg
        )
    }

    // New Academic Year Confirmation Dialog
    if (confirmNewYearDialog) {
        AlertDialog(
            onDismissRequest = { confirmNewYearDialog = false },
            title = { Text("تهيئة السنة الدراسية؟", fontWeight = FontWeight.Bold, color = WarningOrange) },
            text = { Text("أنت على وشك حذف كافة سجلات الطلاب التابعين للسنة الدراسية الحالية.", color = Color.White) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearYear()
                        confirmNewYearDialog = false
                        Toast.makeText(context, "تمت تهيئة السنة الدراسية!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarningOrange, contentColor = Color.Black)
                ) {
                    Text("مسح السنة الدراسية", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmNewYearDialog = false }) {
                    Text("إلغاء", color = SoftGrey)
                }
            },
            containerColor = TabBg
        )
    }

    // Delete All Confirmation Dialog
    if (confirmDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { confirmDeleteAllDialog = false },
            title = { Text("تحذير أخير نهائي ⚠️", fontWeight = FontWeight.Bold, color = DangerRed) },
            text = { Text("هل تريد حذف كافة البيانات في التطبيق؟ هذا الإجراء غير قابل للتراجع.", color = Color.White) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        confirmDeleteAllDialog = false
                        Toast.makeText(context, "تم مسح جميع البيانات بنجاح!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = Color.White)
                ) {
                    Text("تأكيد تصفير الكل", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteAllDialog = false }) {
                    Text("إلغاء", color = SoftGrey)
                }
            },
            containerColor = TabBg
        )
    }
}
