package com.dekagames.atlasmaker;

import com.dekagames.atlasmaker.algorithm.PictureRect;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *  Класс для описания кадра спрайта. Содержит индивидуальные настройки кадра.
 */
public class FrameNode {
    public BufferedImage image;	            //оригинальная картинка в памяти
    public BufferedImage resultImage;       //картинка для атласа - кропнутая и обведенная

    public      int     cropLeft,		    // сколько пикселей при кропе будет
                        cropTop,			// обрезано с каждой стороны
                        cropRight,			// отрицательное значение - кроп не инициализирован
                        cropBottom;			// и требует расчета

    public PictureRect pictureRect;	        // rect для упаковки - инициализируется при передаче в процедуру
                                            // упаковки. Здесь же после упаковки находятся найденные координаты

    private     String          fileName;		// полный путь к файлу с картинкой
    public      int	            xPivot, yPivot;	// координаты горячей точки


    public FrameNode(String path) {
        fileName = path;

        cropLeft = -1;
        cropTop = -1;
        cropRight = -1;
        cropBottom = -1;

        try {
            image = ImageIO.read(new File(path));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Can not open file: " + path);
            Logger.getLogger(FrameNode.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /**
     * производит экземпляр PictureRect необходимый для расчета упаковки.
     *
     * @param crop
     * @return
     */
    public PictureRect buildPictureRect(boolean crop, boolean pixel_border, int transp_border) {
        int w, h;	// ширина и высота блока (с учетом кропа и бордера)
        int pixBorderrWidth;

        // есть ли окантовка дублирующим пикселем
        if(pixel_border)
            pixBorderrWidth = 1;
        else
            pixBorderrWidth = 0;

        if (crop) {
            cropImage();
        } else {
            cropLeft = cropRight = cropTop = cropBottom = 0;
        }

        w = image.getWidth() - cropLeft - cropRight + transp_border * 2 + pixBorderrWidth*2;
        h = image.getHeight() - cropTop - cropBottom + transp_border * 2 + pixBorderrWidth*2;

        pictureRect = new PictureRect(w, h);

        // сделаем результирующее изображение - именно оно потом рисуется в атласе
        if (image == null){
            JOptionPane.showMessageDialog(null, "Error - Image was not loaded!");
        } else {
            // вырежем из первоначального изображения нужный кусок
            BufferedImage croppedImage = image.getSubimage(cropLeft, cropTop,
                                                            image.getWidth()-cropLeft-cropRight,
                                                            image.getHeight()-cropTop-cropBottom);
            // создадим нужного размера картинку и врисуем туду вырезанный кусок
            resultImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D resGr = resultImage.createGraphics();
            resGr.drawImage(croppedImage, transp_border + pixBorderrWidth, transp_border + pixBorderrWidth, null);

            //сделаем погранчную обводку если надо
            if (pixel_border){
                for (int i=0; i<croppedImage.getWidth(); i++){      // верхняя линия
                    int rgb = croppedImage.getRGB(i,0);
                    resultImage.setRGB(transp_border+pixBorderrWidth+i, transp_border, rgb);
                }
                for (int i=0; i<croppedImage.getWidth(); i++){      // нижняя линия
                    int rgb = croppedImage.getRGB(i,croppedImage.getHeight()-1);
                    resultImage.setRGB(transp_border+pixBorderrWidth+i, resultImage.getHeight()-transp_border-1, rgb);
                }
                for (int i=0; i<croppedImage.getHeight(); i++){      // левая линия
                    int rgb = croppedImage.getRGB(0,i);
                    resultImage.setRGB(transp_border, transp_border+pixBorderrWidth+i, rgb);
                }
                for (int i=0; i<croppedImage.getHeight(); i++){      // правая линия
                    int rgb = croppedImage.getRGB(croppedImage.getWidth()-1,i);
                    resultImage.setRGB(resultImage.getWidth()-transp_border-1, transp_border+pixBorderrWidth+i, rgb);
                }
                // углы
                int rgb = croppedImage.getRGB(0,0);
                resultImage.setRGB(transp_border, transp_border, rgb);
                rgb = croppedImage.getRGB(croppedImage.getWidth()-1,0);
                resultImage.setRGB(resultImage.getWidth()-transp_border-1, transp_border, rgb);
                rgb = croppedImage.getRGB(0,croppedImage.getHeight()-1);
                resultImage.setRGB(transp_border, resultImage.getHeight() - transp_border-1, rgb);
                rgb = croppedImage.getRGB(croppedImage.getWidth()-1,croppedImage.getHeight()-1);
                resultImage.setRGB(resultImage.getWidth()-transp_border-1, resultImage.getHeight() - transp_border-1, rgb);

            }
        }
        return pictureRect;
    }

    // расчет отсекаемого количества пикселей с каждой стороны
    private void cropImage() {
        boolean line_empty;

        if (image == null) return;

        // сначала отсечем слева
        for (int i = 0; i < image.getWidth(); i++) {
            cropLeft = i;
            line_empty = true;
            // цикл по точкам вертикальной линии
            for (int j = 0; j < image.getHeight(); j++) {
                if (0 == ((image.getRGB(i, j) >> 24) & 0xff))    continue;			// если альфа = 0 перейдем к следующей точке
                else {
                    line_empty = false;				// если есть непрозрачность - закончим перебор
                    break;
                }
            }
            if (!line_empty) 	break;				// если перебор был окончен - дальше не проверяем
        }


        // отсекаем  справа
        for (int i = image.getWidth() - 1; i >= 0; i--) {
            cropRight = image.getWidth() - 1 - i;
            line_empty = true;
            // цикл по точкам вертикальной линии
            for (int j = 0; j < image.getHeight(); j++) {
                if (0 == ((image.getRGB(i, j) >> 24) & 0xff))    continue;			// если альфа = 0 перейдем к следующей точке
                else {
                    line_empty = false;				// если есть непрозрачность - закончим перебор
                    break;
                }
            }
            if (!line_empty) break;				// если перебор был окончен - дальше не проверяем
        }

        // отсекаем  снизу
        for (int i = image.getHeight() - 1; i >= 0; i--) {
            cropBottom = image.getHeight() - 1 - i;
            line_empty = true;
            // цикл по точкам горизонтальной линии
            for (int j = 0; j < image.getWidth(); j++) {
                if (0 == ((image.getRGB(j, i) >> 24) & 0xff))    continue;			// если альфа = 0 перейдем к следующей точке
                else {
                    line_empty = false;				// если есть непрозрачность - закончим перебор
                    break;
                }
            }
            if (!line_empty) break;				// если перебор был окончен - дальше не проверяем
        }

        // отсекаем  сверху
        for (int i = 0; i < image.getHeight(); i++) {
            cropTop = i;
            line_empty = true;
            // цикл по точкам горизонтальной линии
            for (int j = 0; j < image.getWidth(); j++) {
                if (0 == ((image.getRGB(j, i) >> 24) & 0xff))    continue;			// если альфа = 0 перейдем к следующей точке
                else {
                    line_empty = false;				// если есть непрозрачность - закончим перебор
                    break;
                }
            }
            if (!line_empty) break;				// если перебор был окончен - дальше не проверяем
        }
    }


    @Override
    public String toString() {
        return fileName;
    }

    public String getPath() {
        return fileName;
    }

}
