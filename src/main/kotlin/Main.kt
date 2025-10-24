//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.text.BasicTextField
//import androidx.compose.material3.* // ✅ Use only Material 3
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.window.Window
//import androidx.compose.ui.window.application
//import kotlinx.serialization.*
//import kotlinx.serialization.json.*
//import java.io.File
//
//@Serializable
//data class Task(
//    val id: Int,
//    val title: String,
//    val completed: Boolean = false
//)
//
//enum class FilterType { All, Active, Completed }
//
//fun main() = application {
//    Window(onCloseRequest = ::exitApplication, title = "TaskMaster Pro") {
//        MaterialTheme(colorScheme = lightColorScheme()) { // ✅ Material 3 version
//            TaskManagerApp()
//        }
//    }
//}
//
//@Composable
//fun TaskManagerApp() {
//    var tasks by remember { mutableStateOf(loadTasks()) }
//    var newTaskText by remember { mutableStateOf("") }
//    var filter by remember { mutableStateOf(FilterType.All) }
//
//    val filteredTasks = when (filter) {
//        FilterType.All -> tasks
//        FilterType.Active -> tasks.filter { !it.completed }
//        FilterType.Completed -> tasks.filter { it.completed }
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(20.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text("TaskMaster Pro", style = MaterialTheme.typography.headlineMedium)
//        Spacer(Modifier.height(16.dp))
//
//        Row(verticalAlignment = Alignment.CenterVertically) {
//            BasicTextField(
//                value = newTaskText,
//                onValueChange = { newTaskText = it },
//                modifier = Modifier
//                    .weight(1f)
//                    .background(Color.LightGray, shape = MaterialTheme.shapes.small)
//                    .padding(8.dp)
//            )
//            Spacer(Modifier.width(8.dp))
//            Button(onClick = {
//                if (newTaskText.isNotBlank()) {
//                    tasks = tasks + Task(
//                        id = tasks.size + 1,
//                        title = newTaskText.trim()
//                    )
//                    newTaskText = ""
//                    saveTasks(tasks)
//                }
//            }) {
//                Text("Add")
//            }
//        }
//
//        Spacer(Modifier.height(16.dp))
//
//        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//            FilterButton("All", filter == FilterType.All) { filter = FilterType.All }
//            FilterButton("Active", filter == FilterType.Active) { filter = FilterType.Active }
//            FilterButton("Completed", filter == FilterType.Completed) { filter = FilterType.Completed }
//        }
//
//        Spacer(Modifier.height(16.dp))
//
//        LazyColumn {
//            items(filteredTasks) { task ->
//                TaskItem(
//                    task = task,
//                    onToggle = {
//                        tasks = tasks.map {
//                            if (it.id == task.id) it.copy(completed = !it.completed) else it
//                        }
//                        saveTasks(tasks)
//                    },
//                    onDelete = {
//                        tasks = tasks.filterNot { it.id == task.id }
//                        saveTasks(tasks)
//                    }
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun FilterButton(text: String, selected: Boolean, onClick: () -> Unit) {
//    Button(
//        onClick = onClick,
//        colors = ButtonDefaults.buttonColors(
//            containerColor = if (selected) Color(0xFF2196F3) else Color.LightGray
//        )
//    ) {
//        Text(text, color = Color.White)
//    }
//}
//
//@Composable
//fun TaskItem(task: Task, onToggle: () -> Unit, onDelete: () -> Unit) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 4.dp)
//            .background(if (task.completed) Color(0xFFB2DFDB) else Color(0xFFE0F7FA))
//            .padding(8.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Checkbox(
//            checked = task.completed,
//            onCheckedChange = { onToggle() }
//        )
//        Text(
//            task.title,
//            modifier = Modifier.weight(1f)
//        )
//        Button(
//            onClick = onDelete,
//            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
//        ) {
//            Text("Delete", color = Color.White)
//        }
//    }
//}
//
//private val tasksFile = File("tasks.json")
//
//private fun saveTasks(tasks: List<Task>) {
//    tasksFile.writeText(Json.encodeToString(tasks))
//}
//
//private fun loadTasks(): List<Task> {
//    return if (tasksFile.exists()) {
//        try {
//            Json.decodeFromString(tasksFile.readText())
//        } catch (e: Exception) {
//            emptyList()
//        }
//    } else emptyList()
//}

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.launch
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Enhanced data model with timestamp and priority
@Serializable
data class Task(
    val id: String,
    val title: String,
    val completed: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

@Serializable
enum class Priority { LOW, MEDIUM, HIGH }

enum class FilterType { ALL, ACTIVE, COMPLETED }
enum class SortType { CREATED, PRIORITY, ALPHABETICAL }

// Custom serializer for LocalDateTime
object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
    }
    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.parse(decoder.decodeString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }
}

// Repository pattern for better separation of concerns
class TaskRepository(private val file: File = File("tasks.json")) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    suspend fun saveTasks(tasks: List<Task>): Result<Unit> = runCatching {
        file.writeText(json.encodeToString(tasks))
    }

    suspend fun loadTasks(): Result<List<Task>> = runCatching {
        if (file.exists()) {
            json.decodeFromString<List<Task>>(file.readText())
        } else {
            emptyList()
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "TaskMaster Pro"
    ) {
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = Color(0xFF6200EE),
                secondary = Color(0xFF03DAC6),
                background = Color(0xFFF5F5F5)
            )
        ) {
            TaskManagerApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskManagerApp() {
    val repository = remember { TaskRepository() }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var newTaskText by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(FilterType.ALL) }
    var sortType by remember { mutableStateOf(SortType.CREATED) }
    var newTaskPriority by remember { mutableStateOf(Priority.MEDIUM) }

    // Load tasks on startup
    LaunchedEffect(Unit) {
        repository.loadTasks().fold(
            onSuccess = {
                tasks = it
                isLoading = false
            },
            onFailure = {
                snackbarHostState.showSnackbar("Error loading tasks: ${it.message}")
                isLoading = false
            }
        )
    }

    // Filter and sort tasks
    val filteredAndSortedTasks = remember(tasks, filter, sortType) {
        val filtered = when (filter) {
            FilterType.ALL -> tasks
            FilterType.ACTIVE -> tasks.filter { !it.completed }
            FilterType.COMPLETED -> tasks.filter { it.completed }
        }

        when (sortType) {
            SortType.CREATED -> filtered.sortedByDescending { it.createdAt }
            SortType.PRIORITY -> filtered.sortedByDescending { it.priority.ordinal }
            SortType.ALPHABETICAL -> filtered.sortedBy { it.title.lowercase() }
        }
    }

    val stats = remember(tasks) {
        TaskStats(
            total = tasks.size,
            completed = tasks.count { it.completed },
            active = tasks.count { !it.completed }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with stats
            TaskHeader(stats)

            Spacer(Modifier.height(24.dp))

            // Add task section
            AddTaskSection(
                taskText = newTaskText,
                priority = newTaskPriority,
                onTaskTextChange = { newTaskText = it },
                onPriorityChange = { newTaskPriority = it },
                onAddTask = {
                    if (newTaskText.isNotBlank()) {
                        val newTask = Task(
                            id = java.util.UUID.randomUUID().toString(),
                            title = newTaskText.trim(),
                            priority = newTaskPriority
                        )
                        tasks = tasks + newTask
                        newTaskText = ""
                        newTaskPriority = Priority.MEDIUM

                        scope.launch {
                            repository.saveTasks(tasks).onFailure {
                                snackbarHostState.showSnackbar("Error saving task")
                            }
                        }
                    }
                }
            )

            Spacer(Modifier.height(24.dp))

            // Filters and sorting
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FilterChips(
                    currentFilter = filter,
                    onFilterChange = { filter = it }
                )

                SortDropdown(
                    currentSort = sortType,
                    onSortChange = { sortType = it }
                )
            }

            Spacer(Modifier.height(16.dp))

            // Task list
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(32.dp))
            } else if (filteredAndSortedTasks.isEmpty()) {
                EmptyState(filter)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = filteredAndSortedTasks,
                        key = { it.id }
                    ) { task ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            TaskItem(
                                task = task,
                                onToggle = {
                                    tasks = tasks.map {
                                        if (it.id == task.id) it.copy(completed = !it.completed) else it
                                    }
                                    scope.launch {
                                        repository.saveTasks(tasks)
                                    }
                                },
                                onDelete = {
                                    tasks = tasks.filterNot { it.id == task.id }
                                    scope.launch {
                                        repository.saveTasks(tasks).fold(
                                            onSuccess = {
                                                snackbarHostState.showSnackbar("Task deleted")
                                            },
                                            onFailure = {
                                                snackbarHostState.showSnackbar("Error deleting task")
                                            }
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

data class TaskStats(val total: Int, val completed: Int, val active: Int)

@Composable
fun TaskHeader(stats: TaskStats) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "TaskMaster Pro",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatChip("Total: ${stats.total}", Color(0xFF6200EE))
            StatChip("Active: ${stats.active}", Color(0xFFFF9800))
            StatChip("Done: ${stats.completed}", Color(0xFF4CAF50))
        }
    }
}

@Composable
fun StatChip(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = color,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun AddTaskSection(
    taskText: String,
    priority: Priority,
    onTaskTextChange: (String) -> Unit,
    onPriorityChange: (Priority) -> Unit,
    onAddTask: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = taskText,
                    onValueChange = onTaskTextChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Enter a new task...") },
                    singleLine = true
                )

                IconButton(
                    onClick = onAddTask,
                    enabled = taskText.isNotBlank()
                ) {
                    Icon(Icons.Default.Add, "Add task")
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Priority:", modifier = Modifier.align(Alignment.CenterVertically))
                Priority.values().forEach { p ->
                    FilterChip(
                        selected = priority == p,
                        onClick = { onPriorityChange(p) },
                        label = { Text(p.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = priorityColor(p)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChips(currentFilter: FilterType, onFilterChange: (FilterType) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterType.values().forEach { filter ->
            FilterChip(
                selected = currentFilter == filter,
                onClick = { onFilterChange(filter) },
                label = { Text(filter.name.lowercase().capitalize()) }
            )
        }
    }
}

@Composable
fun SortDropdown(currentSort: SortType, onSortChange: (SortType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        FilterChip(
            selected = false,
            onClick = { expanded = true },
            label = { Text("Sort: ${currentSort.name.lowercase().capitalize()}") }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SortType.values().forEach { sort ->
                DropdownMenuItem(
                    text = { Text(sort.name.lowercase().capitalize()) },
                    onClick = {
                        onSortChange(sort)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun EmptyState(filter: FilterType) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = when (filter) {
                FilterType.ALL -> "No tasks yet. Add one to get started!"
                FilterType.ACTIVE -> "No active tasks. Great job!"
                FilterType.COMPLETED -> "No completed tasks yet."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
    }
}

@Composable
fun TaskItem(task: Task, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.completed)
                Color(0xFFE8F5E9)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Checkbox(
                checked = task.completed,
                onCheckedChange = { onToggle() }
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else null,
                    color = if (task.completed) Color.Gray else Color.Black
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = priorityColor(task.priority).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            task.priority.name,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = priorityColor(task.priority)
                        )
                    }

                    Text(
                        task.createdAt.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
            }
        }
    }
}

fun priorityColor(priority: Priority) = when (priority) {
    Priority.LOW -> Color(0xFF4CAF50)
    Priority.MEDIUM -> Color(0xFFFF9800)
    Priority.HIGH -> Color(0xFFF44336)
}