package com.example.alexandermikhaylov.rabbitmqtest;

import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ListActivity {
    MessageConsumerTask mTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        EditText host = (EditText)findViewById(R.id.host);
        EditText exchange = (EditText)findViewById(R.id.exchange);
        EditText filters = (EditText)findViewById(R.id.filters);

        List<String> params = new ArrayList<String>();
        params.add(host.getText().toString());
        params.add(exchange.getText().toString());
        for (String o : filters.getText().toString().split("\n")) {
            params.add(o);
        }

        mTask = new MessageConsumerTask();
        mTask.execute(params.toArray(new String[params.size()]));
    }

    class MessageConsumerTask extends AsyncTask<String, String, Void> {
        private MessageConsumer mConsumer;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            mConsumer = new MessageConsumer(params[0], params[1]);
            mConsumer.connectToRabbitMQ();

            for (int i = 2; i < params.length; i++) {
                mConsumer.AddBinding(params[i]);
            }

            mConsumer.setOnReceiveMessageHandler(new MessageConsumer.OnReceiveMessageHandler(){
                public void onReceiveMessage(byte[] message) {
                String text = "";
                try {
                    text = new String(message, "UTF8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                publishProgress(text);
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }
}
