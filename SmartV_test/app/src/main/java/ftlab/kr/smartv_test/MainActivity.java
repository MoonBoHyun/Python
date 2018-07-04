package ftlab.kr.smartv_test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import kr.ftlab.lib.SmartSensor;
import kr.ftlab.lib.SmartSensorEventListener;
import kr.ftlab.lib.SmartSensorResultMDI;

public class MainActivity extends AppCompatActivity implements SmartSensorEventListener {
    private SmartSensor mMI;
    private SmartSensorResultMDI mResultMDI;

    private Button btnStart;
    private TextView txtResult;

    int mProcess_Status = 0;
    int Process_Stop = 0;
    int Process_Start = 1;

    BroadcastReceiver mHeadSetConnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(Intent.ACTION_HEADSET_PLUG)) { // 이어폰 단자에 센서 결함 유무 확인
                if (intent.hasExtra("state")) {
                    if (intent.getIntExtra("state", 0) == 0) {//센서 분리 시
                        stopSensing();
                        btnStart.setEnabled(false);//센서가 분리되면 START/STOP 버튼 비활성화, 클릭 불가
                        Toast.makeText(MainActivity.this, "Sensor not found", Toast.LENGTH_SHORT).show();
                    } else if (intent.getIntExtra("state", 0) == 1) {//센서 결합 시
                        Toast.makeText(MainActivity.this, "Sensor found", Toast.LENGTH_SHORT).show();
                        btnStart.setEnabled(true);//센서가 연결되면 START/STOP 버튼 활성화
                    }
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        btnStart = (Button) findViewById(R.id.button_on);
        txtResult = (TextView) findViewById(R.id.textresult);

        mMI = new SmartSensor(MainActivity.this, this);
        mMI.selectDevice(SmartSensor.MDI);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //checkPermission();
            PermissionListener permissionlistener = new PermissionListener() {
                @Override
                public void onPermissionGranted() {
                    //Toast.makeText(ActivityIntro.this, "Permission Granted", Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                    Toast.makeText(MainActivity.this, "승인하세요", Toast.LENGTH_SHORT).show();
                    finish();

                }
            };

            new TedPermission(this)
                    .setPermissionListener(permissionlistener)
                    .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                    .setPermissions(android.Manifest.permission.RECORD_AUDIO)
                    .check();
        }
    }

    public void mOnClick(View v) {
        if (mProcess_Status == Process_Start) {//버튼 클릭 시의 상태가 Start 이면 stop process 수행
            stopSensing();
        } else {//버튼 클릭 시의 상태가 Stop 이면 start process 수행
            startSensing();
        }
    }

    android.os.Handler mHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    String str = "";

                    mResultMDI = mMI.getResultMDI();//측정된 값을 가져옴	mResultUV = 1 일경우 1µW

                    switch (mResultMDI.MDI_Type) {
                        case SmartSensor.MDI_V:
                            Log.d("test", "value : " + mResultMDI.MDI_Value);
                            break;
                    }
                    str = String.format("%1.0f\n", mResultMDI.MDI_Value); // 측정값은 자외선 강도
                    txtResult.setText(str);
                    break;
            }
        }
    };

    public void startSensing() {
        btnStart.setText("STOP");
        mProcess_Status = Process_Start;//현재 상태를 start로 설정
        mMI.start();//측정 시작
    }

    public void stopSensing() {
        btnStart.setText("START");
        mProcess_Status = Process_Stop;//현재 상태를 stop로 설정
        mMI.stop();//측정 종료
    }

    public boolean onCreateOptionsMenu(Menu menu) { //상단 메뉴 버튼 생성
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            mMI.registerSelfConfiguration();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intflt = new IntentFilter();
        intflt.addAction(Intent.ACTION_HEADSET_PLUG);
        this.registerReceiver(mHeadSetConnectReceiver, intflt);
    }

    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(mHeadSetConnectReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() { //앱 종료 시 수행
        mMI.quit();
        finish();
        System.exit(0);
        super.onDestroy();
    }

    @Override
    public void onMeasured() {
        /*String str = "";
        mResultMDI = mMI.getResultMDI();

        switch (mResultMDI.MDI_Type) {
            case SmartSensor.MDI_V:
                Log_d("Test", "Value: " + mResultMDI.MDI_Value)
                str = String.format("%1.0f", mResultMDI.MDI_Value);
                txtResult.setText(str);
                break;
        }*/
        mHandler.sendEmptyMessageDelayed(0, 1000);

    }

    @Override
    public void onSelfConfigurated() {
        mProcess_Status = 0;
        btnStart.setText("START");

    }
}
