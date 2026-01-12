package org.example.config

import org.example.entity.Account
import org.example.repository.AccountRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class DataInitializer(private val accountRepository: AccountRepository) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (accountRepository.findByUsername("admin") == null) {
            accountRepository.save(
                Account(
                    username = "admin",
                    password = "admin",
                    balance = BigDecimal("10000.00"),
                    isAdmin = true
                )
            )
        }

        val users = listOf(
            Account(username = "alice", password = "alice123", balance = BigDecimal("500.00")),
            Account(username = "bob", password = "bob123", balance = BigDecimal("300.00")),
            Account(username = "charlie", password = "charlie123", balance = BigDecimal("150.00"))
        )

        users.forEach { user ->
            if (accountRepository.findByUsername(user.username) == null) {
                accountRepository.save(user)
            }
        }
    }
}
