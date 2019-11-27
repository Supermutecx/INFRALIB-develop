package ro.infrasoft.infralib.io.pdf;

import com.tomgibara.imageio.impl.tiff.TIFFImageReaderSpi;
import com.tomgibara.imageio.impl.tiff.TIFFImageWriterSpi;
import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.PDimension;
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.util.GraphicsRenderingHints;
import org.icepdf.ri.util.FontPropertiesManager;
import org.icepdf.ri.util.PropertiesManager;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.spi.IIORegistry;
import javax.imageio.stream.ImageOutputStream;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.KernelJAI;
import javax.media.jai.LookupTableJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.operator.ErrorDiffusionDescriptor;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.SampleModel;
import java.io.File;
import java.util.Iterator;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * PDF utilities.
 */
public class PDFUtil {
    public static final double PRINTER_RESOLUTION = 600; //150, 200, 300, 600, 1200

    // This compression type may be wpecific to JAI ImageIO Tools
    public static final String COMPRESSION_TYPE_GROUP4FAX = "CCITT T.4";

    public static void pdfToTiff(File pdf, File tiff) throws Exception {

        ImageIO.scanForPlugins();
        IIORegistry registry = IIORegistry.getDefaultInstance();
        registry.registerServiceProvider(new TIFFImageWriterSpi());
        registry.registerServiceProvider(new TIFFImageReaderSpi());

        Iterator<ImageWriter> iterator = ImageIO.getImageWritersByFormatName("tiff");
        if (!iterator.hasNext()) {
            throw new Exception("Missing JAVA image api.");
        }
        boolean foundCompressionType = false;
        for (String type : iterator.next().getDefaultWriteParam().getCompressionTypes()) {
            if (COMPRESSION_TYPE_GROUP4FAX.equals(type)) {
                foundCompressionType = true;
                break;
            }
        }
        if (!foundCompressionType) {
            throw new Exception(
                    "TIFF ImageIO plug-in does not support Group 4 Fax " +
                            "compression type (" + COMPRESSION_TYPE_GROUP4FAX + ")");
        }

        long intime = System.currentTimeMillis();

        // open the url
        Document document = new Document();
        document.setFile(pdf.getAbsolutePath());

        // save page caputres to file.
        ImageOutputStream ios = ImageIO.createImageOutputStream(tiff);
        ImageWriter writer = ImageIO.getImageWritersByFormatName("tiff").next();
        writer.setOutput(ios);

        // Paint each pages content to an image and write the image to file
        for (int i = 0; i < document.getNumberOfPages(); i++) {
            final double targetDPI = PRINTER_RESOLUTION;
            float scale = 1.0f;
            float rotation = 0f;

            // Given no initial zooming, calculate our natural DPI when
            // printed to standard US Letter paper
            PDimension size = document.getPageDimension(i, rotation, scale);
            double dpi = Math.sqrt((size.getWidth() * size.getWidth()) +
                    (size.getHeight() * size.getHeight())) /
                    Math.sqrt((8.5 * 8.5) + (11 * 11));

            // Calculate scale required to achieve at least our target DPI
            if (dpi < (targetDPI - 0.1)) {
                scale = (float) (targetDPI / dpi);
                size = document.getPageDimension(i, rotation, scale);
            }

            int pageWidth = (int) size.getWidth();
            int pageHeight = (int) size.getHeight();

            BufferedImage image = new BufferedImage(
                    pageWidth, pageHeight, BufferedImage.TYPE_INT_RGB);
            Graphics g = image.createGraphics();
            document.paintPage(
                    i, g, GraphicsRenderingHints.PRINT, Page.BOUNDARY_CROPBOX,
                    rotation, scale);
            g.dispose();


            // JAI filter code
            PlanarImage surrogateImage = PlanarImage.wrapRenderedImage(image);
            LookupTableJAI lut = new LookupTableJAI(new byte[][]{{(byte) 0x00,
                    (byte) 0xff}, {(byte) 0x00, (byte) 0xff}, {(byte) 0x00, (byte) 0xff}});
            ImageLayout layout = new ImageLayout();
            byte[] map = new byte[]{(byte) 0x00, (byte) 0xff};
            ColorModel cm = new IndexColorModel(1, 2, map, map, map);
            layout.setColorModel(cm);
            SampleModel sm = new MultiPixelPackedSampleModel(DataBuffer.TYPE_BYTE,
                    surrogateImage.getWidth(),
                    surrogateImage.getHeight(),
                    1);
            layout.setSampleModel(sm);
            RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
            PlanarImage op = ErrorDiffusionDescriptor.create(surrogateImage, lut,
                    KernelJAI.ERROR_FILTER_FLOYD_STEINBERG, hints);
            BufferedImage dst = op.getAsBufferedImage();

            // capture the page image to file
            IIOImage img = new IIOImage(dst, null, null);
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionType(COMPRESSION_TYPE_GROUP4FAX);
            if (i == 0) {
                writer.write(null, img, param);
            } else {
                writer.writeInsert(-1, img, param);
            }
            image.flush();
        }

        ios.flush();
        ios.close();
        writer.dispose();

        // clean up resources
        document.dispose();
    }
}
