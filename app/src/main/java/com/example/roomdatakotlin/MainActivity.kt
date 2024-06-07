package com.example.roomdatakotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.room.Room
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import coil.compose.rememberAsyncImagePainter
import com.example.roomdatakotlin.ui.theme.RoomDataKotlinTheme


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RoomDataKotlinTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    StudentScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}



@Composable
fun StudentScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val db = Room.databaseBuilder(
        context,
        StudentDB::class.java, "student-db"
    ).allowMainThreadQueries().build()

    var listStudents by remember { mutableStateOf(db.studentDAO().getAll()) }
    var editingStudent by remember { mutableStateOf<StudentModel?>(null) }
    var showingAddStudentDialog by remember { mutableStateOf(false) }
    var showingStudentDetail by remember { mutableStateOf<StudentModel?>(null) }

    Column(Modifier.fillMaxWidth()) {
        Text(
            text = "Quản lý Sinh viên",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        Button(
            onClick = { showingAddStudentDialog = true },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Thêm SV")
        }

        LazyColumn {
            items(listStudents) { student ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable { showingStudentDetail = student }
                ) {
                    Text(modifier = Modifier.weight(1f), text = student.uid.toString())
                    Text(modifier = Modifier.weight(1f), text = student.hoten.toString())
                    Text(modifier = Modifier.weight(1f), text = student.mssv.toString())
                    Text(modifier = Modifier.weight(1f), text = student.diemTB.toString())
                    Text(modifier = Modifier.weight(1f), text = student.daratruong.toString())

                    Button(onClick = { editingStudent = student }) {
                        Text(text = "Sửa")
                    }
                    Button(onClick = {
                        db.studentDAO().delete(student)
                        listStudents = db.studentDAO().getAll()
                    }) {
                        Text(text = "Xóa")
                    }
                }
                Divider()
            }
        }

        if (showingAddStudentDialog) {
            AddStudentDialog(
                onDismiss = { showingAddStudentDialog = false },
                onSave = { newStudent ->
                    db.studentDAO().insert(newStudent)
                    listStudents = db.studentDAO().getAll()
                    showingAddStudentDialog = false
                }
            )
        }

        editingStudent?.let { student ->
            EditStudentDialog(
                student = student,
                onDismiss = { editingStudent = null },
                onSave = { updatedStudent ->
                    db.studentDAO().update(updatedStudent)
                    listStudents = db.studentDAO().getAll()
                    editingStudent = null
                }
            )
        }

        showingStudentDetail?.let { student ->
            StudentDetailScreen(
                student = student,
                onDismiss = { showingStudentDetail = null }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentDialog(
    onDismiss: () -> Unit,
    onSave: (StudentModel) -> Unit
) {
    var hoten by remember { mutableStateOf("") }
    var mssv by remember { mutableStateOf("") }
    var diemTB by remember { mutableStateOf("") }
    var daratruong by remember { mutableStateOf(false) }
    var photoPath by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Thêm Sinh viên") },
        text = {
            Column {
                TextField(
                    value = hoten,
                    onValueChange = { hoten = it },
                    label = { Text("Họ tên") }
                )
                TextField(
                    value = mssv,
                    onValueChange = { mssv = it },
                    label = { Text("MSSV") }
                )
                TextField(
                    value = diemTB,
                    onValueChange = { diemTB = it },
                    label = { Text("Điểm TB") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = daratruong,
                        onCheckedChange = { daratruong = it }
                    )
                    Text(text = "Đã ra trường")
                }
                TextField(
                    value = photoPath,
                    onValueChange = { photoPath = it },
                    label = { Text("Đường dẫn ảnh") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val newStudent = StudentModel(
                    hoten = hoten,
                    mssv = mssv,
                    diemTB = diemTB.toFloatOrNull() ?: 0f,
                    daratruong = daratruong,
                    photoPath = photoPath
                )
                onSave(newStudent)
            }) {
                Text("Lưu")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditStudentDialog(
    student: StudentModel,
    onDismiss: () -> Unit,
    onSave: (StudentModel) -> Unit
) {
    var hoten by remember { mutableStateOf(student.hoten ?: "") }
    var mssv by remember { mutableStateOf(student.mssv ?: "") }
    var diemTB by remember { mutableStateOf(student.diemTB?.toString() ?: "0") }
    var daratruong by remember { mutableStateOf(student.daratruong ?: false) }
    var photoPath by remember { mutableStateOf(student.photoPath ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Sửa Sinh viên") },
        text = {
            Column {
                TextField(
                    value = hoten,
                    onValueChange = { hoten = it },
                    label = { Text("Họ tên") }
                )
                TextField(
                    value = mssv,
                    onValueChange = { mssv = it },
                    label = { Text("MSSV") }
                )
                TextField(
                    value = diemTB,
                    onValueChange = { diemTB = it },
                    label = { Text("Điểm TB") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = daratruong,
                        onCheckedChange = { daratruong = it }
                    )
                    Text(text = "Đã ra trường")
                }
                TextField(
                    value = photoPath,
                    onValueChange = { photoPath = it },
                    label = { Text("Đường dẫn ảnh") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val updatedStudent = student.copy(
                    hoten = hoten,
                    mssv = mssv,
                    diemTB = diemTB.toFloatOrNull() ?: 0f,
                    daratruong = daratruong,
                    photoPath = photoPath
                )
                onSave(updatedStudent)
            }) {
                Text("Lưu")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}



@Composable
fun StudentDetailScreen(student: StudentModel, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Chi tiết Sinh viên") },
        text = {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Họ tên: ${student.hoten}")
                Text(text = "MSSV: ${student.mssv}")
                Text(text = "Điểm TB: ${student.diemTB}")
                Text(text = "Đã ra trường: ${if (student.daratruong == true) "Có" else "Không"}")
                student.photoPath?.let {
                    if (it.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(it),
                            contentDescription = null,
                            modifier = Modifier.size(100.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}