/*
* Created by blueskybone on 2023/1/27.
*/
#include "core.h"

namespace arkscreen {
    inline uchar Bin(size_t color, size_t thresh, threshType type) {
        if ((color > thresh && type == THRESH_BINARY)
            || (color > thresh && type == THRESH_BINARY_INV))
            return 255;
        else return 0;
    }

    void Threshold(Bitmap8 &src, Bitmap8 &dst,
                   size_t thresh, threshType type) {
        size_t dst_width = src.cols, dst_height = src.rows;

        dst.release();
        dst.create(dst_width, dst_height);

        for (size_t i = 0; i < dst_height; i++) {
            for (size_t j = 0; j < dst_width; j++) {
                *(dst.at(i, j)) = Bin(*(dst.at(i, j)), thresh, type);
            }
        }
    }

    //faster, less accurate
    void ResizeNearest(Bitmap8 &src, Bitmap8 &dst, Size size) {
        size_t src_x = 0, src_y = 0;
        size_t dst_width = size.width, dst_height = size.height;
        size_t src_width = src.cols, src_height = src.rows;

        dst.release();
        dst.create(dst_width, dst_height);
        for (size_t i = 0; i < dst_height; i++) {
            for (size_t j = 0; j < dst_width; j++) {
                src_x = j * src_width / dst_width;
                src_y = i * src_height / dst_height;
                *(dst.at(i, j)) = *(src.at(src_y, src_x));
            }
        }
    }


    //more accurate
    void ResizeLiner(Bitmap8 &src, Bitmap8 &dst, Size size) {
        double src_x , src_y;
        double dst_width = size.width, dst_height = size.height;
        double src_width = src.cols, src_height = src.rows;

        dst.release();
        dst.create(size.width, size.height);

        for (size_t i = 0; i < dst_height; i++) {
            for (size_t j = 0; j < dst_width; j++) {
                src_x = (j + 0.5) * src_width / dst_width - 0.5;
                src_y = (i + 0.5) * src_height / dst_height - 0.5;
                if (src_x < 0) src_x = 0;
                if (src_y < 0) src_y = 0;
                double src_x_1 = floor(src_x);
                double src_x_2 = src_x_1 + 1;
                double src_y_1 = floor(src_y);
                double src_y_2 = src_y_1 + 1;
                size_t px = (src_x_2 - src_x) * (src_y_2 - src_y) * (*(src.at(src_y_1, src_x_1))) +
                            (src_x - src_x_1) * (src_y - src_y_1) * (*(src.at(src_y_2, src_x_2))) +
                            (src_x_2 - src_x) * (src_y - src_y_1) * (*(src.at(src_y_2, src_x_1))) +
                            (src_x - src_x_1) * (src_y_2 - src_y) * (*(src.at(src_y_1, src_x_2)));
                *(dst.at(i, j)) = Bin(px, 127, THRESH_BINARY);
            }
        }
    }

    size_t DifferImage(uchar *data_ptr1, uchar *data_ptr2, size_t size) {
        size_t cnt = 0;
        for (int i = 0; i < size; i++) {
            if (!(*data_ptr1) && (*data_ptr2))
                cnt++;
            data_ptr1++;
            data_ptr2++;
        }
        return cnt;
    }

    void
    AndroidBitmapToBitmap(Bitmap8 &dst_bitmap, AndroidBitmapInfo &src_info, uint32_t *src_data) {
        size_t cols = src_info.width;
        size_t rows = src_info.height;
        dst_bitmap.release();
        dst_bitmap.create(cols, rows);

        uchar *data_dst_ptr = dst_bitmap.data_start;
        uint32_t *data_src_ptr = src_data;

        uint src_color;
        uint red, green, blue;
        uchar dst_color;
        for (size_t i = 0; i < cols * rows; i++) {
            src_color = *data_src_ptr;
            red = (src_color & 0x00ff0000) >> 16;
            green = (src_color & 0x0000ff00) >> 8;
            blue = src_color & 0x000000ff;
            //opencv use BGR to Gray
            src_color = (uint) (red * 0.114 + green * 0.587 + blue * 0.299);
            //src_color = (uint) (red * 0.299 + green * 0.587 + blue * 0.114);
            if (src_color <= 120) {
                dst_color = 0;
            } else {
                dst_color = 255;
            }
            *data_dst_ptr = dst_color;
            data_src_ptr++;
            data_dst_ptr++;
        }
    }
}