//
// Created by 杨孝鸿 on 2021-01-10.
//

#ifndef DAVINCI_IMAGEPROCESS_H
#define DAVINCI_IMAGEPROCESS_H

#include <opencv2/opencv.hpp>
#include <iostream>
#include <opencv2/imgproc/types_c.h>

using namespace std;
using namespace cv;

class ImageProcess {
public:
    ImageProcess() {};

    ~ImageProcess() {};

    void ImageDownsample(Mat &image, int time);

    void ImageUpsample(Mat &image, int time);

    void ColorReduce(Mat &image, int div);

    void Salt(Mat &image, int n);

    void Sharpen2D(Mat &image);

    void PixelArt(Mat &image);

    void ImageToPixelBlock(Mat &image, int size, int ch);

    int ImageBlockPixelMean(Mat &image, int x, int y, int size);

    void ImagePixelBlockColor(Mat &image, int x, int y, int size, int color);


};


#endif //DAVINCI_IMAGEPROCESS_H
