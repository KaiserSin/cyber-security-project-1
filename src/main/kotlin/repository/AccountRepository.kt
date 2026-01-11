package org.example.repository

import org.example.entity.Account
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AccountRepository : JpaRepository<Account, Long> {
    fun findByUsername(username: String): Account?
    fun findByUsernameAndPassword(username: String, password: String): Account?
}
