package org.example.controller

import org.example.entity.Account
import org.example.service.AccountService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import jakarta.servlet.http.HttpSession
import java.math.BigDecimal

@Controller
class BankController(private val accountService: AccountService) {

    @GetMapping("/")
    fun index(session: HttpSession): String {
        val accountId = session.getAttribute("accountId")
        return if (accountId != null) "redirect:/dashboard" else "redirect:/login"
    }

    @GetMapping("/login")
    fun loginPage(): String = "login"

    @PostMapping("/login")
    fun login(
        @RequestParam username: String,
        @RequestParam password: String,
        session: HttpSession,
        model: Model
    ): String {
        // A05 SECURITY MISCONFIGURATION
        if (username == "admin" && password == "admin") {
            session.setAttribute("isAdmin", true)
            return "redirect:/admin"
        }

        val account = accountService.login(username, password)
        return if (account != null) {
            session.setAttribute("accountId", account.id)
            "redirect:/dashboard"
        } else {
            model.addAttribute("error", "Invalid credentials")
            "login"
        }
    }
    // =====================================================
    // A05:2021 - SECURITY MISCONFIGURATION 
    // Vulnerability: Hardcoded default credentials admin:admin
    // =====================================================
    // FIX: Check admin from database:
    //
    // @PostMapping("/login")
    // fun login(
    //     @RequestParam username: String,
    //     @RequestParam password: String,
    //     session: HttpSession,
    //     model: Model
    // ): String {
    //     val account = accountService.login(username, password) ?: return "login".also {
    //         model.addAttribute("error", "Invalid credentials")
    //     }
    //     session.setAttribute("accountId", account.id)
    //     return if (account.isAdmin) "redirect:/admin" else "redirect:/dashboard"
    // }
    // =====================================================

    @GetMapping("/register")
    fun registerPage(): String = "register"

    @PostMapping("/register")
    fun register(
        @RequestParam username: String,
        @RequestParam password: String,
        model: Model
    ): String {
        val account = accountService.register(username, password)
        return if (account != null) {
            "redirect:/login?registered"
        } else {
            model.addAttribute("error", "Username already exists")
            "register"
        }
    }

    @PostMapping("/api/register")
    @ResponseBody
    fun registerApi(@RequestBody account: Account): Account? {  // A08 SOFTWARE AND DATA INTEGRITY FAILURES
        return accountService.registerAccount(account)
    }
    // =====================================================
    // A08:2021 - SOFTWARE AND DATA INTEGRITY FAILURES 
    // Vulnerability: Attacker can send {"username":"hacker","password":"123","isAdmin":true} and become admin without any validation
    // =====================================================
    // FIX: Use DTO instead of Entity:
    //
    // data class RegisterRequest(val username: String, val password: String)
    //
    // @PostMapping("/api/register")
    // @ResponseBody
    // fun registerApi(@RequestBody request: RegisterRequest): Account? {
    //     return accountService.register(request.username, request.password)
    // }
    // =====================================================

    @GetMapping("/dashboard")
    fun dashboard(session: HttpSession, model: Model): String {
        val accountId = session.getAttribute("accountId") as? Long ?: return "redirect:/login"
        val account = accountService.getAccount(accountId) ?: return "redirect:/login"
        model.addAttribute("account", account)
        return "dashboard"
    }

    @PostMapping("/transfer")
    fun transfer(
        @RequestParam toUsername: String,
        @RequestParam amount: BigDecimal,
        session: HttpSession
    ): String {
        val accountId = session.getAttribute("accountId") as? Long ?: return "redirect:/login"
        val success = accountService.transfer(accountId, toUsername, amount)
        return if (success) "redirect:/dashboard?success" else "redirect:/dashboard?error"
    }

    @GetMapping("/logout")
    fun logout(session: HttpSession): String {
        session.invalidate()
        return "redirect:/login"
    }
    
    @GetMapping("/admin")
    fun adminPage(model: Model): String {  // A01 BROKEN ACCESS CONTROL
        model.addAttribute("accounts", accountService.getAllAccounts())
        return "admin"
    }
    // =====================================================
    // A01:2021 - BROKEN ACCESS CONTROL 
    // Vulnerability: anyone can access /admin without logging in
    // =====================================================
    // FIX: Add session check:
    //
    // @GetMapping("/admin")
    // fun adminPage(session: HttpSession, model: Model): String {
    //     if (session.getAttribute("isAdmin") != true) return "redirect:/login"
    //     model.addAttribute("accounts", accountService.getAllAccounts())
    //     return "admin"
    // }
    // =====================================================
}
