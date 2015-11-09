package com.homework.auth.util;

import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;

public class ResizeImage {

	/**
	 * @param im
	 *            原始图像
	 * @param resizeTimes
	 *            需要缩小的倍数，缩小2倍为原来的1/2 ，这个数值越大，返回的图片越小
	 * @return 返回处理后的图像
	 */
	public BufferedImage resizeImage(BufferedImage im, int maxLength) {
		/* 原始图像的宽度和高度 */
		float width = im.getWidth();
		float height = im.getHeight();
		int toWidth;
		int toHeight;
		if (width > height) {
			toWidth = maxLength;
			toHeight = (int) ((height / width) * toWidth);
		} else {
			toHeight = maxLength;
			toWidth = (int) (width / height * toHeight);
		}

		/* 新生成结果图片 */
		BufferedImage result = new BufferedImage(toWidth, toHeight,
				BufferedImage.TYPE_INT_RGB);

		result.getGraphics().drawImage(
				im.getScaledInstance(toWidth, toHeight,
						java.awt.Image.SCALE_SMOOTH), 0, 0, null);
		return result;
	}

	/**
	 * @param im
	 *            原始图像
	 * @param resizeTimes
	 *            需要缩小的倍数，缩小2倍为原来的1/2 ，这个数值越大，返回的图片越小
	 * @return 返回处理后的图像
	 */
	public BufferedImage resizeMinImage(BufferedImage im, int minLength) {
		/* 原始图像的宽度和高度 */
		float width = im.getWidth();
		float height = im.getHeight();
		int toWidth;
		int toHeight;
		if (width > height) {
			toHeight=minLength;
			toWidth=(int) ((width / height) * toHeight);
		} else {
			
			toWidth = minLength;
			toHeight = (int) ((height / width) * toWidth);
		}

		/* 新生成结果图片 */
		BufferedImage result = new BufferedImage(toWidth, toHeight,
				BufferedImage.TYPE_INT_RGB);

		result.getGraphics().drawImage(
				im.getScaledInstance(toWidth, toHeight,
						java.awt.Image.SCALE_SMOOTH), 0, 0, null);
		return result;
	}

	/**
	 * 把图片写到磁盘上
	 * 
	 * @param im
	 * @param path
	 *            eg: C://home// 图片写入的文件夹地址
	 * @param fileName
	 *            DCM1987.jpg 写入图片的名字
	 * @return
	 */
	public boolean writeToDisk(BufferedImage im, String path, String fileName) {
		File f = new File(path + fileName);
		String fileType = getExtension(fileName);
		if (fileType == null)
			return false;
		try {
			ImageIO.write(im, fileType, f);
			im.flush();
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	public void writeHighQuality(BufferedImage im, String fileFullPath,
			String fileName) {
		writeHighQuality(im, fileFullPath, fileName, 0.1f);
	}

	public boolean writeHighQuality(BufferedImage im, String fileFullPath,
			String fileName, float quality) {
		try {
			/* 输出到文件流 */
			FileOutputStream newimage = new FileOutputStream(fileFullPath
					+ fileName);
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(newimage);
			JPEGEncodeParam jep = JPEGCodec.getDefaultJPEGEncodeParam(im);
			/* 压缩质量 */
			jep.setQuality(quality, true);
			encoder.encode(im, jep);
			/* 近JPEG编码 */
			newimage.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 返回文件的文件后缀名
	 * 
	 * @param fileName
	 * @return
	 */
	public String getExtension(String fileName) {
		try {
			return fileName.split("\\.")[fileName.split("\\.").length - 1];
		} catch (Exception e) {
			return null;
		}
	}


}
