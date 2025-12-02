package com.pos.minimarketpos.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class BarcodeGenerator {

    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 100;
    private static final int DEFAULT_QR_SIZE = 250;

    /**
     * Generate a barcode and save it to a file
     */
    public void generateBarcodeToFile(String data, String filePath, BarcodeFormat format)
            throws WriterException, IOException {
        BitMatrix bitMatrix = new MultiFormatWriter()
                .encode(data, format, DEFAULT_WIDTH, DEFAULT_HEIGHT);

        Path path = FileSystems.getDefault().getPath(filePath);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);

        log.info("Barcode generated successfully at: {}", filePath);
    }

    /**
     * Generate a barcode and return as byte array
     */
    public byte[] generateBarcodeToBytes(String data, BarcodeFormat format)
            throws WriterException, IOException {
        BitMatrix bitMatrix = new MultiFormatWriter()
                .encode(data, format, DEFAULT_WIDTH, DEFAULT_HEIGHT);

        BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "PNG", baos);
            return baos.toByteArray();
        }
    }

    /**
     * Generate a barcode with custom dimensions
     */
    public byte[] generateBarcodeToBytes(String data, BarcodeFormat format, int width, int height)
            throws WriterException, IOException {
        BitMatrix bitMatrix = new MultiFormatWriter()
                .encode(data, format, width, height);

        BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "PNG", baos);
            return baos.toByteArray();
        }
    }

    /**
     * Generate EAN-13 barcode (most common for retail products)
     */
    public byte[] generateEAN13Barcode(String productCode) throws WriterException, IOException {
        // EAN-13 requires exactly 13 digits
        if (productCode.length() != 13 || !productCode.matches("\\d+")) {
            throw new IllegalArgumentException("EAN-13 barcode must be exactly 13 digits");
        }
        return generateBarcodeToBytes(productCode, BarcodeFormat.EAN_13);
    }

    /**
     * Generate Code 128 barcode (flexible, alphanumeric)
     */
    public byte[] generateCode128Barcode(String data) throws WriterException, IOException {
        return generateBarcodeToBytes(data, BarcodeFormat.CODE_128);
    }

    /**
     * Generate QR Code
     */
    public byte[] generateQRCode(String data) throws WriterException, IOException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix bitMatrix = new MultiFormatWriter()
                .encode(data, BarcodeFormat.QR_CODE, DEFAULT_QR_SIZE, DEFAULT_QR_SIZE, hints);

        BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "PNG", baos);
            return baos.toByteArray();
        }
    }

    /**
     * Generate QR Code with custom size
     */
    public byte[] generateQRCode(String data, int size) throws WriterException, IOException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix bitMatrix = new MultiFormatWriter()
                .encode(data, BarcodeFormat.QR_CODE, size, size, hints);

        BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "PNG", baos);
            return baos.toByteArray();
        }
    }

    /**
     * Generate a product barcode with automatic format detection
     */
    public byte[] generateProductBarcode(String productCode) throws WriterException, IOException {
        // If it's 13 digits, use EAN-13
        if (productCode.matches("\\d{13}")) {
            return generateEAN13Barcode(productCode);
        }
        // If it's 12 digits, use UPC-A
        else if (productCode.matches("\\d{12}")) {
            return generateBarcodeToBytes(productCode, BarcodeFormat.UPC_A);
        }
        // Otherwise, use Code 128 (alphanumeric)
        else {
            return generateCode128Barcode(productCode);
        }
    }

    /**
     * Generate receipt QR code with sale information
     */
    public byte[] generateReceiptQRCode(Long saleId, String storeName, String total)
            throws WriterException, IOException {
        String qrData = String.format("Sale ID: %d\nStore: %s\nTotal: %s",
                saleId, storeName, total);
        return generateQRCode(qrData);
    }
}