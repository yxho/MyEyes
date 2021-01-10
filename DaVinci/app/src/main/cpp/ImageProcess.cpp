//
// Created by 杨孝鸿 on 2021-01-10.
//

#include "ImageProcess.h"

void ImageProcess::ImageDownsample(Mat &src, int time) {
    pyrDown(src, src, Size(src.cols / time, src.rows / time));
}

void ImageProcess::ImageUpsample(Mat &src, int time) {
    pyrUp(src, src, Size(src.cols * time, src.rows * time));
}

/*
	减色算法
*/
void ImageProcess::ColorReduce(Mat &image, int div) {
    int row = image.rows;
    int col = image.cols * image.channels();

    for (int i = 0; i < row; i++) {
        uchar *data = image.ptr<uchar>(i);
        for (int j = 0; j < col; j++) {
            data[j] = data[j] / div * div + div / 2;
        }
    }
}

/*
	椒盐噪声
	@param n，为噪声个数
*/
void ImageProcess::Salt(Mat &image, int n) {
    for (int k = 0; k < n; k++) {
        int i = rand() % image.rows;
        int j = rand() % image.cols;
        if (image.channels() == 1) {
            image.at<uchar>(i, j) = 255;
        } else if (image.channels() == 3) {
            image.at<Vec3b>(i, j)[0] = 255;
            image.at<Vec3b>(i, j)[1] = 255;
            image.at<Vec3b>(i, j)[2] = 255;
        }
    }
}

/*
	图像锐化
*/
void ImageProcess::Sharpen2D(Mat &image) {
    //构造核
    Mat kernel(3, 3, CV_32F, Scalar(0));
    //对核元素进行赋值
    kernel.at<float>(1, 1) = 5.0;
    kernel.at<float>(0, 1) = -1.0;
    kernel.at<float>(2, 1) = -1.0;
    kernel.at<float>(1, 0) = -1.0;
    kernel.at<float>(1, 2) = -1.0;
    filter2D(image, image, image.depth(), kernel);
}

void ImageProcess::PixelArt(Mat &image) {
    ImageDownsample(image, 2);
    ColorReduce(image, 64);
    ImageUpsample(image, 2);
    //cvtColor(image,image,CV_BGRA2GRAY);

    int size =(int) image.rows / 30;

    //先通道分离
    std::vector<Mat> channels;
    split(image, channels);//拆分
    ImageToPixelBlock(channels.at(0), size, 0);//蓝通道
    ImageToPixelBlock(channels.at(1), size, 1);//绿通道
    ImageToPixelBlock(channels.at(2), size, 2);//红通道
    merge(channels, image);
    //cvtColor(image,image,CV_GRAY2BGRA);
}

/*
	图像转为像素画
*/
void ImageProcess::ImageToPixelBlock(Mat &image, int size, int ch) {
    int row = image.rows;
    int col = image.cols * image.channels();

    for (int i = 0; i < row;) {
        //uchar *data = image.ptr<uchar>(i);
        for (int j = 0; j < col;) {
            int color = ImageBlockPixelMean(image, i, j, size);
//            if (ch == 1)
//                color = color * 0.6;
//            if (ch == 2)
//                color = color * 0.8;
            ImagePixelBlockColor(image, i, j, size, color);
            j += size;
        }
        i += size;
    }
}

/*
 * 求像素块色彩均值
 */
int ImageProcess::ImageBlockPixelMean(Mat &image, int x, int y, int size) {
    int res = 0;
    for (int i = x; i < x + size; i++) {
        if (i >= image.rows)
            return res;
        uchar *data = image.ptr<uchar>(i);
        for (int j = y; j < y + size; j++) {
            if (j >= image.cols * image.channels())
                return res / (i * j);
            res += data[j];
        }
    }
    return res / (size * size);
}

/*
 * 像素块重新上色
 */
void ImageProcess::ImagePixelBlockColor(Mat &image, int x, int y, int size, int color) {
    for (int i = x; i < x + size; i++) {
        if (i >= image.rows)
            return;
        uchar *data = image.ptr<uchar>(i);
        for (int j = y; j < y + size; j++) {
            if (j >= image.cols * image.channels())
                return;
            data[j] = color;
        }
    }
}


