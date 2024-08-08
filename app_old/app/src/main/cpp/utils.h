//
// Created by BlueSkyBone on 2023/1/27.
//
#include <stdio.h>
#ifndef ARKSCREEN_UTILS_H
#define ARKSCREEN_UTILS_H

#define BUFFER_LEN 20
#define DIRECT_MAX 8
#define KERNEL_DIRECT 7
#define BG_COLOR 0xff
#define THRESHOLD 250
#define MIN_HEIGHT 20
#define MIN_WIDTH 100
#define TAGNUM_MAX 28
#define MAX_VALUE 1000000

#define BORDER_TOP 1
#define BORDER_BOTTOM 1
#define BORDER_LEFT 1
#define BORDER_RIGHT 1

#define uchar unsigned char

class mSize
{
public:
    int width;
    int height;
    mSize()
    {

    }
    mSize(int n_width, int n_height)
    {
        width = n_width;
        height = n_height;
    }
};

class mPoint
{
public:
    int x;
    int y;

    mPoint()
    {

    }

    mPoint(int n_x, int n_y)
    {
        x = n_x;
        y = n_y;
    }

    bool equals(const mPoint & n_point)
    {
        if (x == n_point.x && y == n_point.y)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
};

class mRect
{
public:
    int x;
    int y;
    int width;
    int height;
    mRect()
    {

    }

    mRect(int n_x, int n_y, int n_width, int n_height)
    {
        x = n_x;
        y = n_y;
        width = n_width;
        height = n_height;
    }

    size_t area()const
    {
        return width * height;
    }

    bool equals(const mRect & rect)
    {
        if (rect.x == x&& rect.y == y&&
            rect.width == width&&rect.height == height)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    bool contains(const mPoint & point)
    {
        if (point.x >= x && point.x <= x + width &&
            point.y >= y && point.y <= y + height)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
};

class mBitmap8
{
public:
    int rows;
    int cols;
    size_t size;
    uchar * data;
    uchar * datastart;
    uchar * dataend;

    mBitmap8()
    {
        rows = 0;
        cols = 0;
        size = 0;
        data = nullptr;
        datastart = nullptr;
        dataend = nullptr;
    }
    //deep copy
    mBitmap8(const mBitmap8 & n_bitmap)
    {
        rows = n_bitmap.rows;
        cols = n_bitmap.cols;
        size = n_bitmap.size;
        data = (uchar *)malloc(sizeof(uchar)*n_bitmap.size + BUFFER_LEN);
        datastart = data;
        dataend = data + n_bitmap.size;
        memcpy(data, n_bitmap.datastart, n_bitmap.size);
    }

    void create(int width, int height)
    {
        rows = height;
        cols = width;
        size = width*height;
        data = (uchar *)malloc(sizeof(uchar)*size);
        datastart = data;
        dataend = data + size;
    }
    /*
    void create(mRect rect)
    {
    rows = rect.height;
    cols = rect.width;
    size = rows*cols;
    data = (uchar *)malloc(sizeof(uchar)*size + BUFFER_LEN);
    dataStart = data;
    dataend = data + size;
    }
    */

    uchar* at(int row, int col)
    {
        return (uchar*)(datastart + row * cols + col);
    }
    void fillRect(mRect rect, uchar color)
    {
        int x = rect.x;
        int y = rect.y;
        int i;
        for (i = 0; i < rect.height; i++)
        {
            memset(data + (y + i) * cols + x, color, rect.width);
        }
    }
    void copyRectTo(mBitmap8 & bitmap_rect, mRect rect)
    {
        bitmap_rect.release();
        bitmap_rect.create(rect.width, rect.height);
        int i = 0, j = 0;
        for (i = 0; i < rect.height; i++)
        {
            memcpy(bitmap_rect.datastart + i*rect.width,
                   this->datastart + (i + rect.y)*this->cols + rect.x,
                   rect.width);
        }
    }

    void copyMakeBorderTo(mBitmap8 &dst_bitmap, int top, int bottom, int left, int right, uchar color)
    {
        int cols = this->cols;
        int rows = this->rows;
        int width = cols + left + right;
        int height = rows + top + bottom;
        int row;
        dst_bitmap.create(width, height);
        uchar * data_ptr = dst_bitmap.data + width * top;
        //top
        dst_bitmap.fillRect(mRect(0, 0, width, top), BG_COLOR);
        //body
        for (row = 0; row < rows; row++)
        {
            memset(data_ptr, BG_COLOR, left);
            data_ptr += left;
            memcpy(data_ptr, this->datastart + row * cols, cols);
            data_ptr += cols;
            memset(data_ptr, BG_COLOR, right);
            data_ptr += right;
        }
        //bottom
        dst_bitmap.fillRect(mRect(0, rows + top, width, bottom), BG_COLOR);
    }

    void release()
    {
        free(data);
        data = NULL;
        datastart = NULL;
        dataend = NULL;
    }
    ~mBitmap8()
    {
        free(data);
        data = NULL;
        datastart = NULL;
        dataend = NULL;
    }
};

enum threshType
{
    THRESH_BINARY,
    THRESH_BINARY_INV
};

void cutTagBorder(mBitmap8 & src_tag, mBitmap8 & des_tag, int num);

void findOneContourMoore(mBitmap8 & src_img, mPoint startPoint, int direct, std::vector<mPoint> & contours);

mRect BoundingRect(std::vector<mPoint> & contours);

void findAllOuterRect(mBitmap8 & src_img, std::vector<mRect>& rectList);

void ResizeNearest(mBitmap8 & src_img, mBitmap8 & dst_img, mSize size);

char* getTagText(mBitmap8 & img_tag, mBitmap8 & img_resize, FILE *fp);

size_t Sum(uchar * data_ptr1, uchar * data_ptr2, size_t size);

void AndroidBitmap2mBitmap(mBitmap8 & dst_bitmap, AndroidBitmapInfo & src_info, uint32_t* src_data);

#endif //ARKSCREEN_UTILS_H
