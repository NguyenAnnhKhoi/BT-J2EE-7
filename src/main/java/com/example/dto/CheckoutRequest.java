package com.example.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutRequest {

    @NotBlank(message = "Ho ten nguoi nhan khong duoc de trong")
    private String receiverName;

    @NotBlank(message = "So dien thoai khong duoc de trong")
    private String receiverPhone;

    @NotBlank(message = "Dia chi giao hang khong duoc de trong")
    private String shippingAddress;

    @NotBlank(message = "Vui long chon phuong thuc thanh toan")
    private String paymentMethod;

    private String note;
}
