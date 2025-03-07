package com.einvoice.service;

import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import cn.hutool.extra.qrcode.BufferedImageLuminanceSource;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.Loader;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

public class QrPdf {
    private final Path docPath;
    private final Map<Integer, String> qrCodeMap;

    /**
     * 发票的PDF格式.
     *
     * @param docPath PDF文件的路径.
     */
    public QrPdf(Path docPath) {
        this.docPath = docPath;
        this.qrCodeMap = new HashMap<>();
    }

    /**
     * 取到PDF的总页数.
     *
     * @return PDF的总页数
     * @throws IOException 如果无法取到PDF的总页数
     */
    public int getNumberOfPages() throws IOException {
        PDDocument pdfDoc = Loader.loadPDF(docPath.toFile());

        int numberOfPages = pdfDoc.getNumberOfPages();
        pdfDoc.close();
        return numberOfPages;
    }

    /**
     * 渲染一个PDF页面.
     *
     * @param pageIndex 要渲染的页码
     * @param dpi       要渲染成的DPI
     * @return 渲染后的PDF页面
     * @throws IOException 如果渲染失败
     */
    private BufferedImage getPageImage(int pageIndex, int dpi) throws IOException {
        PDDocument pdfDoc = Loader.loadPDF(docPath.toFile());
        PDFRenderer renderer = new PDFRenderer(pdfDoc);
        BufferedImage image = renderer.renderImageWithDPI(pageIndex - 1, dpi, ImageType.BINARY);
        pdfDoc.close();
        return image;
    }

    /**
     * 提取并解码PDF文件中指定页数中的二维码.
     *
     * @param page 想要解析的二维码所处的页数
     * @return 提取并解析后的二维码
     * @throws IOException       如果读取文件失败或者没有这个页码
     * @throws NotFoundException 如果解析二维码失败
     */
    public String getQRCode(int page)
            throws IOException, NotFoundException {
        // Use a stored value in available for speed.
        if (qrCodeMap.containsKey(page)) {
            return qrCodeMap.get(page);
        }

        if (page > getNumberOfPages()) {
            throw new IOException("Page does not exist!");
        }
        // No quick solutions, so lets scan!
        String qrCode = scanQRCode(page);
        qrCodeMap.put(page, qrCode);
        return qrCode;
    }

    /**
     * 扫描并解码PDF渲染图像中的二维码.
     *
     * @param pageIndex 需要渲染的页码
     * @return 二维码解析后的值.
     * @throws NotFoundException 如果无法识别二维码
     * @throws IOException       如果无法读取文件
     */
    private String scanQRCode(int pageIndex) throws NotFoundException, IOException {
        // Hints for scanning
        Vector<BarcodeFormat> decodeFormat = new Vector<>();
        decodeFormat.add(BarcodeFormat.QR_CODE);
        Hashtable<DecodeHintType, Object> hintMap = new Hashtable<>();
        hintMap.put(DecodeHintType.TRY_HARDER, true);
        hintMap.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormat);
        MultiFormatReader qrcodeReader = new MultiFormatReader();
        qrcodeReader.setHints(hintMap);

        // 经多次试验, 从最小的DPI开始, 性能最好
        int[] dpiSettings = { 150, 200, 250, 300 };
        for (int i = 0; i < dpiSettings.length; i++) {
            try {
                // 从最小的DPI开始.
                BufferedImage pageImage = getPageImage(pageIndex, dpiSettings[i]);
                LuminanceSource source = new BufferedImageLuminanceSource(pageImage);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                // By using decodeWithState, we keep the Hints that we set earlier.
                Result result = qrcodeReader.decodeWithState(bitmap);
                return result.getText();
            } catch (NotFoundException e) {
                // 该DPI尝试失败. 使用数组中下一个DPI.
                // A NotFoundException is thrown.
                if (i == dpiSettings.length - 1) {
                    throw e;
                }
            }
        }
        return null;
    }
}
