package cn.iesst.demo.security;

public class StudentAuthenticationException extends RuntimeException {
    public StudentAuthenticationException(String message) {
        super(message);
    }
}
