//
// Created by BlueSkyBone on 2023/1/27.
//
#include <iostream>
#include <vector>
#include <android/bitmap.h>

#include "utils.h"

const int direction[DIRECT_MAX][2] =
        {   { -1, -1 }, { 0, -1 },
            { 1, -1 }, { 1, 0 },
            { 1, 1 }, { 0, 1 },
            { -1, 1 }, { -1, 0 }    };
/*
 * Don't change the number of string's head or the order of strings.
 * They are as the same order as target_std.dat's data.
 * */
char tag[TAGNUM_MAX][20] =
        { "50_medic", "50_supporter", "50_caster", "50_guard",
          "50_vanguard", "50_defender", "50_sniper", "50_specialist",
          "90_top_operator", "85_sen_operator", "75_str_starter", "60_melee",
          "60_ranged", "10_DPS", "80_rbt_robot", "05_defense",
          "07_survival", "08_healing", "09_DP-Recovery", "06_aoe",
          "04_slow", "18_support", "20_Fast-Redeploy", "17_debuff",
          "19_shift", "29_nuker", "28_summon", "30_crowed-control" };
/*
 * Moore boundary following algorithm to find outer contour.
 * */
void findOneContourMoore(mBitmap8 & src_img, mPoint startPoint, int direct, std::vector<mPoint> & contours)
{
    mPoint point_iter = startPoint;
    int direct_iter = direct;
    contours.push_back(startPoint);
    do
    {
        for (int i = 0; i < DIRECT_MAX; i++)
        {
            direct_iter = (direct_iter + 1) % DIRECT_MAX;
            int x = point_iter.x + direction[direct_iter][0];
            int y = point_iter.y + direction[direct_iter][1];
            uchar color = *(src_img.at(y, x));
            if (color == 0)
            {
                contours.emplace_back(x, y);
                point_iter.x = x;
                point_iter.y = y;
                direct_iter = (direct_iter + DIRECT_MAX - 3) % DIRECT_MAX;
                break;
            }
        }
    } while (!point_iter.equals(startPoint));
}

mRect BoundingRect(std::vector<mPoint> & contours)
{
    int min_x = INT_MAX;
    int min_y = INT_MAX;
    int max_x = INT_MIN;
    int max_y = INT_MIN;
    int i = 0;
    for (i = 0; i < contours.size(); i++)
    {
        mPoint point = mPoint(contours[i].x, contours[i].y);
        if (point.x < min_x)
        {
            min_x = point.x;
        }
        if (point.x > max_x)
        {
            max_x = point.x;
        }
        if (point.y < min_y)
        {
            min_y = point.y;
        }
        if (point.y > max_y)
        {
            max_y = point.y;
        }
    }
    mRect rect(min_x, min_y, max_x - min_x + 1, max_y - min_y + 1);
    return rect;
}

void findAllOuterRect(mBitmap8 & src_img, std::vector<mRect>& rectList)
{
    uchar * data_ptr_work;
    uchar * data_ptr_follow;
    int cnt = 1;

    std::vector<mPoint> contours;

    data_ptr_work = src_img.datastart + 1;
    data_ptr_follow = src_img.datastart;

    while (data_ptr_work != src_img.dataend)
    {
        /*
        src_img have padding using background at least 1 pixel, it doesn't matter
        even if data_ptr_work & data_ptr_follow aren't at the same line.
        */
        uchar ch1 = *data_ptr_work;
        uchar ch2 = *data_ptr_follow;

        if (ch1 == 0 && ch2 == 255) // start point
        {
            int row = cnt / src_img.cols;
            int col = cnt % src_img.cols;

            findOneContourMoore(src_img, mPoint(col, row), KERNEL_DIRECT, contours);
            mRect rect = BoundingRect(contours);
            src_img.fillRect(rect, BG_COLOR);
            if (contours.size() > THRESHOLD &&
                rect.width > MIN_WIDTH &&
                rect.height>MIN_HEIGHT )
            {
                rect.x -= BORDER_LEFT;
                rect.y -= BORDER_TOP;
                rectList.push_back(rect);
            }
            contours.clear();
        }
        data_ptr_work++;
        data_ptr_follow++;
        cnt++;
    }
    /*
    int i, j;
    for (i = 0; i < src_img.rows; i++)
    {
        for (j = 1; j < src_img.cols; j++)
        {
            uchar k1 = *(src_img.at(i, j));
            uchar k2 = *(src_img.at(i, j - 1));
            if (k1 == 0 && k2 == 255)// start point
            {
                findOneContourMoore(src_img, mPoint(j, i), KERNEL_DIRECT, contours);
                mRect rect = mBoundingRect(contours);
                src_img.fillRect(rect, BG_COLOR);
                if (contours.size() > THRESHOLD)
                {
                    rect.x -= BORDER_LEFT;
                    rect.y -= BORDER_TOP;
                    rectList.push_back(rect);
                }
                contours.clear();
            }
        }
    }
    */
}

/*
 * Nearest Resize is fit to process binary images. Maybe.
 * */
void ResizeNearest(mBitmap8 & src_img, mBitmap8 & dst_img, mSize size)
{
    int i = 0, j = 0;
    int src_x = 0, src_y = 0;
    int dst_width = size.width, dst_height = size.height;
    int src_width = src_img.cols, src_height = src_img.rows;
    dst_img.release();
    dst_img.create(dst_width, dst_height);

    for (i = 0; i < dst_height; i++)
    {
        for (j = 0; j < dst_width; j++)
        {
            src_x = j * src_width / dst_width;
            src_y = i * src_height / dst_height;
            *(dst_img.at(i, j)) = *(src_img.at(src_y, src_x));
        }
    }
}

/*
 * de-noise. sometimes the border is not square,
 * caused by Arknights' main face rendering with dust effect.
 * */
void cutTagBorder(mBitmap8 & src_tag, mBitmap8 & dst_tag, int num)
{
    mRect rc;
    rc.x = 0, rc.y = 0, rc.width = src_tag.cols, rc.height = src_tag.rows;
    int rows = src_tag.rows;
    int cols = src_tag.cols;
    int row = 0, col = 0;
    // top
    for (row = 0; row < rows; row++)
    {
        int sum = 0;
        for (col = 0; col < cols; col++)
        {
            if (*(src_tag.at(row, col)) == 0)
                sum++;
        }
        if (sum * 2>cols)
        {
            rc.y = row;
            break;
        }
    }
    // bottom
    for (row = rows - 1; row >= 0; row--)
    {
        int sum = 0;
        for (col = 0; col < cols; col++)
        {
            if (*(src_tag.at(row, col)) == 0)
                sum++;
        }
        if (sum * 3>cols)
        {
            rc.height = rc.height - rows + 1 + row - rc.y;
            break;
        }
    }
    //left
    for (col = 0; col < cols; col++)
    {
        int sum = 0;
        for (row = 0; row < rows; row++)
        {
            if (*(src_tag.at(row, col)) == 0)
                sum++;
        }
        if (sum * 2>rows)
        {
            rc.x = col;
            break;
        }
    }
    //right
    for (col = cols - 1; col >= 0; col--)
    {
        int sum = 0;
        for (row = 0; row < rows; row++)
        {
            if (*(src_tag.at(row, col)) == 0)
                sum++;
        }
        if (sum * 2>rows)
        {
            rc.width = rc.width - cols + 1 + col - rc.x;
            break;
        }
    }
    rc.height -= num;
    src_tag.copyRectTo(dst_tag, rc);
}

size_t Sum(uchar * data_ptr1, uchar * data_ptr2, size_t size)
{
    size_t cnt = 0;
    for (int i = 0; i < size; i++)
    {
        if (!(*data_ptr1) && (*data_ptr2))
        {
            cnt++;
        }
        data_ptr1++;
        data_ptr2++;
    }
    return cnt;
}

char* getTagText(mBitmap8 & img_tag, mBitmap8 & img_resize, FILE *fp)
{
    size_t min = MAX_VALUE;
    int final_num = 0;
    uchar * stdData;
    for (int i = 0; i < TAGNUM_MAX; i++)
    {
        int num = 30;
        int rows = 0;
        int cols = 0;

        fread(&num, sizeof(int), 1, fp);
        fread(&rows, sizeof(int), 1, fp);
        fread(&cols, sizeof(int), 1, fp);

        stdData = (uchar *)malloc(sizeof(uchar)*cols*rows);
        fread(stdData, sizeof(uchar), rows*cols, fp);

        ResizeNearest(img_tag, img_resize, mSize(cols, rows));
        uchar * stdData_temp = stdData;
        size_t val = Sum(stdData_temp, img_resize.datastart, rows*cols);

        if (val < min)
        {
            min = val;
            final_num = num;
        }
        img_resize.release();
        free(stdData);
        stdData = nullptr;
    }
    return tag[final_num];
}

void AndroidBitmap2mBitmap(mBitmap8 & dst_bitmap, AndroidBitmapInfo & src_info, uint32_t* src_data)
{
    int cols = src_info.width;
    int rows = src_info.height;
    dst_bitmap.release();
    dst_bitmap.create(cols,rows);

    uchar * data_dst_ptr = dst_bitmap.datastart;
    uint32_t* data_src_ptr = src_data;

    uint src_color;
    uint red, green, blue;
    uchar dst_color;
    for(size_t i = 0; i < cols * rows; i++ )
    {
        src_color = *data_src_ptr;
        red = (src_color & 0x00ff0000) >> 16;
        green = (src_color & 0x0000ff00) >> 8;
        blue = src_color & 0x000000ff;
        src_color =(uint)( red * 0.3 + green * 0.59 + blue * 0.11);
        if (src_color <= 120)
        {
            dst_color = 0;
        }
        else
        {
            dst_color = 255;
        }
        *data_dst_ptr = dst_color;
        data_src_ptr++;
        data_dst_ptr++;
    }
}



