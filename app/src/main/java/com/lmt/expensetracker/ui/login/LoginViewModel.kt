//package com.lmt.expensetracker.ui.login
//
//import androidx.compose.runtime.getValue // Để dùng 'by' lấy giá trị
//import androidx.compose.runtime.setValue // Để dùng 'by' gán giá trị
//import androidx.compose.runtime.mutableStateOf // Hàm tạo State
//import androidx.lifecycle.ViewModel
//
//class LoginViewModel : ViewModel() {
//    private val authRepo = AuthRepository();
//
//    // State của UI - Giống như useState nhưng nằm ở ViewModel
//    var email by mutableStateOf("")
//    var password by mutableStateOf("")
//    var loginStatus by mutableStateOf("")
//
//    fun onLoginClick() {
//        val isLoginSuccess = authRepo.login(email, password)
//        loginStatus = if (isLoginSuccess) "Login Success" else "Login Failed"
//
//    }
//}