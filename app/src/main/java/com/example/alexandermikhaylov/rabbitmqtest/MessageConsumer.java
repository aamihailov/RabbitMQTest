package com.example.alexandermikhaylov.rabbitmqtest;

import com.rabbitmq.client.QueueingConsumer;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 *Consumes messages from a RabbitMQ broker
 *
 */
public class MessageConsumer extends IConnectToRabbitMQ {

    public MessageConsumer(String server, String exchange) {
        super(server, exchange, "topic");

        mBindingKeys = new ArrayList<>();
    }

    //The Queue name for this consumer
    private String mQueue;
    private QueueingConsumer MySubscription;

    //last message to post back
    private byte[] mLastMessage;

    // An interface to be implemented by an object that is interested in messages(listener)
    public interface OnReceiveMessageHandler{
        public void onReceiveMessage(byte[] message);
    }

    //A reference to the listener, we can only have one at a time(for now)
    private OnReceiveMessageHandler mOnReceiveMessageHandler;

    /**
     *
     * Set the callback for received messages
     * @param handler The callback
     */
    public void setOnReceiveMessageHandler(OnReceiveMessageHandler handler){
        mOnReceiveMessageHandler = handler;
    }

    /**
     * Create Exchange and then start consuming. A binding needs to be added before any messages will be delivered
     */
    @Override
    public boolean connectToRabbitMQ(String username, String password)
    {
        if(super.connectToRabbitMQ(username, password))
        {
            try {
                mQueue = mModel.queueDeclare().getQueue();
                MySubscription = new QueueingConsumer(mModel);
                mModel.basicConsume(mQueue, false, MySubscription);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            AddBinding();
            Consume();
        }
        return false;
    }

    private List<String> mBindingKeys;

    public void dropBinding()
    {
        mBindingKeys.clear();
    }

    public void setBinding(String routingKey)
    {
        mBindingKeys.add(routingKey);
    }

    private void AddBinding()
    {
        try {
            for (String key : mBindingKeys) {
                mModel.queueBind(mQueue, mExchange, key);
            }
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
                mOnReceiveMessageHandler.onReceiveMessage(mLastMessage);
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