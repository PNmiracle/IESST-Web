package cn.iesst.demo.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InvoiceRequest(
        Long id,
        Long orderId,
        Long studentUserId,
        String invoiceTitle,
        String taxNumber,
        String invoiceType,
        BigDecimal invoiceAmount,
        String receiverEmail,
        String receiverPhone,
        String receiverAddress,
        String status,
        String remark,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
