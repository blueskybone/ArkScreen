/*
* Created by blueskybone on 2023/1/27.
*/
#pragma once
#ifndef CORE_H
#define CORE_H

#include <stdio.h>
#include <string.h>
#include <malloc.h>
#include <vector>
#include <iostream>
#include <android/bitmap.h>


#define BUFFER_LEN 20
#define BG_COLOR 0xff //white(todo: change to black)
#define TAG_NUM 29
#define BORDER_TOP 1
#define BORDER_BOTTOM 1
#define BORDER_LEFT 1
#define BORDER_RIGHT 1

typedef unsigned char uchar;

namespace arkscreen {

    enum threshType {
        THRESH_BINARY,
        THRESH_BINARY_INV
    };

    class Size {
    public:
        size_t width;
        size_t height;

        Size() {}

        Size(size_t w, size_t h) {
            width = w;
            height = h;
        }
    };

    class Point {
    public:
        size_t x;
        size_t y;

        Point() {}

        Point(size_t nx, size_t ny) {
            x = nx;
            y = ny;
        }

        bool equals(const Point &point) {
            return (x == point.x && y == point.y);
        }
    };

    class Rect {
    public:
        size_t x;
        size_t y;
        size_t width;
        size_t height;

        Rect() {}

        Rect(const size_t &n_x, const size_t &n_y, const size_t &n_width, const size_t &n_height) {
            x = n_x;
            y = n_y;
            width = n_width;
            height = n_height;
        }

        size_t area() const //todo: change func name to size()
        {
            return width * height;
        }

        bool equals(const Rect &rect) {
            return (rect.x == x && rect.y == y &&
                    rect.width == width && rect.height == height);
        }

        bool contains(const Point &point) {
            return (point.x >= x && point.x <= x + width &&
                    point.y >= y && point.y <= y + height);
        }
    };

    class Bitmap8 {
    public:
        size_t rows;
        size_t cols;
        size_t size;
        uchar *data;
        uchar *data_start;
        uchar *data_end;

        Bitmap8() {
            rows = 0;
            cols = 0;
            size = 0;
            data = nullptr;
            data_start = nullptr;
            data_end = nullptr;
        }

        //deep copy
        Bitmap8(const Bitmap8 &n_bitmap) {
            rows = n_bitmap.rows;
            cols = n_bitmap.cols;
            size = n_bitmap.size;
            data = (uchar *) malloc(sizeof(uchar) * n_bitmap.size + BUFFER_LEN);
            data_start = data;
            data_end = data + n_bitmap.size;
            memcpy(data, n_bitmap.data_start, n_bitmap.size);
        }

        void create(const size_t &width, const size_t &height) {
            rows = height;
            cols = width;
            size = width * height;
            data = (uchar *) malloc(sizeof(uchar) * size);
            data_start = data;
            data_end = data + size;
        }

        uchar *at(const size_t &row, const size_t &col) {
            return (uchar *) (data_start + row * cols + col);
        }

        void fillRect(const Rect &rect, const uchar color) {
            size_t x = rect.x;
            size_t y = rect.y;
            int i;
            for (i = 0; i < rect.height; i++) {
                memset(data + (y + i) * cols + x, color, rect.width);
            }
        }

        void copyRectTo(Bitmap8 &bitmap_rect, Rect rect) {
//            if (!isValid() || !dest.isValid()) {
//                LOGE("Invalid source or destination bitmap");
//                return;
//            }
//

            // 3. 检查矩形是否在源位图范围内
            if (rect.x < 0 || rect.y < 0 ||
                rect.x + rect.width > cols ||
                rect.y + rect.height > rows) {
//                LOGE("Rect out of bounds: (%d,%d %dx%d) vs bitmap (%dx%d)",
//                     rect.x, rect.y, rect.width, rect.height, cols, rows);
                return;
            }




            bitmap_rect.release();
            bitmap_rect.create(rect.width, rect.height);
            //int i = 0, j = 0;

            for (size_t i = 0; i < rect.height; i++) {
                memcpy(bitmap_rect.data_start + i * rect.width,
                       this->data_start + (i + rect.y) * this->cols + rect.x,
                       rect.width);
            }
        }

        void copyMakeBorderTo(Bitmap8 &dst_bitmap, int top, int bottom, int left, int right,
                              uchar color) {
            size_t cols = this->cols;
            size_t rows = this->rows;
            size_t width = cols + left + right;
            size_t height = rows + top + bottom;
            size_t row;
            dst_bitmap.create(width, height);
            uchar *data_ptr = dst_bitmap.data + width * top;
            //top
            dst_bitmap.fillRect(Rect(0, 0, width, top), BG_COLOR);
            //body
            for (row = 0; row < rows; row++) {
                memset(data_ptr, BG_COLOR, left);
                data_ptr += left;
                memcpy(data_ptr, this->data_start + row * cols, cols);
                data_ptr += cols;
                memset(data_ptr, BG_COLOR, right);
                data_ptr += right;
            }
            //bottom
            dst_bitmap.fillRect(Rect(0, rows + top, width, bottom), BG_COLOR);
        }

        void release() {
            free(data);
            data = NULL;
            data_start = NULL;
            data_end = NULL;
        }

        ~Bitmap8() {
            free(data);
            data = NULL;
            data_start = NULL;
            data_end = NULL;
        }
    };

    void ResizeNearest(Bitmap8 &src, Bitmap8 &dst, Size size);

    void ResizeLiner(Bitmap8 &src, Bitmap8 &dst, Size size);

    uchar Bin(size_t color, size_t thresh, threshType type);

    size_t DifferImage(uchar *data_ptr1, uchar *data_ptr2, size_t size);

    void Threshold(Bitmap8 &src, Bitmap8 &dst,
                   size_t thresh, threshType type);

    void
    AndroidBitmapToBitmap(Bitmap8 &dst_bitmap, AndroidBitmapInfo &src_info, uint32_t *src_data);

    namespace tag {
        void CutTagBorder(Bitmap8 &src_tag, Bitmap8 &des_tag, size_t num);

        void FindOneContourMoore(Bitmap8 &src_img, Point startPoint, int direct,
                                 std::vector<Point> &contours);

        Rect BoundingRect(std::vector<Point> &contours);

        void FindAllOuterRect(Bitmap8 &src_img, std::vector<Rect> &rectList);

        const char *GetTagText(Bitmap8 &img_tag, Bitmap8 &img_resize, FILE *fp);
    }
}
#endif