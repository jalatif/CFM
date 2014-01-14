package com.abhinav.CFM.receivers;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.abhinav.CFM.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MyTestActivity extends Activity {
    private Button bSet, bCheck, bStop;
    private TextView tvStatus;
    private EditText etNum;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        bSet = (Button) findViewById(R.id.bSet);
        bCheck = (Button) findViewById(R.id.bCheck);
        bStop = (Button) findViewById(R.id.bStop);
        tvStatus = (TextView) findViewById(R.id.tvStatus);
        etNum = (EditText) findViewById(R.id.etNum);

        bSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String num = "**21*" + etNum.getText().toString() + "#";
                setCallForward(num);
            }
        });

        bStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCallForward("##21#");
            }
        });

        bCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCallForward("*#21#");
            }
        });

        try{
            Process process = Runtime.getRuntime().exec("logcat -c");
        }
        catch (IOException e){}

        String TAG = "CFM";
        String pname = getPackageName();
        String[] CMDLINE_GRANTPERMS = { "su", "-c", null };
        if (getPackageManager().checkPermission(android.Manifest.permission.READ_LOGS, pname) != 0) {
            Log.d(TAG, "we do not have the READ_LOGS permission!");
            if (android.os.Build.VERSION.SDK_INT >= 16) {
                Log.d(TAG, "Working around JellyBeans 'feature'...");
                try {
                    // format the commandline parameter
                    CMDLINE_GRANTPERMS[2] = String.format("pm grant %s android.permission.READ_LOGS", pname);
                    java.lang.Process p = Runtime.getRuntime().exec(CMDLINE_GRANTPERMS);
                    int res = p.waitFor();
                    Log.d(TAG, "exec returned: " + res);
                    if (res != 0)
                        throw new Exception("failed to become root");
                } catch (Exception e) {
                    Log.d(TAG, "exec(): " + e);
                    //Toast.makeText(context, "Failed to obtain READ_LOGS permission", Toast.LENGTH_LONG).show();
                }
            }
        } else
            Log.d(TAG, "we have the READ_LOGS permission already!");


    }

    private void setCallForward(String num){

        String callForwardString = num ;
        Intent intentCallForward = new Intent(Intent.ACTION_CALL); // ACTION_CALL
        Uri uri2 = Uri.fromParts("tel", callForwardString, "#");
        intentCallForward.setData(uri2);
        //startActivity(intentCallForward);
        startActivityForResult(intentCallForward, 1);
        //Intent callIntent = new Intent(Intent.ACTION_CALL);
        //callIntent.setData(Uri.parse("tel:123456789"));
        //startActivity(callIntent);
        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            StringBuilder log=new StringBuilder();
            String line = "";
            tvStatus.setText("");
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line);
                System.out.println("jalatif: " + line);
                if (line.contains("QuietBalance") || line.contains("Voice: ")){
                    //System.out.println("jalatif: forward Matched");
                    if (line.contains("Erase"))
                        tvStatus.setText("Call Forwarding Disabled");
                    else if (line.contains("Registration"))
                        tvStatus.setText("Call Forwarding active to " + num);//.substring(6, 15));
                    else if (line.contains("Voice: Not forwarded"))
                        tvStatus.setText("Call not forwarded");
                    else if (line.contains("Voice"))
                        tvStatus.setText("Call forwarded to " + line.split("Voice: ")[1]);
                    else
                        ;
                    //    tvStatus.setText("Call Forwarding Details Status Check");
                    //tvStatus.append(line + "\n");
                }
            }
        }
        catch (IOException e) {}
        try{
            Process process = Runtime.getRuntime().exec("logcat -c");
        }
        catch (IOException e){}
    }

}