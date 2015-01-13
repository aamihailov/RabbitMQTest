package com.example.alexandermikhaylov.rabbitmqtest;

import android.os.Handler;

import com.rabbitmq.client.QueueingConsumer;
import java.io.*;

/**
 *Consumes messages from a RabbitMQ broker
 *
 */
public class MessageConsumer extends IConnectToRabbitMQ {

    public MessageConsumer(String server, String exchange) {
        super(server, exchange, "topic");
    }

    //The Queue name for this consumer
    private String mQueue;
    private QueueingConsumer MySubscription;

    //last message to post back
    private byte[] mLastMessage;

    // An interface to be implemented by an object that is interested in messages(listener)
    public interface OnReceiveMessageHandler{
        public void onReceiveMessage(byte[] message);
    };

    //A reference to the listener, we can only have one at a time(for now)
    private OnReceiveMessageHandler mOnReceiveMessageHandler;

    /**
     *
     * Set the callback for received messages
     * @param handler The callback
     */
    public void setOnReceiveMessageHandler(OnReceiveMessageHandler handler){
        mOnReceiveMessageHandler = handler;
    };

    private Handler mMessageHandler = new Handler();

    // Create runnable for posting back to main thread
    final Runnable mReturnMessage = new Runnable() {
        public void run() {
            mOnReceiveMessageHandler.onReceiveMessage(mLastMessage);
        }
    };

    /**
     * Create Exchange and then start consuming. A binding needs to be added before any messages will be delivered
     */
    @Override
    public boolean connectToRabbitMQ()
    {
        if(super.connectToRabbitMQ())
        {
            try {
                mQueue = mModel.queueDeclare().getQueue();
                MySubscription = new QueueingConsumer(mModel);
                mModel.basicConsume(mQueue, false, MySubscription);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            if (MyExchangeType == "fanout")
                AddBinding("");//fanout has default binding
            Consume();
        }
        return false;
    }

    /**
     * Add a binding between this consumers Queue and the Exchange with routingKey
     * @param routingKey the binding key eg GOOG
     */
    public void AddBinding(String routingKey)
    {
        try {
            mModel.queueBind(mQueue, mExchange, routingKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void Consume()
    {
        while(true) {
            QueueingConsumer.Delivery delivery;
            try {
                delivery = MySubscription.nextDelivery();
                mLastMessage = delivery.getBody();
                mMessageHandler.post(mReturnMessage);
                try {
                    mModel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }
}