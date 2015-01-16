package com.example.alexandermikhaylov.rabbitmqtest;

import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TabHost;

import com.rabbitmq.client.QueueingConsumer;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends ListActivity {
    MessageConsumerTask mTask;

    EditText mHost;
    EditText mUsername;
    EditText mPassword;
    EditText mExchange;
    EditText mFilters;

    ArrayList<StringPair> mList;
    MyAdapter mAdapter;

    TabHost mTabs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindFields();
        setDefaults();
    }

    private void bindFields() {
        setContentView(R.layout.activity_main);

        mHost = (EditText)findViewById(R.id.host);
        mUsername = (EditText)findViewById(R.id.username);
        mPassword= (EditText)findViewById(R.id.password);
        mExchange = (EditText)findViewById(R.id.exchange);
        mFilters = (EditText)findViewById(R.id.filters);

        mList = new ArrayList<>();
        mAdapter = new MyAdapter(this, mList);
        setListAdapter(mAdapter);

        mTabs = (TabHost)findViewById(android.R.id.tabhost);
        mTabs.setup();

        TabHost.TabSpec spec = mTabs.newTabSpec("tag1");
        spec.setContent(R.id.settingsTab);
        spec.setIndicator("Settings");
        mTabs.addTab(spec);

        spec = mTabs.newTabSpec("tag2");
        spec.setContent(R.id.logTab);
        spec.setIndicator("Logs");
        mTabs.addTab(spec);

        mTabs.setCurrentTab(0);
    }

    private void setDefaults() {
        mHost.setText("home.geohero.ru");
        mUsername.setText("nskdvlp");
        mPassword.setText("12345678");
        mExchange.setText("snmp_int_notif");
        mFilters.setText("#.error\n#.warning\n#.normal");
    }

    @Override
    protected void onResume() {
        super.onPause();
//        mConsumer.connectToRabbitMQ();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        mConsumer.dispose();
    }

    public void onReconnectClick(View v) {
        List<String> params = new ArrayList<>();
        params.add(mHost.getText().toString());
        params.add(mUsername.getText().toString());
        params.add(mPassword.getText().toString());
        params.add(mExchange.getText().toString());
        params.addAll(Arrays.asList(mFilters.getText().toString().split("\n")));

        if (mTask == null) {
            mTask = new MessageConsumerTask();
            mTask.execute(params.toArray(new String[params.size()]));
        }
    }

    public void onClearClick(View v) {
        mList.clear();
        mAdapter.notifyDataSetChanged();
    }

    class MessageConsumerTask extends AsyncTask<String, String, Void> {
        private MessageConsumer mConsumer;

        private void publishText(String val1, String val2) {
            StringPair el = new StringPair();
            el.setItem(val1);
            el.setSubItem(val2);
            mList.add(0, el);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            publishText("Connecting...", "");
        }

        @Override
        protected Void doInBackground(String... params) {
            mConsumer = new MessageConsumer(params[0], params[3]);

            mConsumer.dropBinding();
            for (int i = 4; i < params.length; i++) {
                mConsumer.setBinding(params[i]);
            }

            mConsumer.setOnReceiveMessageHandler(new MessageConsumer.OnReceiveMessageHandler(){
                public void onReceiveMessage(QueueingConsumer.Delivery delivery) {
                    String text = "";
                    try {
                        text = new String(delivery.getBody(), "UTF8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    publishProgress(delivery.getEnvelope().getRoutingKey(), text);
                }
            });

            mConsumer.connectToRabbitMQ(params[1], params[2]);

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            Calendar cal = Calendar.getInstance();
            cal.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss - ");
            publishText(sdf.format(cal.getTime()).concat(values[0]), values[1]);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            publishText("Connection ended", "");
        }
    }
}
