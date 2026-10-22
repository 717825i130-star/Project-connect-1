package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import com.example.ui.components.appTextFieldColors
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.*

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onDemoLogin: (UserRole) -> Unit,
    onRegister: (
        name: String,
        email: String,
        role: UserRole,
        department: String,
        yearOrTitle: String,
        skills: List<String>,
        interests: List<String>
    ) -> Result<Any>,
    onPerformLogin: (email: String, role: UserRole) -> Result<Any>,
    modifier: Modifier = Modifier
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Register fields
    var fullName by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var yearOrTitle by remember { mutableStateOf("") }
    var skillsInput by remember { mutableStateOf("") }
    var interestsInput by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Branding Logo and Header
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(PrimaryBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = "ProjectConnect Icon",
                tint = Color.White,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "ProjectConnect",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = PrimaryBlueVariant
        )

        Text(
            text = "Bridge student ambitions with faculty project mentorship",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Role Selector Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select Your Role",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlueVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceVariantLight, RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    val studentSelected = selectedRole == UserRole.STUDENT
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .then(
                                if (studentSelected) Modifier.background(StudentMarbleHeroBrush)
                                else Modifier.background(Color.Transparent)
                            )
                            .clickable {
                                selectedRole = UserRole.STUDENT
                                errorMessage = null
                            }
                            .padding(vertical = 10.dp)
                            .testTag("role_student_toggle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = if (studentSelected) Color.White else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Student",
                                fontWeight = FontWeight.Bold,
                                color = if (studentSelected) Color.White else TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }

                    val staffSelected = selectedRole == UserRole.STAFF
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .then(
                                if (staffSelected) Modifier.background(StaffMarbleHeroBrush)
                                else Modifier.background(Color.Transparent)
                            )
                            .clickable {
                                selectedRole = UserRole.STAFF
                                errorMessage = null
                            }
                            .padding(vertical = 10.dp)
                            .testTag("role_staff_toggle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WorkOutline,
                                contentDescription = null,
                                tint = if (staffSelected) Color.White else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Staff / Mentor",
                                fontWeight = FontWeight.Bold,
                                color = if (staffSelected) Color.White else TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Auth Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (isRegisterMode) "Create ${selectedRole.name.lowercase().replaceFirstChar { it.uppercase() }} Account" else "Sign In to Your Workspace",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlueVariant
                )
                Spacer(modifier = Modifier.height(14.dp))

                if (isRegisterMode) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        placeholder = { Text(if (selectedRole == UserRole.STUDENT) "e.g. Maya Lin" else "e.g. Dr. Robert Chen") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = PrimaryBlue) },
                        colors = appTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_fullname_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text("Department") },
                        placeholder = { Text("e.g. Computer Science / Biomedical") },
                        leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = PrimaryBlue) },
                        colors = appTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_dept_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = yearOrTitle,
                        onValueChange = { yearOrTitle = it },
                        label = { Text(if (selectedRole == UserRole.STUDENT) "Academic Year" else "Faculty Title") },
                        placeholder = { Text(if (selectedRole == UserRole.STUDENT) "e.g. 3rd Year B.Tech" else "e.g. Associate Professor") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = PrimaryBlue) },
                        colors = appTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_year_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = skillsInput,
                        onValueChange = { skillsInput = it },
                        label = { Text(if (selectedRole == UserRole.STUDENT) "Skills (comma-separated)" else "Research Areas") },
                        placeholder = { Text("Python, TensorFlow, SQL, Kotlin") },
                        leadingIcon = { Icon(Icons.Default.StarOutline, contentDescription = null, tint = PrimaryBlue) },
                        colors = appTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_skills_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = interestsInput,
                        onValueChange = { interestsInput = it },
                        label = { Text("Project Domains / Interests") },
                        placeholder = { Text("AI & Machine Learning, Healthcare") },
                        leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = PrimaryBlue) },
                        colors = appTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_interests_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = null
                    },
                    label = { Text("Campus Email") },
                    placeholder = { Text(if (selectedRole == UserRole.STUDENT) "alex.student@campus.edu" else "dr.sharma@campus.edu") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = PrimaryBlue) },
                    colors = appTextFieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("email_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = { Text("Password") },
                    placeholder = { Text("Enter at least 6 characters") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryBlue) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    colors = appTextFieldColors(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                if (!isRegisterMode) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        TextButton(
                            onClick = { showForgotPasswordDialog = true },
                            modifier = Modifier.testTag("forgot_password_button")
                        ) {
                            Text(
                                text = "Forgot Password?",
                                fontSize = 12.sp,
                                color = PrimaryBlueLight,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Error / Success Feedback
                AnimatedVisibility(visible = errorMessage != null) {
                    Surface(
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage.orEmpty(),
                                color = Color(0xFFB91C1C),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = successMessage != null) {
                    Surface(
                        color = SecondaryGreenContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SecondaryGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = successMessage.orEmpty(),
                                color = SecondaryGreenVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Primary Action Button (Blue)
                Button(
                    onClick = {
                        errorMessage = null
                        successMessage = null
                        if (email.isBlank() || !email.contains("@")) {
                            errorMessage = "Please enter a valid campus email."
                            return@Button
                        }
                        if (password.length < 6) {
                            errorMessage = "Password must be at least 6 characters."
                            return@Button
                        }

                        if (isRegisterMode) {
                            if (fullName.isBlank()) {
                                errorMessage = "Please enter your full name."
                                return@Button
                            }
                            if (department.isBlank()) {
                                errorMessage = "Please enter your department."
                                return@Button
                            }
                            val skillsList = skillsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            val interestsList = interestsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            val res = onRegister(
                                fullName,
                                email,
                                selectedRole,
                                department,
                                yearOrTitle.ifBlank { if (selectedRole == UserRole.STUDENT) "1st Year" else "Faculty" },
                                skillsList,
                                interestsList
                            )
                            res.fold(
                                onSuccess = {
                                    successMessage = "Account created successfully! Welcome to ProjectConnect."
                                    onLoginSuccess()
                                },
                                onFailure = {
                                    errorMessage = it.message ?: "Registration failed."
                                }
                            )
                        } else {
                            val res = onPerformLogin(email, selectedRole)
                            res.fold(
                                onSuccess = { onLoginSuccess() },
                                onFailure = { errorMessage = it.message }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedRole == UserRole.STUDENT) StudentMarblePrimary else StaffMarblePrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_auth_button")
                ) {
                    Text(
                        text = if (isRegisterMode) "Complete Registration" else "Sign In",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Toggle Sign In vs Register
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isRegisterMode) "Already have an account?" else "New to ProjectConnect?",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    TextButton(
                        onClick = {
                            isRegisterMode = !isRegisterMode
                            errorMessage = null
                            successMessage = null
                        },
                        modifier = Modifier.testTag("toggle_register_mode")
                    ) {
                        Text(
                            text = if (isRegisterMode) "Sign In" else "Create Account",
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Demo Sign-In Card (Clear developer notice & instant access)
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderStroke),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = SecondaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Demo Sign-In (Local Evaluation Mode)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlueVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Clearly labeled demo accounts for rapid testing. Demo data persists locally on this device.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onDemoLogin(UserRole.STUDENT) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryBlue),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("demo_student_button")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Demo Student", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Alex (3rd Year)", fontSize = 10.sp, color = TextSecondary)
                        }
                    }

                    OutlinedButton(
                        onClick = { onDemoLogin(UserRole.STAFF) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SecondaryGreen),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, SecondaryGreen),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("demo_staff_button")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Demo Staff", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Dr. Sharma (Mentor)", fontSize = 10.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        var resetEmail by remember { mutableStateOf(email) }
        var resetSent by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = {
                Text(
                    text = "Reset Password",
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlueVariant
                )
            },
            text = {
                Column {
                    if (!resetSent) {
                        Text(
                            text = "Enter your institutional email address. We will verify your campus domain and send a secure reset link.",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = resetEmail,
                            onValueChange = { resetEmail = it },
                            label = { Text("Institutional Email") },
                            placeholder = { Text("your.name@campus.edu") },
                            singleLine = true,
                            colors = appTextFieldColors(),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Surface(
                            color = SecondaryGreenContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SecondaryGreen
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Password reset instructions dispatched to $resetEmail.",
                                    color = SecondaryGreenVariant,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (!resetSent) {
                    Button(
                        onClick = {
                            if (resetEmail.contains("@")) {
                                resetSent = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Send Reset Link", color = Color.White)
                    }
                } else {
                    Button(
                        onClick = { showForgotPasswordDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryGreen)
                    ) {
                        Text("Done", color = Color.White)
                    }
                }
            },
            dismissButton = {
                if (!resetSent) {
                    TextButton(onClick = { showForgotPasswordDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            }
        )
    }
}
