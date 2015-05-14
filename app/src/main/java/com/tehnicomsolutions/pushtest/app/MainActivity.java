package com.tehnicomsolutions.pushtest.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tehnicomsolutions.pushtest.app.gcm.GCMRegistrationResponse;
import com.tehnicomsolutions.pushtest.app.gcm.GCMUtilities;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity
{
    Button btnRegister;
    TextView tvRegisterStatus;
    TextView tvPushContent;

    ATRegister atRegister;
    Receiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnRegister = (Button)findViewById(R.id.btRegister);
        tvRegisterStatus = (TextView)findViewById(R.id.tvRegisterStatus);
        tvPushContent = (TextView)findViewById(R.id.tvPushContent);

        btnRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (atRegister != null)
                {
                    atRegister.cancel(true);
                }
                atRegister = new ATRegister();
                atRegister.execute();
            }
        });
        receiver = new Receiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("push"));

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if(receiver != null)
        {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
            receiver = null;
        }
    }

    private class ATRegister extends AsyncTask<String, Void, Internet.Response>
    {
        @Override
        protected Internet.Response doInBackground(String... params)
        {
            GCMRegistrationResponse gcmRes = new GCMUtilities(MainActivity.this).getRegistrationId(false);
            List<NameValuePair> nvp = new ArrayList<>();
            nvp.add(new BasicNameValuePair("id", gcmRes.registrationId));
            nvp.add(new BasicNameValuePair("type", "android"));
            Internet.Response response = Internet.httpPost("http://www.novosti.rs/php/pushnotification/set.php", nvp);
            return response;
        }

        @Override
        protected void onPostExecute(Internet.Response s)
        {

            tvRegisterStatus.setText(s.responseData);
        }
    }

    private class Receiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            tvPushContent.setText(intent.getStringExtra("message"));
        }
    }


}
