package com.vivalnk.sdk.demo.vital.ui;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vivalnk.sdk.Callback;
import com.vivalnk.sdk.demo.base.app.ConnectedActivity;
import com.vivalnk.sdk.demo.base.app.Layout;
import com.vivalnk.sdk.demo.repository.device.DeviceManager;
import com.vivalnk.sdk.demo.vital.R;
import com.vivalnk.sdk.device.bp5s.BP5SManager;
import com.vivalnk.sdk.utils.GSON;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BP5SActivity extends ConnectedActivity {

  private static final String TAG              = "BP5SActivity";
  private static final String PREF_NAME        = "bp_history_prefs";
  private static final String PREF_KEY_RECORDS = "bp_records";

  BP5SManager manager;

  @BindView(R.id.btnEngineerModule) Button btnEngineerModule;
  @BindView(R.id.tvPrinter)         TextView tvPrinter;
  @BindView(R.id.tvMeasureStatus)   TextView tvMeasureStatus;
  @BindView(R.id.tvSys)             TextView tvSys;
  @BindView(R.id.tvDia)             TextView tvDia;
  @BindView(R.id.tvHeartRate)       TextView tvHeartRate;
  @BindView(R.id.tvArrhythmia)      TextView tvArrhythmia;
  @BindView(R.id.tvRecordTime)      TextView tvRecordTime;
  @BindView(R.id.layoutHistory)     LinearLayout layoutHistory;
  @BindView(R.id.tvHistoryProgress) TextView tvHistoryProgress;
  @BindView(R.id.historyContainer)  LinearLayout historyContainer;

  private List<BPRecord> bpRecords = new ArrayList<>();
  private Gson gson = new Gson();

  public static class BPRecord {
    public String sys; public String dia; public String heartRate;
    public boolean arrhythmia; public String time; public long timestamp;
    public BPRecord(String sys, String dia, String heartRate, boolean arrhythmia, String time, long timestamp) {
      this.sys = sys; this.dia = dia; this.heartRate = heartRate;
      this.arrhythmia = arrhythmia; this.time = time; this.timestamp = timestamp;
    }
  }

  @Override protected Layout getLayout() { return Layout.createLayoutByID(R.layout.activity_device_bp5s); }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    manager = DeviceManager.getInstance().getBP5SMananger();
    initEngineerModule();
    loadRecords();
    rebuildHistoryTable();
  }

  private void initEngineerModule() {
    try {
      Class.forName("com.vivalnk.sdk.engineer.ui.EngineerActivity_BP5S");
      btnEngineerModule.setVisibility(View.VISIBLE);
    } catch (ClassNotFoundException e) {
      btnEngineerModule.setVisibility(View.GONE);
    }
  }

  @OnClick(R.id.btnStartMeasure)
  public void btnStartMeasure() {
    manager.startMeasure(mDevice, new Callback() {
      @Override public void onStart() {
        runOnUiThread(() -> {
          tvMeasureStatus.setText("Starting...");
          tvMeasureStatus.setTextColor(0xFFFF9800);
        });
      }
      @Override public void onComplete(Map<String, Object> data) {
        Log.d(TAG, "Callback Complete: " + GSON.toJson(data));
        processData(data);
      }
      @Override public void onError(int code, String msg) {
        runOnUiThread(() -> {
          tvMeasureStatus.setText("Error: " + msg + " (" + code + ")");
          tvMeasureStatus.setTextColor(Color.RED);
        });
      }
    });
  }

  /**
   * SDK 콜백 데이터를 GSON으로 plain Map 변환 후 파싱.
   * SDK가 커스텀 객체를 반환하므로 instanceof Map 체크가 실패함 → JSON round-trip으로 해결.
   */
  private void processData(Map<String, Object> rawMap) {
    if (rawMap == null) return;

    String json = GSON.toJson(rawMap);
    runOnUiThread(() -> tvPrinter.setText(json));

    // SDK 커스텀 객체 → plain Map 변환
    Map<String, Object> normalized = gson.fromJson(json, Map.class);
    if (normalized == null) return;

    Object dataObj = normalized.get("data");

    // 1. 히스토리 리스트
    if (dataObj instanceof List) {
      List<?> list = (List<?>) dataObj;
      for (Object item : list) {
        if (item instanceof Map) showResult((Map<String, Object>) item);
      }
      runOnUiThread(() -> {
        tvMeasureStatus.setText("History Data Loaded");
        rebuildHistoryTable();
      });
      return;
    }

    // 2. ONLINE_RESULT_BP 구조: {"data": {sys, dia, heartRate, ...}}
    if (dataObj instanceof Map) {
      showResult((Map<String, Object>) dataObj);
      return;
    }

    // 3. 데이터가 최상위에 바로 있는 경우
    showResult(normalized);
  }

  private void showResult(Map<String, Object> map) {
    final String sys = getStr(map, "sys");
    final String dia = getStr(map, "dia");
    final String hr  = getStr(map, "heartRate");
    final boolean arr = "true".equalsIgnoreCase(getStr(map, "arrhythmia"));

    Log.d(TAG, "Parsed → SYS=" + sys + " DIA=" + dia + " HR=" + hr + " ARR=" + arr);

    if (sys == null && dia == null) return;

    // BP 디바이스 시간 대신 안드로이드 기기의 현재 시간 사용
    final long recordTime = System.currentTimeMillis();
    final String timeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(recordTime));

    runOnUiThread(() -> {
      if (sys != null) tvSys.setText(sys);
      if (dia != null) tvDia.setText(dia);
      if (hr != null)  tvHeartRate.setText(hr);
      tvArrhythmia.setText("Arrhythmia: " + (arr ? "Yes" : "No"));

      if (sys != null && dia != null) {
        tvMeasureStatus.setText("Measurement Complete");
        tvMeasureStatus.setTextColor(0xFF4CAF50);
        tvRecordTime.setText("Time: " + timeStr);

        BPRecord record = new BPRecord(sys, dia, hr != null ? hr : "--", arr, timeStr, recordTime);
        boolean duplicate = false;
        for (BPRecord r : bpRecords) { if (Math.abs(r.timestamp - recordTime) < 5000) { duplicate = true; break; } }
        if (!duplicate) {
          bpRecords.add(0, record);
          saveRecords();
          rebuildHistoryTable();
        }
      }
    });
  }

  /** Map에서 키 직접 탐색 → 없으면 extras 내부 탐색. null 반환 시 값 없음. */
  private String getStr(Map<?, ?> map, String key) {
    if (map == null) return null;
    // 직접 탐색
    for (Object k : map.keySet()) {
      if (key.equalsIgnoreCase(String.valueOf(k))) {
        Object v = map.get(k);
        if (v == null) return null;
        if (v instanceof Double) return String.valueOf(((Double) v).longValue());
        return String.valueOf(v);
      }
    }
    // extras 내부 탐색
    Object extras = map.get("extras");
    if (extras instanceof Map) {
      return getStr((Map<?, ?>) extras, key);
    }
    return null;
  }

  private void saveRecords() { getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit().putString(PREF_KEY_RECORDS, gson.toJson(bpRecords)).apply(); }
  private void loadRecords() {
    String json = getSharedPreferences(PREF_NAME, MODE_PRIVATE).getString(PREF_KEY_RECORDS, null);
    if (json != null) {
      List<BPRecord> loaded = gson.fromJson(json, new TypeToken<List<BPRecord>>() {}.getType());
      if (loaded != null) bpRecords = loaded;
    }
  }

  private void rebuildHistoryTable() {
    historyContainer.removeAllViews();
    tvHistoryProgress.setText("Records: " + bpRecords.size());
    for (int i = 0; i < bpRecords.size(); i++) {
      BPRecord rec = bpRecords.get(i);
      LinearLayout row = new LinearLayout(this);
      row.setPadding(10, 10, 10, 10);
      row.setBackgroundColor(i % 2 == 0 ? Color.WHITE : Color.parseColor("#F0F0F0"));
      row.addView(makeCell(rec.sys + "/" + rec.dia, 2, Color.RED, true));
      row.addView(makeCell(rec.heartRate, 1, Color.BLACK, false));
      row.addView(makeCell(rec.time, 3, Color.GRAY, false));
      historyContainer.addView(row);
    }
  }

  private TextView makeCell(String text, int weight, int color, boolean bold) {
    TextView tv = new TextView(this);
    tv.setLayoutParams(new LinearLayout.LayoutParams(0, -2, weight));
    tv.setText(text); tv.setTextColor(color); tv.setTextSize(12f); tv.setGravity(Gravity.CENTER);
    if (bold) tv.setTypeface(null, Typeface.BOLD);
    return tv;
  }

  @OnClick(R.id.btnReadHistoryData) void readHistory() { manager.readHistory(mDevice, new Callback() {
    @Override public void onComplete(Map<String, Object> data) { processData(data); }
    @Override public void onError(int code, String msg) {}
  });}
  @OnClick(R.id.btnDisconnect) void disconnect() { DeviceManager.getInstance().disconnect(mDevice); finish(); }
  @OnClick(R.id.btnStopMeasure) void stop() { manager.stopMeasure(mDevice, new Callback() {
    @Override public void onComplete(Map<String, Object> data) {}
    @Override public void onError(int code, String msg) {}
  }); }
  @OnClick(R.id.btnClearAllHistory) void clear() { bpRecords.clear(); saveRecords(); rebuildHistoryTable(); }
}
