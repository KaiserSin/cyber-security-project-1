package org.example.service

import org.example.entity.Account
import org.example.repository.AccountRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class AccountService(private val accountRepository: AccountRepository) {

    fun register(username: String, password: String): Account? {
        if (accountRepository.findByUsername(username) != null) {
            return null
        }
        val account = Account(
            username = username,
            password = password,
            balance = BigDecimal("100.00")
        )
        return accountRepository.save(account)
    }

    fun login(username: String, password: String): Account? {
        return accountRepository.findByUsernameAndPassword(username, password)
    }

    fun getAccount(id: Long): Account? {
        return accountRepository.findById(id).orElse(null)
    }

    fun transfer(fromId: Long, toUsername: String, amount: BigDecimal): Boolean {
        val fromAccount = accountRepository.findById(fromId).orElse(null) ?: return false
        val toAccount = accountRepository.findByUsername(toUsername) ?: return false
        
        if (fromAccount.balance < amount || amount <= BigDecimal.ZERO) {
            return false
        }
        
        fromAccount.balance = fromAccount.balance - amount
        toAccount.balance = toAccount.balance + amount
        
        accountRepository.save(fromAccount)
        accountRepository.save(toAccount)
        return true
    }
}
