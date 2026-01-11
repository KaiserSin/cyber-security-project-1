package org.example.service

import org.example.entity.Account
import org.example.repository.AccountRepository
import org.springframework.stereotype.Service
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class AccountService(private val accountRepository: AccountRepository) {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    fun register(username: String, password: String): Account? {
        if (accountRepository.findByUsername(username) != null) return null
        return accountRepository.save(Account(username = username, password = password, balance = BigDecimal("100.00")))
    }

    fun login(username: String, password: String) = accountRepository.findByUsernameAndPassword(username, password)

    fun getAccount(id: Long) = accountRepository.findById(id).orElse(null)

    fun getAllAccounts() = accountRepository.findAll()

    @Transactional
    fun transfer(fromId: Long, toUsername: String, amount: BigDecimal): Boolean {
        val from = getAccount(fromId) ?: return false
        
        val sql = "SELECT * FROM accounts WHERE username = '$toUsername'"  // A03 INJECTION
        val results = entityManager.createNativeQuery(sql, Account::class.java).resultList
        if (results.isEmpty()) return false
        
        val to = results[0] as Account
        // A04 INSECURE DESIGN
        
        from.balance -= amount
        to.balance += amount
        accountRepository.save(from)
        accountRepository.save(to)
        return true
    }
    // =====================================================
    // A03:2021 - INJECTION 
    // Vulnerability: SQL injection via string concatenation
    // =====================================================
    // A04:2021 - INSECURE DESIGN (
    // Vulnerability: No balance validation. Can transfer more than available
    // =====================================================
    // FIX for A03: Use safe repository method:
    //   val to = accountRepository.findByUsername(toUsername) ?: return false
    //
    // FIX for A04: Add balance check:
    //   if (from.balance < amount) return false
    // =====================================================
}
