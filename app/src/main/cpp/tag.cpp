/*
* Created by blueskybone on 2023/1/27.
*/
#include "core.h"

#define DIRECT_MAX 8
#define KERNEL_DIRECT 7
#define BG_COLOR 0xff //TODO:change to black
#define THRESHOLD 250
#define MIN_HEIGHT 20
#define MIN_WIDTH 100

namespace arkscreen {
    const int direction[DIRECT_MAX][2] =
            {{-1, -1},
             {0,  -1},
             {1,  -1},
             {1,  0},
             {1,  1},
             {0,  1},
             {-1, 1},
             {-1, 0}};

    /*
    * Don't change tags' order in tag_en and tag_cn.
    * They are as the same order as target_std.dat's data.
    * */
    const char tag_en[TAG_NUM][20] =
            {"medic", "supporter", "caster", "guard",
             "vanguard", "defender", "sniper", "specialist",
             "top-ope", "sen-ope", "starter", "melee",
             "ranged", "dps", "robot", "defense",
             "survival", "healing", "dp-recovery", "aoe",
             "slow", "support", "fast-redeploy", "debuff",
             "shift", "nuker", "summon", "crowed-control","elemental"};

//    const char tag_cn[TAG_NUM][20] =
//            {"医疗干员", "辅助干员", "术师干员", "近卫干员", "先锋干员",
//             "重装干员", "狙击干员", "特种干员", "高级资深干员", "资深干员", "新手", "近战位",
//             "远程位", "输出", "支援机械", "防护", "生存", "治疗", "费用回复", "群攻",
//             "减速", "支援", "快速复活", "削弱", "位移", "爆发", "召唤", "控场","元素"};


    void tag::CutTagBorder(Bitmap8 &src, Bitmap8 &dst, size_t num) {
        Rect rc;
        rc.x = 0, rc.y = 0, rc.width = src.cols, rc.height = src.rows;
        size_t rows = src.rows;
        size_t cols = src.cols;
        size_t row, col;
        // top
        for (row = 0; row < rows; row++) {
            int sum = 0;
            for (col = 0; col < cols; col++) {
                if (*(src.at(row, col)) == 0)
                    sum++;
            }
            if (sum * 2 > cols) {
                rc.y = row;
                break;
            }
        }
        // bottom
        for (row = rows - 1; row >= 0; row--) {
            int sum = 0;
            for (col = 0; col < cols; col++) {
                if (*(src.at(row, col)) == 0)
                    sum++;
            }
            if (sum * 3 > cols) {
                rc.height = rc.height - rows + 1 + row - rc.y;
                break;
            }
        }
        //left
        for (col = 0; col < cols; col++) {
            int sum = 0;
            for (row = 0; row < rows; row++) {
                if (*(src.at(row, col)) == 0)
                    sum++;
            }
            if (sum * 2 > rows) {
                rc.x = col;
                break;
            }
        }
        //right
        for (col = cols - 1; col >= 0; col--) {
            int sum = 0;
            for (row = 0; row < rows; row++) {
                if (*(src.at(row, col)) == 0)
                    sum++;
            }
            if (sum * 2 > rows) {
                rc.width = rc.width - cols + 1 + col - rc.x;
                break;
            }
        }
        rc.height -= num;
        src.copyRectTo(dst, rc);
    }

    void tag::FindOneContourMoore(Bitmap8 &src, Point startPoint, int direct,
                                  std::vector<Point> &contours) {
        Point point_iter = startPoint;
        int direct_iter = direct;
        contours.emplace_back(startPoint);
        do {
            for (int i = 0; i < DIRECT_MAX; i++) {
                direct_iter = (direct_iter + 1) % DIRECT_MAX;
                size_t x = point_iter.x + direction[direct_iter][0];
                size_t y = point_iter.y + direction[direct_iter][1];
                uchar color = *(src.at(y, x));
                if (color == 0) {
                    contours.emplace_back(x, y);
                    point_iter.x = x;
                    point_iter.y = y;
                    direct_iter = (direct_iter + DIRECT_MAX - 3) % DIRECT_MAX;
                    break;
                }
            }
        } while (!point_iter.equals(startPoint));
    }

    void tag::FindAllOuterRect(Bitmap8 &src, std::vector<Rect> &rectList) {
        uchar *data_ptr_work;
        uchar *data_ptr_follow;
        size_t cnt = 1;

        std::vector<Point> contours;

        data_ptr_work = src.data_start + 1;
        data_ptr_follow = src.data_start;

        while (data_ptr_work != src.data_end) {
            /*
            src_img have padding using background at least 1 pixel, it doesn't matter
            even if data_ptr_work & data_ptr_follow aren't at the same line.
            */
            uchar ch1 = *data_ptr_work;
            uchar ch2 = *data_ptr_follow;

            if (ch1 == 0 && ch2 == 255) // start point
            {
                size_t row = cnt / src.cols;
                size_t col = cnt % src.cols;

                FindOneContourMoore(src, Point(col, row), KERNEL_DIRECT, contours);
                Rect rect = BoundingRect(contours);
                src.fillRect(rect, BG_COLOR);
                if (contours.size() > THRESHOLD &&
                    rect.width > MIN_WIDTH &&
                    rect.height > MIN_HEIGHT) {
                    rect.x -= BORDER_LEFT;
                    rect.y -= BORDER_TOP;
                    rectList.emplace_back(rect);
                }
                contours.clear();
            }
            data_ptr_work++;
            data_ptr_follow++;
            cnt++;
        }
    }

    Rect tag::BoundingRect(std::vector<Point> &contours) {
        size_t min_x = INT_MAX;
        size_t min_y = INT_MAX;
        size_t max_x = 0;
        size_t max_y = 0;
        for (auto &contour: contours) {
            Point point = Point(contour.x, contour.y);
            if (point.x < min_x) {
                min_x = point.x;
            }
            if (point.x > max_x) {
                max_x = point.x;
            }
            if (point.y < min_y) {
                min_y = point.y;
            }
            if (point.y > max_y) {
                max_y = point.y;
            }
        }
        Rect rect(min_x, min_y, max_x - min_x + 1, max_y - min_y + 1);
        return rect;
    }

    const char *tag::GetTagText(Bitmap8 &img_tag, Bitmap8 &img_resize, FILE *fp) {
        size_t min = 100000;
        size_t final_num = 0;
        uchar *stdData;

        for (int i = 0; i < TAG_NUM; i++) {
            int num = 30;
            int rows = 0;
            int cols = 0;

            fread(&num, sizeof(int), 1, fp);
            fread(&rows, sizeof(int), 1, fp);
            fread(&cols, sizeof(int), 1, fp);

            stdData = (uchar *) malloc(sizeof(uchar) * cols * rows);
            fread(stdData, sizeof(uchar), cols * rows, fp);
            //ResizeNearest(img_tag, img_resize, Size(cols, rows));
            ResizeLiner(img_tag, img_resize, Size(cols, rows));
            uchar *stdData_temp = stdData;
            size_t val = DifferImage(stdData_temp, img_resize.data_start, rows * cols);

            if (val < min) {
                min = val;
                final_num = num;
            }
            img_resize.release();
            free(stdData);
            stdData = nullptr;
        }
        return tag_en[final_num];
    }
}