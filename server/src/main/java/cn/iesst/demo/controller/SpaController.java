package cn.iesst.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {
    @GetMapping({
            "/admin",
            "/admin/",
            "/admin/login",
            "/admin/dashboard",
            "/admin/banners",
            "/admin/journals",
            "/admin/submissions",
            "/admin/content",
            "/admin/students",
            "/admin/orders",
            "/admin/governance"
    })
    public String adminRoutes() {
        return "forward:/index.html";
    }

    @GetMapping({
            "/SCI",
            "/SCI/{id}",
            "/EI",
            "/EI/{id}",
            "/journals",
            "/journals/{id}",
            "/services/{kind}",
            "/about",
            "/submit",
            "/student/login",
            "/student/register",
            "/student/orders"
    })
    public String siteRoutes() {
        return "forward:/index.html";
    }
}
