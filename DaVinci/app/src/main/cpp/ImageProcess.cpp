//
// Created by 杨孝鸿 on 2021-01-10.
//

#include "ImageProcess.h"

Mat ImageProcess::ImageDownsample(Mat &src, int time) {
    Mat dst;
    pyrDown(src, dst, Size(src.cols / time, src.rows / time));
    return dst;
}

Mat ImageProcess::ImageUpsample(Mat &src, int time) {
    Mat dst;
    pyrUp(src, dst, Size(src.cols * time, src.rows * time));
    return dst;
}

/*
	减色算法
*/
void ImageProcess::ColorReduce(Mat &image, int div)
{
    int row = image.rows;
    int col = image.cols * image.channels();

    for (int i = 0; i < row; i++)
    {
        uchar *data = image.ptr<uchar>(i);
        for (int j = 0; j < col; j++)
        {
            data[j] = data[j] / div * div + div / 2;
        }
    }
}

/*
	椒盐噪声
	@param n，为噪声个数
*/
void ImageProcess::Salt(Mat &image, int n)
{
    for (int k = 0; k<n; k++)
    {
        int i = rand() % image.rows;
        int j = rand() % image.cols;
        if (image.channels() == 1)
        {
            image.at<uchar>(i, j) = 255;
        }
        else if (image.channels() == 3)
        {
            image.at<Vec3b>(i, j)[0] = 255;
            image.at<Vec3b>(i, j)[1] = 255;
            image.at<Vec3b>(i, j)[2] = 255;
        }
    }
}

/*
	图像锐化
*/
Mat ImageProcess::Sharpen2D(Mat &image)
{
    Mat result;
    //构造核
    Mat kernel(3, 3, CV_32F, Scalar(0));
    //对核元素进行赋值
    kernel.at<float>(1, 1) = 5.0;
    kernel.at<float>(0, 1) = -1.0;
    kernel.at<float>(2, 1) = -1.0;
    kernel.at<float>(1, 0) = -1.0;
    kernel.at<float>(1, 2) = -1.0;
    filter2D(image, result, image.depth(), kernel);
    return result;
}

void ImageProcess::PixelArt(Mat &image){
    //Mat result;
    //result = ImageDownsample(image, 8);
    //ColorReduce(result, 64);
    //result = ImageUpsample(image, 8);
    cvtColor(image,image,CV_BGRA2GRAY);
    cvtColor(image,image,CV_GRAY2BGRA);

}

//像素处理  指针最快

//int rowNumber = image.rows;//行数
//int colNumber = image.cols * image.channels();//每一行元素个数 = 列数 x 通道数
//for (int i = 0; i < rowNumber; i++)//行循环
//{
//uchar* data = image.ptr<uchar>(i);//获取第i行的首地址
//for (int j = 0; j < colNumber; j++)//列循环
//{
////开始处理
//data[j] = 255;
//}
//}

