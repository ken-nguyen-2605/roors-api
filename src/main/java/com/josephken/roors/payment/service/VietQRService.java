package com.josephken.roors.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * VietQR Service - Generates QR codes for Vietnamese bank transfers
 * Following VietQR standard supported by all major Vietnamese banks
 */
@Service
@Slf4j
public class VietQRService {

    /**
     * Generate VietQR data string for QR code generation
     *
     * Format: https://img.vietqr.io/image/{BANK_CODE}-{ACCOUNT_NUMBER}-{TEMPLATE}.jpg?amount={AMOUNT}&addInfo={INFO}&accountName={NAME}
     * 
     * @param bankCode Bank code (e.g., "970415" for Vietinbank, "970436" for Vietcombank)
     * @param accountNumber Bank account number
     * @param accountName Account holder name
     * @param amount Transaction amount
     * @param addInfo Additional info/description (order number)
     * @return VietQR URL for generating QR code image
     */
    public String generateVietQR(String bankCode, String accountNumber, String accountName, 
                                  BigDecimal amount, String addInfo) {
        try {
            String template = "compact"; // compact, compact2, print, or qr_only
            
            String baseUrl = String.format("https://img.vietqr.io/image/%s-%s-%s.jpg",
                    bankCode, accountNumber, template);
            
            StringBuilder urlBuilder = new StringBuilder(baseUrl);
            urlBuilder.append("?amount=").append(amount.toString());
            urlBuilder.append("&addInfo=").append(encodeValue(addInfo));
            urlBuilder.append("&accountName=").append(encodeValue(accountName));
            
            String qrUrl = urlBuilder.toString();
            log.info("Generated VietQR: {}", qrUrl);
            return qrUrl;
            
        } catch (Exception e) {
            log.error("Error generating VietQR: {}", e.getMessage());
            throw new RuntimeException("Failed to generate VietQR code");
        }
    }
    
    /**
     * Generate VietQR data string using EMV QRCode format (raw QR data)
     * This can be used to generate QR code images locally
     * 
     * @param bankCode Bank BIN (e.g., "970415")
     * @param accountNumber Bank account number
     * @param amount Transaction amount
     * @param description Payment description
     * @return Raw QR data string
     */
    public String generateVietQRData(String bankCode, String accountNumber, 
                                      BigDecimal amount, String description) {
        // EMV QRCode format for VietQR
        StringBuilder qrData = new StringBuilder();
        
        // Payload Format Indicator
        qrData.append("00020101");
        
        // Point of Initiation Method (12 = static, 11 = dynamic)
        qrData.append("010212");
        
        // Merchant Account Information (tag 38 for VietQR)
        String merchantInfo = buildMerchantInfo(bankCode, accountNumber);
        qrData.append("38").append(String.format("%02d", merchantInfo.length())).append(merchantInfo);
        
        // Transaction Currency (704 = VND)
        qrData.append("5303704");
        
        // Transaction Amount
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            String amountStr = amount.toString();
            qrData.append("54").append(String.format("%02d", amountStr.length())).append(amountStr);
        }
        
        // Country Code
        qrData.append("5802VN");
        
        // Additional Data Field (description)
        if (description != null && !description.isEmpty()) {
            String additionalData = buildAdditionalData(description);
            qrData.append("62").append(String.format("%02d", additionalData.length())).append(additionalData);
        }
        
        // CRC (placeholder, should be calculated)
        qrData.append("6304");
        
        return qrData.toString();
    }
    
    private String buildMerchantInfo(String bankCode, String accountNumber) {
        StringBuilder info = new StringBuilder();
        
        // GUID for VietQR
        String guid = "A000000727";
        info.append("00").append(String.format("%02d", guid.length())).append(guid);
        
        // Beneficiary Organization (bank code)
        info.append("01").append(String.format("%02d", bankCode.length())).append(bankCode);
        
        // Beneficiary Account (account number)
        info.append("02").append(String.format("%02d", accountNumber.length())).append(accountNumber);
        
        return info.toString();
    }
    
    private String buildAdditionalData(String description) {
        // Bill Number or Reference
        return "08" + String.format("%02d", description.length()) + description;
    }
    
    private String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
                    .replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            log.error("Error encoding value: {}", e.getMessage());
            return value;
        }
    }
    
    /**
     * Get bank code from bank name
     * List of major Vietnamese banks
     */
    public String getBankCode(String bankName) {
        return switch (bankName.toUpperCase()) {
            case "VIETCOMBANK", "VCB" -> "970436";
            case "VIETINBANK", "VTB" -> "970415";
            case "BIDV" -> "970418";
            case "AGRIBANK" -> "970405";
            case "TECHCOMBANK", "TCB" -> "970407";
            case "MBBANK", "MB" -> "970422";
            case "VPBANK", "VPB" -> "970432";
            case "SACOMBANK", "STB" -> "970403";
            case "ACB" -> "970416";
            case "SHB" -> "970443";
            case "TPBANK", "TPB" -> "970423";
            case "VIETBANK", "VBB" -> "970433";
            default -> "970436"; // Default to Vietcombank
        };
    }
}
