package com.godot17.arksc.activity;

import static com.godot17.arksc.utils.NetworkUtils.doAttendance;
import static com.godot17.arksc.utils.NetworkUtils.getBindingInfoWith;
import static com.godot17.arksc.utils.NetworkUtils.getCredByGrant;
import static com.godot17.arksc.utils.NetworkUtils.getGameInfoStream;
import static com.godot17.arksc.utils.NetworkUtils.getGrantCodeByToken;
import static com.godot17.arksc.utils.NetworkUtils.getJsonContent;
import static com.godot17.arksc.utils.PrefManager.getAutoSign;
import static com.godot17.arksc.utils.PrefManager.getToken;
import static com.godot17.arksc.utils.PrefManager.getUserInfo;
import static com.godot17.arksc.utils.PrefManager.setSignItem;
import static com.godot17.arksc.utils.Utils.convertSec2DayHourMin;
import static com.godot17.arksc.utils.Utils.showToast;

import static java.lang.Math.max;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.godot17.arksc.R;
import com.godot17.arksc.datautils.GameInfo;
import com.godot17.arksc.utils.LoadingDialog;

import org.w3c.dom.Text;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.util.zip.GZIPInputStream;

public class SklandActivity extends Activity {


    private final String TAG = "SklandActivity";
    private LoadingDialog loadingDialog;
    private Handler mHandler;
    private TextView text_info;
    private TextView text_ap;
    private TextView text_ap_max;
    private TextView text_ap_recover;
    private TextView text_recruit_ts;
    private TextView text_recruit_value;
    private TextView text_recruit_max;
    private TextView text_hire_time;
    private TextView text_hire_cnt;
    private TextView text_train_status;
    private TextView text_trainer;
    private TextView text_labor_value;
    private TextView text_manufactures_status;
    private TextView text_trading_status;
    private TextView text_dormitories_value;
    private TextView text_tired;
    private TextView text_clue;
    private TextView text_campaign;
    private TextView text_routine_day;
    private TextView text_routine_week;
    private TextView text_low_item;
    private TextView text_high_item;


    /**
     * TODO:
     * 顺便把干员信息一起载入了。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadingDialog = new LoadingDialog(this);
        setLayout();
        mHandler = new Handler();
        LoadingData();
    }

    /**
     * TODO:
     * 改下逻辑，把cred和uid和gameId信息保存在本地，
     * 出问题了再从Toekn请求。
     * 现在这样直接从token请求，代码上少一层exception，但是实机体感上速度稍微慢点。
     */
    private void LoadingData() {
        loadingDialog.show();
        new Thread(() -> {
            String token = getToken(this);
            if (token.equals("")) {
                loadingDialog.dismiss();
                showToast(this, "未登录状态");
                finish();
                return;
            }
            try {
                //get grant
                String code = getGrantCodeByToken(token);
                if (code == null) {
                    loadingDialog.dismiss();
                    showToast(this, "error with getGrantCode");
                    finish();
                    return;
                } else if (code.equals("UNAUTHORIZED")) {
                    loadingDialog.dismiss();
                    showToast(this, "Token已过期，请重新登录");
                    finish();
                    return;
                }

                //get cred
                String cred = getCredByGrant(code);
                if (cred == null) {
                    loadingDialog.dismiss();
                    showToast(this, "error with getCred");
                    finish();
                    return;
                }

                //get uid and gameId
                String uid = getBindingInfoWith(cred, "uid");
                String channelMasterId = getBindingInfoWith(cred, "channelMasterId");
                if (uid == null) {
                    loadingDialog.dismiss();
                    showToast(this, "error with getUid");
                    finish();
                    return;
                }

                getGameInfo(cred, uid);
                if (getAutoSign(this)) {
                    if (channelMasterId == null) {
                        showToast(this, "error with getChannelMasterId");
                        finish();
                    }
                    doAutoSigh(cred, uid, channelMasterId);
                }
            } catch (Exception e) {
                Log.e(TAG, "error with", e);
            }
            //finish();
        }).start();
    }

    private void doAutoSigh(String cred, String uid, String channelId) throws MalformedURLException {
        String resp = doAttendance(cred, uid, channelId);
        if (resp == null) {
            loadingDialog.dismiss();
            showToast(this, "签到失败");
        } else {
            String code = getJsonContent(resp, "code");
            if (code == null) {
                loadingDialog.dismiss();
                showToast(this, "签到失败");
            } else {
                if (code.equals("10001")) {
                    loadingDialog.dismiss();

                    showToast(this, "签到失败：" + getJsonContent(resp, "message"));
                } else if (code.equals("0")) {
                    loadingDialog.dismiss();
                    String item = getJsonContent(resp, "name").replace("\"", "")
                            + "×"
                            + getJsonContent(resp, "count").replace("\"", "");
                    setSignItem(this, item);
                    showToast(this, "签到成功：" + item);

                } else {
                    loadingDialog.dismiss();
                    showToast(this, "签到失败：" + code);
                }
            }
        }
    }

    private void getGameInfo(String cred, String uid) throws IOException {
        Log.e(TAG, "cred  " + cred);
        Log.e(TAG, "uid  " + uid);
        InputStream is = getGameInfoStream(cred, uid);

        if (is == null) {
            loadingDialog.dismiss();
            showToast(this, "获取数据失败");
            return;
        }
        GZIPInputStream gzip = new GZIPInputStream(is);
        ObjectMapper om = new ObjectMapper();
        JsonNode tree = om.readTree(gzip);

        //info
        GameInfo.info.nickName = tree.at("/data/status/name").toString().replace("\"", "");
        GameInfo.info.level = tree.at("/data/status/level").asInt();
        GameInfo.info.progress = tree.at("/data/status/mainStageProgress").toString().replace("\"", "");
        //Ap
        //分情况
        //如果currentAp本身大于max（一般来说recoverTime  == -1）,直接取current.
        //然后正常计算自然恢复理智
        //如果currentTS > recoverTs, 取max
        //如果currentTs < recoverTS, 取计算值。
        int currentTs = tree.at("/data/currentTs").asInt();
        int ap_current = tree.at("/data/status/ap/current").asInt();
        int ap_max = tree.at("/data/status/ap/max").asInt();
        int ap_lastApAddTime = tree.at("/data/status/ap/lastApAddTime").asInt();
        int ap_recover = tree.at("/data/status/ap/completeRecoveryTime").asInt();
        GameInfo.ap.max = ap_max;
        if (ap_recover == -1) {
            GameInfo.ap.current = ap_current;
            GameInfo.ap.recoverTime = -1;
        } else if (ap_recover < currentTs) {
            GameInfo.ap.current = ap_max;
            GameInfo.ap.recoverTime = -1;
        } else {
            GameInfo.ap.current = (currentTs - ap_lastApAddTime) / (60 * 6) + ap_current;
            GameInfo.ap.recoverTime = (ap_recover - currentTs);
        }
        //train
        //charInfoMap
        //专精完成 "remainSecs": 0,
        //无专精 remainSecs": -1,
        JsonNode node_train = tree.at("/data/building/training");
        JsonNode node_char = tree.at("/data/charInfoMap");
        GameInfo.train.isNull = node_train.isNull();
        if (!GameInfo.train.isNull) {
            JsonNode trainee = node_train.get("trainee");
            GameInfo.train.traineeIsNull = trainee.isNull();
            if (!GameInfo.train.traineeIsNull) {
                String traineeCode = node_train.get("trainee").get("charId")
                        .toString()
                        .replace("\"", "");
                Log.e(TAG, traineeCode);
                GameInfo.train.trainee = node_char.get(traineeCode).get("name")
                        .toString()
                        .replace("\"", "");
                Log.e(TAG, GameInfo.train.trainee);
                GameInfo.train.status = trainee.get("targetSkill").asInt();
            }
            GameInfo.train.time = node_train.get("remainSecs").asInt();
        }
        //Recruit
        GameInfo.recruit.isNull = tree.at("/data/recruit").isNull();
        if (!GameInfo.recruit.isNull) {
            int unable = 0;
            int complete = 0;
            int finishTs = -1;
            for (int i = 0; i < 4; i++) {
                JsonNode node = tree.at("/data/recruit").get(i);
                int state = node.get("state").asInt();
                switch (state) {
                    case 0:
                        unable++;
                        break;
                    case 3:
                        complete++;
                        break;
                    case 2:
                        finishTs = max(node.get("finishTs").asInt(), finishTs);
                        break;
                }
            }
            if (finishTs == -1 || finishTs < currentTs) {
                GameInfo.recruit.time = -1;
            } else {
                GameInfo.recruit.time = (finishTs - currentTs);
            }
            GameInfo.recruit.max = 4 - unable;
            GameInfo.recruit.value = complete;
        }
        /*      hire
         *                "state": 1,
         *                 "refreshCount": 1,
         *                 "completeWorkTime": 1693772441,
         *                 "slotState": 2
         * */
        JsonNode node_hire = tree.at("/data/building/hire");
        GameInfo.hire.isNull = node_hire.isNull();
        if (!GameInfo.hire.isNull) {
            GameInfo.hire.value = node_hire.get("refreshCount").asInt();
            GameInfo.hire.time = node_hire.get("completeWorkTime").asInt() - currentTs;
        }

        /*     Campaign
         *             "reward": {
         *                 "current": 1020,
         *                 "total": 1200
         *             }
         * */
        JsonNode node_campaign = tree.at("/data/campaign/reward");
        GameInfo.campaign.isNull = node_campaign.isNull();
        if (!GameInfo.campaign.isNull) {
            GameInfo.campaign.current = node_campaign.get("current").asInt();
            GameInfo.campaign.total = node_campaign.get("total").asInt();
        }

        /* RoutineDay and week
         * "routine": {
         *             "daily": {
         *                 "current": 10,
         *                 "total": 10
         *             },
         *             "weekly": {
         *                 "current": 13,
         *                 "total": 13
         *             }
         *         },
         * */
        GameInfo.routineDay.isNull = tree.at("/data/routine").isNull();
        GameInfo.routineWeek.isNull = tree.at("/data/routine").isNull();
        if (!GameInfo.routineWeek.isNull && !GameInfo.routineDay.isNull) {
            JsonNode node_day = tree.at("/data/routine/daily");
            JsonNode node_week = tree.at("/data/routine/weekly");
            GameInfo.routineDay.current = node_day.get("current").asInt();
            GameInfo.routineDay.total = node_day.get("total").asInt();
            GameInfo.routineWeek.current = node_week.get("current").asInt();
            GameInfo.routineWeek.total = node_week.get("total").asInt();
        }

        /*      Tower
         * "tower": {
         *             "records": [],
         *             "reward": {
         *                 "higherItem": {
         *                     "current": 0,
         *                     "total": 24
         *                 },
         *                 "lowerItem": {
         *                     "current": 0,
         *                     "total": 60
         *                 },
         *                 "termTs": 1694807999
         *             }
         *         },
         */
        JsonNode node_tower = tree.at("/data/tower");
        GameInfo.tower.isNull = node_tower.isNull();
        if (!GameInfo.tower.isNull) {
            JsonNode node_high = node_tower.at("/reward/higherItem");
            GameInfo.tower.highCurrent = node_high.get("current").asInt();
            GameInfo.tower.highTotal = node_high.get("total").asInt();
            JsonNode node_low = node_tower.at("/reward/lowerItem");
            GameInfo.tower.lowCurrent = node_low.get("current").asInt();
            GameInfo.tower.lowTotal = node_low.get("total").asInt();
        }
        /*BASE*/

        /* Trading
         *  "stock": [
         *  "stockLimit": 10
         * //算法：(current - lastupdate) / ((completeTime - lastTime) / (limit - stock)) + stock
         * */
        JsonNode node_trading = tree.at("/data/building/tradings");
        GameInfo.trading.isNull = node_trading.isNull();
        if (!GameInfo.trading.isNull) {
            int stock = 0;
            int stockLimit = 0;
            for (int i = 0; i < node_trading.size(); i++) {
                JsonNode node = node_trading.get(i);
                int node_com = node.get("completeWorkTime").asInt();
                int node_max = node.get("stockLimit").asInt();
                int node_stock = node.get("stock").size();
                if (currentTs > node_com) {
                    node_stock += 1;
                }

                stock += node_stock;
                stockLimit += node_max;
            }
            GameInfo.trading.value = stock;
            GameInfo.trading.maxValue = stockLimit;
        }
        //man
        /*//算法：
        (current - lastupdate) / ((completeTime - lastTime) / ((capacity/weight for each) - complete)) + complete
         *
         * */
        JsonNode node_product = tree.at("/data/building/manufactures");
        GameInfo.manufactures.isNull = node_product.isNull();
        if (!GameInfo.manufactures.isNull) {
            int value = 0;
            int max = 0;
            for (int i = 0; i < node_product.size(); i++) {
                JsonNode node = node_product.get(i);
                String id = node.get("formulaId").toString().replace("\"", "");
                int weight = getWeightFromId(id);
                int node_max = node.get("capacity").asInt() / weight;
                int node_com = node.get("completeWorkTime").asInt();
                int node_last = node.get("lastUpdateTime").asInt();
                int node_value = node.get("complete").asInt();
                if (currentTs >= node_com) {
                    node_value = node_max;
                } else {
                    node_value += (currentTs - node_last) /
                            ((node_com - node_last) / (node_max - node_value));
                }
                max += node_max;
                value += node_value;
            }
            GameInfo.manufactures.value = value;
            GameInfo.manufactures.maxValue = max;
        }
        //Labor
        //(current - lastupdate) * (max - value) / secRemain + value, if > max =max
        //
        JsonNode node_labor = tree.at("/data/building/labor");
        int labor_update_value = node_labor.get("value").asInt();
        int labor_max = node_labor.get("maxValue").asInt();
        int labor_value = (currentTs - node_labor.get("lastUpdateTime").asInt()) * (labor_max - labor_update_value) / node_labor.get("remainSecs").asInt() + labor_update_value;
        int labor_remain = node_labor.get("remainSecs").asInt() - (currentTs - node_labor.get("lastUpdateTime").asInt());
        if (labor_value > labor_max) {
            labor_value = labor_max;
        }
        GameInfo.labor.value = labor_value;
        GameInfo.labor.maxValue = labor_max;
        if (labor_remain < 0) {
            labor_remain = 0;
        }
        GameInfo.labor.recoverTime = labor_remain;
        //Dormitories
        //没研究明白 似乎ap == 8640000的就是休息完成的(240hour)
        JsonNode node_dormitories = tree.at("/data/building/dormitories");
        GameInfo.dormitories.isNull = node_dormitories.isNull();
        if (!GameInfo.dormitories.isNull) {
            int max = 0;
            int value = 0;
            for (int i = 0; i < node_dormitories.size(); i++) {
                JsonNode node = node_dormitories.get(i).get("chars");
                max += node.size();
                for (int j = 0; j < node.size(); j++) {
                    if (node.get(j).get("ap").asInt() == 8640000) value++;
                }
            }
            GameInfo.dormitories.maxValue = max;
            GameInfo.dormitories.value = value;
        }
        //clue
        JsonNode node_meeting = tree.at("/data/building/meeting");
        GameInfo.meeting.isNull = node_meeting.isNull();
        if (!GameInfo.meeting.isNull) {
            GameInfo.meeting.value = node_meeting.get("clue").get("board").size();
        }
        //tired
        JsonNode node_tired = tree.at("/data/building/tiredChars");
        GameInfo.tired.value = node_tired.size();
        printGameInfo();

        is.close();
        gzip.close();
        loadingDialog.dismiss();
        mHandler.post(this::updateLayout);

    }

    @SuppressLint("SetTextI18n")
    private void updateLayout() {

        //info
        String info = GameInfo.info.nickName + "\n\n"
                + "lv." + GameInfo.info.level + "\n\n"
                + "进度:";
        if (GameInfo.info.progress.equals("")) {
            info += "全部完成";
        } else {
            info += GameInfo.info.progress;
        }
        text_info.setText(info);
        //ap
        int time;
        text_ap.setText(GameInfo.ap.current + "");
        text_ap_max.setText("/" + GameInfo.ap.max);
        time = GameInfo.ap.recoverTime;
        if (time == -1) {
            text_ap_recover.setText("理智已恢复");
        } else {
            text_ap_recover.setText(convertSec2DayHourMin(time));
        }


        //recruit
        text_recruit_max.setText("/" + GameInfo.recruit.max);
        text_recruit_value.setText(GameInfo.recruit.value + "");
        time = GameInfo.recruit.time;
        if (time == -1) {
            text_recruit_ts.setText("招募已完成");
        } else {
            text_recruit_ts.setText(convertSec2DayHourMin(time));
        }
        //hire
        if (GameInfo.hire.isNull) {
            text_hire_time.setText(R.string.null_data);
            text_hire_cnt.setText("");
        } else {
            time = GameInfo.hire.time;
            if (time < 0) {
                text_hire_time.setText("已完成刷新");
                text_hire_cnt.setText((GameInfo.hire.value + 1) + "");
            } else {
                text_hire_time.setText(convertSec2DayHourMin(time));
                text_hire_cnt.setText(GameInfo.hire.value + "");
            }

        }
        //train
        if (GameInfo.train.isNull) {
            text_trainer.setText(R.string.null_data);
            text_train_status.setText("");
        } else {
            if (GameInfo.train.traineeIsNull) {
                text_trainer.setText(R.string.empty);
                text_train_status.setText(R.string.empty);
            } else {
                text_trainer.setText(GameInfo.train.trainee);
                if (GameInfo.train.status == -1) {
                    text_train_status.setText(R.string.empty);
                } else {
                    if (time == 0) {
                        text_train_status.setText(R.string.train_com);
                    } else {
                        text_train_status.setText(convertSec2DayHourMin(time));
                    }
                }
            }
        }
        //labor
        if (GameInfo.labor.isNull) {
            text_labor_value.setText(R.string.null_data);
        } else {
            text_labor_value.setText(GameInfo.labor.value + "/" + GameInfo.labor.maxValue);
        }
        //manufacture
        if (GameInfo.manufactures.isNull) {
            text_manufactures_status.setText(R.string.null_data);
        } else {
            text_manufactures_status.setText(GameInfo.manufactures.value + "/" + GameInfo.manufactures.maxValue);
        }
        //trading
        if (GameInfo.manufactures.isNull) {
            text_trading_status.setText(R.string.null_data);
        } else {
            text_trading_status.setText(GameInfo.trading.value + "/" + GameInfo.trading.maxValue);
        }
        //do
        if (GameInfo.dormitories.isNull) {
            text_dormitories_value.setText(R.string.null_data);
        } else {
            text_dormitories_value.setText(GameInfo.dormitories.value + "/" + GameInfo.dormitories.maxValue);
        }
        //tired
        text_tired.setText(GameInfo.tired.value + "");
        //clue
        if (GameInfo.meeting.isNull) {
            text_clue.setText(R.string.null_data);
        } else {
            text_clue.setText(GameInfo.meeting.value + "/" + GameInfo.meeting.maxValue);
        }
        //campaign
        if (GameInfo.campaign.isNull) {
            text_campaign.setText(R.string.null_data);
        } else {
            text_campaign.setText(GameInfo.campaign.current + "/" + GameInfo.campaign.total);
        }
        //routine
        if (GameInfo.routineDay.isNull) {
            text_routine_day.setText(R.string.null_data);
        } else {
            text_routine_day.setText(GameInfo.routineDay.current + "/" + GameInfo.routineDay.total);
        }
        if (GameInfo.routineWeek.isNull) {
            text_routine_week.setText(R.string.null_data);
        } else {
            text_routine_week.setText(GameInfo.routineWeek.current + "/" + GameInfo.routineWeek.total);
        }
        //tower
        if (GameInfo.tower.isNull) {
            text_low_item.setText(R.string.null_data);
            text_high_item.setText(R.string.null_data);
        } else {
            text_low_item.setText(GameInfo.tower.lowCurrent + "/" + GameInfo.tower.lowTotal);
            text_high_item.setText(GameInfo.tower.highCurrent + "/" + GameInfo.tower.highTotal);
        }
    }

    private void printGameInfo() {
        Log.e("理智", "" + GameInfo.ap.current + "/" + GameInfo.ap.max);
        Log.e("公开招募", "" + GameInfo.recruit.value + " / " + GameInfo.recruit.max + " " + GameInfo.recruit.time);
        Log.e("公招刷新", "" + GameInfo.hire.value);

        Log.e("训练室", "" + GameInfo.train.trainee + " " + GameInfo.train.time);
        Log.e("每周剿灭", "" + GameInfo.campaign.current + " / " + GameInfo.campaign.total);
        Log.e("日常", "" + GameInfo.routineDay.current + " / " + GameInfo.routineDay.total);
        Log.e("周常", "" + GameInfo.routineWeek.current + " / " + GameInfo.routineWeek.total);
        Log.e("保全", "" + GameInfo.tower.lowCurrent + " / " + GameInfo.tower.lowTotal
                + "   " + GameInfo.tower.highCurrent + " / " + GameInfo.tower.highTotal);
        Log.e("订单进度", "" + GameInfo.trading.value + " / " + GameInfo.trading.maxValue);
        Log.e("制造进度", "" + GameInfo.manufactures.value + " / " + GameInfo.manufactures.maxValue);
        Log.e("休息进度", "" + GameInfo.dormitories.value + " / " + GameInfo.dormitories.maxValue);
        Log.e("线索收集", "" + GameInfo.meeting.value + " / " + GameInfo.meeting.maxValue);
        Log.e("干员疲劳", "" + GameInfo.tired.value);
        Log.e("无人机", "" + GameInfo.labor.value + " / " + GameInfo.labor.maxValue);
    }

    //updateLayout

    /**
     * 小组件：为了向下兼容（和向上兼容）,做个1x2,和2x2 1x4,和2x4的吧。
     */
    private void setLayout() {
        setContentView(R.layout.activity_skland);
        text_info = findViewById(R.id.text_info);
        text_ap = findViewById(R.id.text_ap);
        text_ap_max = findViewById(R.id.text_ap_max);
        text_ap_recover = findViewById(R.id.text_ap_recover);
        text_recruit_ts = findViewById(R.id.text_recruit_ts);
        text_recruit_value = findViewById(R.id.text_recruit_value);
        text_recruit_max = findViewById(R.id.text_recruit_max);
        text_hire_time = findViewById(R.id.text_hire_time);
        text_hire_cnt = findViewById(R.id.text_hire_cnt);
        text_train_status = findViewById(R.id.text_train_status);
        text_trainer = findViewById(R.id.text_trainer);
        text_labor_value = findViewById(R.id.text_labor_value);
        text_manufactures_status = findViewById(R.id.text_manufactures_status);
        text_trading_status = findViewById(R.id.text_trading_status);
        text_dormitories_value = findViewById(R.id.text_dormitories_value);
        text_tired = findViewById(R.id.text_tired);
        text_clue = findViewById(R.id.text_clue);
        text_campaign = findViewById(R.id.text_campaign);
        text_routine_day = findViewById(R.id.text_routine_day);
        text_routine_week = findViewById(R.id.text_routine_week);
        text_low_item = findViewById(R.id.text_low_item);
        text_high_item = findViewById(R.id.text_high_item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView textView = findViewById(R.id.text_userInfo);
        textView.setText(getUserInfo(this));
    }

    private int getWeightFromId(String id) {
        switch (id) {
            case "1":
            case "4":
                return 2;
            case "2":
            case "13":
            case "14":
                return 3;
            default:
                return 5;
        }
    }
}
