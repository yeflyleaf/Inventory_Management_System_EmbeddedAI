package com.example.backend.service.impl;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.backend.service.BarcodeService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

/**
 * 条形码识别服务实现
 */
@Service
public class BarcodeServiceImpl implements BarcodeService {

    /**
     * 从上传的图片中识别条形码
     * 支持多种格式：EAN-13, EAN-8, UPC-A, UPC-E, CODE-39, CODE-93, CODE-128, ITF, CODABAR, QR_CODE, DATA_MATRIX
     * 如果直接识别失败，会自动尝试旋转图片（90度、180度、270度）后再次识别
     *
     * @param file 上传的图片文件
     * @return 识别到的条形码内容，如果未识别到则返回null
     */
    @Override
    public String recognizeBarcodeFromImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            // 将上传的文件转换为 BufferedImage
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                return null;
            }

            // 尝试多种方式识别条形码
            String result = decodeBarcode(image);
            if (result != null) {
                return result;
            }

            // 如果直接识别失败，尝试旋转图片后识别
            result = decodeWithRotation(image);
            if (result != null) {
                return result;
            }

            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 使用 ZXing 库对图片进行条形码解码
     * 配置了 TRY_HARDER 模式以提高识别率
     *
     * @param image 待识别的缓冲图片对象
     * @return 识别到的条形码文本，如果识别失败则返回null
     */
    private String decodeBarcode(BufferedImage image) {
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            // 配置解码参数
            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
            // 支持多种条形码格式
            hints.put(DecodeHintType.POSSIBLE_FORMATS, EnumSet.of(
                    BarcodeFormat.EAN_13,
                    BarcodeFormat.EAN_8,
                    BarcodeFormat.UPC_A,
                    BarcodeFormat.UPC_E,
                    BarcodeFormat.CODE_39,
                    BarcodeFormat.CODE_93,
                    BarcodeFormat.CODE_128,
                    BarcodeFormat.ITF,
                    BarcodeFormat.CODABAR,
                    BarcodeFormat.QR_CODE,
                    BarcodeFormat.DATA_MATRIX));
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");

            MultiFormatReader reader = new MultiFormatReader();
            Result result = reader.decode(bitmap, hints);
            return result.getText();
        } catch (NotFoundException e) {
            // 未找到条形码
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 尝试对图片进行旋转后识别
     * 依次旋转 90、180、270 度进行尝试
     *
     * @param image 原始图片
     * @return 识别到的条形码文本，如果所有角度都识别失败则返回null
     */
    private String decodeWithRotation(BufferedImage image) {
        // 尝试 90 度、180 度、270 度旋转
        int[] rotations = { 90, 180, 270 };

        for (int degrees : rotations) {
            BufferedImage rotatedImage = rotateImage(image, degrees);
            String result = decodeBarcode(rotatedImage);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    /**
     * 旋转图片指定角度
     *
     * @param image   原始图片
     * @param degrees 旋转角度 (90, 180, 270)
     * @return 旋转后的新图片对象
     */
    private BufferedImage rotateImage(BufferedImage image, int degrees) {
        double radians = Math.toRadians(degrees);
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));

        int width = image.getWidth();
        int height = image.getHeight();

        int newWidth = (int) Math.floor(width * cos + height * sin);
        int newHeight = (int) Math.floor(height * cos + width * sin);

        BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight, image.getType());
        java.awt.Graphics2D g2d = rotatedImage.createGraphics();

        g2d.translate((newWidth - width) / 2, (newHeight - height) / 2);
        g2d.rotate(radians, width / 2.0, height / 2.0);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        return rotatedImage;
    }
}
