package com.sionic.app.domain.user

import com.sionic.app.domain.user.dto.LoginRequest
import com.sionic.app.domain.user.dto.LoginResponse
import com.sionic.app.domain.user.dto.RegisterRequest
import com.sionic.app.domain.user.dto.RegisterResponse
import com.sionic.app.exception.DuplicateEmailException
import com.sionic.app.security.JwtTokenProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) {

    @Transactional
    fun register(request: RegisterRequest): RegisterResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw DuplicateEmailException()
        }
        val user = userRepository.save(
            User(
                email = request.email,
                password = passwordEncoder.encode(request.password),
                name = request.name
            )
        )
        return RegisterResponse.from(user)
    }

    @Transactional(readOnly = true)
    fun login(request: LoginRequest): LoginResponse {
        val user = userRepository.findByEmail(request.email)
            .filter { passwordEncoder.matches(request.password, it.password) }
            .orElseThrow { BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.") }
        return LoginResponse(accessToken = jwtTokenProvider.generateToken(user.email, user.role.name))
    }
}
