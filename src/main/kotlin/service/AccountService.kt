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

    fun registerAccount(account: Account): Account? {
        if (accountRepository.findByUsername(account.username) != null) return null
        return accountRepository.save(account)
    }

    fun login(username: String, password: String) = accountRepository.findByUsernameAndPassword(username, password)

    fun getAccount(id: Long) = accountRepository.findById(id).orElse(null)

    fun getAllAccounts() = accountRepository.findAll()

    @Transactional
    fun updateUsername(accountId: Long, newUsername: String): Boolean {
        val sql = "UPDATE accounts SET username = '$newUsername' WHERE id = $accountId"  // A03 INJECTION
        entityManager.createNativeQuery(sql).executeUpdate()
        return true
    }
    // =====================================================
    // A03:2021 - INJECTION 
    // Vulnerability: Attacker can modify any field: balance, isAdmin, or delete data
    // =====================================================
    // FIX: Use parameterized query:
    //   val sql = "UPDATE accounts SET username = :newUsername WHERE id = :id"
    //   entityManager.createNativeQuery(sql)
    //       .setParameter("newUsername", newUsername)
    //       .setParameter("id", accountId)
    //       .executeUpdate()
    // =====================================================

    @Transactional
    fun transfer(fromId: Long, toUsername: String, amount: BigDecimal): Boolean {
        val from = getAccount(fromId) ?: return false
        val to = accountRepository.findByUsername(toUsername) ?: return false
        // A04 INSECURE DESIGN
        from.balance -= amount
        to.balance += amount
        accountRepository.save(from)
        accountRepository.save(to)
        return true
    }
    // =====================================================
    // A04:2021 - INSECURE DESIGN 
    // Vulnerability: No balance validation. Can transfer more than available
    // =====================================================
    // FIX for A04: Add balance check:
    //   if (from.balance < amount) return false
    // =====================================================
}
