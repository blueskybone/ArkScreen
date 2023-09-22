package com.godot17.arksc.activity;

import static com.godot17.arksc.utils.Utils.getMarkDownTextOnAct;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.godot17.arksc.R;
import com.godot17.arksc.datautils.FinalOpeList;
import com.godot17.arksc.service.DataQueryService;

import java.util.ArrayList;
import java.util.List;

import io.noties.markwon.Markwon;

public class CalActivity extends Activity implements View.OnClickListener {
    private int tagCnt = 0;
    private Button[] buttons = new Button[29];
    private TextView textView_cal;

    private void setLayout() {
        setContentView(R.layout.activity_cal);
        Button button_1 = findViewById(R.id.button_1);
        Button button_2 = findViewById(R.id.button_2);
        Button button_3 = findViewById(R.id.button_3);
        Button button_4 = findViewById(R.id.button_4);
        Button button_5 = findViewById(R.id.button_5);
        Button button_6 = findViewById(R.id.button_6);
        Button button_7 = findViewById(R.id.button_7);
        Button button_8 = findViewById(R.id.button_8);
        Button button_9 = findViewById(R.id.button_9);
        Button button_10 = findViewById(R.id.button_10);
        Button button_11 = findViewById(R.id.button_11);
        Button button_12 = findViewById(R.id.button_12);
        Button button_13 = findViewById(R.id.button_13);
        Button button_14 = findViewById(R.id.button_14);
        Button button_15 = findViewById(R.id.button_15);
        Button button_16 = findViewById(R.id.button_16);
        Button button_17 = findViewById(R.id.button_17);
        Button button_18 = findViewById(R.id.button_18);
        Button button_19 = findViewById(R.id.button_19);
        Button button_20 = findViewById(R.id.button_20);
        Button button_21 = findViewById(R.id.button_21);
        Button button_22 = findViewById(R.id.button_22);
        Button button_23 = findViewById(R.id.button_23);
        Button button_24 = findViewById(R.id.button_24);
        Button button_25 = findViewById(R.id.button_25);
        Button button_26 = findViewById(R.id.button_26);
        Button button_27 = findViewById(R.id.button_27);
        Button button_28 = findViewById(R.id.button_28);

        Button button_reset = findViewById(R.id.button_29);

        textView_cal = findViewById(R.id.textView_cal);
        buttons = new Button[]{button_1, button_2, button_3, button_4, button_5,
                button_6, button_7, button_8, button_9, button_10, button_11,
                button_12, button_13, button_14, button_15, button_16, button_17,
                button_18, button_19, button_20, button_21, button_22, button_23,
                button_24, button_25, button_26, button_27, button_28};

        for (Button button : buttons) {
            button.setOnClickListener(this);
            button.setEnabled(true);
            button.setClickable(true);
            button.setSelected(false);
        }
        button_reset.setOnClickListener(this);
        button_reset.setEnabled(true);
        button_reset.setClickable(true);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.button_29) {
            for (Button button : buttons) {
                button.setSelected(false);
                tagCnt = 0;
            }
        } else {
            int idx = getButtonIdx(id);
            if (buttons[idx].isSelected()) {
                buttons[idx].setSelected(false);
                tagCnt--;
            } else if (tagCnt < 6) {
                buttons[idx].setSelected(true);
                tagCnt++;
            } else {
                Toast.makeText(this, "最多选择6个标签", Toast.LENGTH_SHORT).show();
            }
        }
        try {
            startDataQuery();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private int getButtonIdx(int id) {
        if (id == R.id.button_1) return 0;
        else if (id == R.id.button_2) return 1;
        else if (id == R.id.button_3) return 2;
        else if (id == R.id.button_4) return 3;
        else if (id == R.id.button_5) return 4;
        else if (id == R.id.button_6) return 5;
        else if (id == R.id.button_7) return 6;
        else if (id == R.id.button_8) return 7;
        else if (id == R.id.button_9) return 8;
        else if (id == R.id.button_10) return 9;
        else if (id == R.id.button_11) return 10;
        else if (id == R.id.button_12) return 11;
        else if (id == R.id.button_13) return 12;
        else if (id == R.id.button_14) return 13;
        else if (id == R.id.button_15) return 14;
        else if (id == R.id.button_16) return 15;
        else if (id == R.id.button_17) return 16;
        else if (id == R.id.button_18) return 17;
        else if (id == R.id.button_19) return 18;
        else if (id == R.id.button_20) return 19;
        else if (id == R.id.button_21) return 20;
        else if (id == R.id.button_22) return 21;
        else if (id == R.id.button_23) return 22;
        else if (id == R.id.button_24) return 23;
        else if (id == R.id.button_25) return 24;
        else if (id == R.id.button_26) return 25;
        else if (id == R.id.button_27) return 26;
        else if (id == R.id.button_28) return 27;
        else return 0;
    }

    private void startDataQuery() throws InterruptedException {
        List<String> listTags = new ArrayList<>();
        for (Button button : buttons) {
            if (button.isSelected()) {
                listTags.add(button.getContentDescription().toString());
            }
        }
        String[] tags = new String[listTags.size()];
        listTags.toArray(tags);

        DataQueryService dataService = DataQueryService.getInstance();
        if(dataService == null){
            startService();
        }
        assert dataService != null;
        FinalOpeList finalOpeList = dataService.getAllOpeList(this, tags);
        String opeListText = getMarkDownTextOnAct(finalOpeList.opeGroups);
        Markwon.create(this).setMarkdown(textView_cal, opeListText);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLayout();
        startService();
    }
    private void startService(){
        this.startService(new Intent(this, DataQueryService.class));
    }
}
