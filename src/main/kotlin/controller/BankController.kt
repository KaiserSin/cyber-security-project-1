package org.example.controller

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
        val account = accountService.login(username, password)
        return if (account != null) {
            session.setAttribute("accountId", account.id)
            "redirect:/dashboard"
        } else {
            model.addAttribute("error", "Invalid credentials")
            "login"
        }
    }

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
        session: HttpSession,
        model: Model
    ): String {
        val accountId = session.getAttribute("accountId") as? Long ?: return "redirect:/login"
        val success = accountService.transfer(accountId, toUsername, amount)
        return if (success) {
            "redirect:/dashboard?success"
        } else {
            "redirect:/dashboard?error"
        }
    }

    @GetMapping("/logout")
    fun logout(session: HttpSession): String {
        session.invalidate()
        return "redirect:/login"
    }
}
