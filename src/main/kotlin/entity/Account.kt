package org.example.entity

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "accounts")
data class Account(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(unique = true, nullable = false)
    val username: String = "",
    
    @Column(nullable = false)
    val password: String = "",
    
    @Column(nullable = false)
    var balance: BigDecimal = BigDecimal.ZERO,
    
    @Column(nullable = false)
    val isAdmin: Boolean = false
)
