package cn.iesst.demo.controller;

import cn.iesst.demo.model.LoginRequest;
import cn.iesst.demo.model.InvoiceRequest;
import cn.iesst.demo.model.StudentRegisterRequest;
import cn.iesst.demo.model.StudentOrder;
import cn.iesst.demo.model.StudentOrderProgress;
import cn.iesst.demo.service.StudentUserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentController {
    private final StudentUserService studentUserService;

    public StudentController(StudentUserService studentUserService) {
        this.studentUserService = studentUserService;
    }

    @GetMapping("/me")
    public Map<String, Object> me(HttpSession session) {
        return studentUserService.me(session);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        return studentUserService.login(request.username(), request.password(), session);
    }

    @PostMapping("/register")
    public Map<String, Object> register(@Valid @RequestBody StudentRegisterRequest request, HttpSession session) {
        return studentUserService.register(request.mobile(), request.displayName(), request.password(), request.confirmPassword(), session);
    }

    @PostMapping("/logout")
    public Map<String, Boolean> logout(HttpSession session) {
        studentUserService.logout(session);
        return Map.of("success", true);
    }

    @GetMapping("/orders")
    public List<StudentOrder> orders(HttpSession session) {
        return studentUserService.orders(session);
    }

    @GetMapping("/orders/{id}/progress")
    public List<StudentOrderProgress> orderProgress(@PathVariable long id, HttpSession session) {
        return studentUserService.orderProgress(id, session);
    }

    @GetMapping("/invoices")
    public List<InvoiceRequest> invoices(HttpSession session) {
        return studentUserService.invoices(session);
    }
}
