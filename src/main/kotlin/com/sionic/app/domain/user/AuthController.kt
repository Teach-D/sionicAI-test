package com.sionic.app.domain.user

import com.sionic.app.domain.user.dto.LoginRequest
import com.sionic.app.domain.user.dto.LoginResponse
import com.sionic.app.domain.user.dto.RegisterRequest
import com.sionic.app.domain.user.dto.RegisterResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(private val userService: UserService) {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@Valid @RequestBody request: RegisterRequest): RegisterResponse =
        userService.register(request)

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): LoginResponse =
        userService.login(request)
}
