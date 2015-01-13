package com.example.alexandermikhaylov.rabbitmqtest;

import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

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

        mTask = new MessageConsumerTask();
        mTask.execute(host.getText().toString(), exchange.getText().toString());
    }

    class MessageConsumerTask extends AsyncTask<String, String, Void> {
        private MessageConsumer mConsumer;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            mConsumer = new MessageConsumer(params[0], params[1], "fanout");
            mConsumer.connectToRabbitMQ();

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
