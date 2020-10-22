package com.mj.rxjavaflowable;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private List<String> datas = new ArrayList<>();
    private EditText etInsert;
    private Button btnComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initLayout();

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case(R.id.btn_complete) :
                datas.add(etInsert.getText().toString());
                registFlowable();
                break;
        }
    }

    private void initLayout(){

        etInsert = findViewById(R.id.et_insert);
        btnComplete = findViewById(R.id.btn_complete);

        btnComplete.setOnClickListener(this);
    }

    private void registFlowable() {

        Flowable<String> flowable = Flowable.create(new FlowableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<String> emitter) throws Throwable {

                for (String data : datas) {
                    if (emitter.isCancelled()) {
                        return;
                    }

                    emitter.onNext(data);
                }

                emitter.onComplete();
            }
        }, BackpressureStrategy.BUFFER); //초과데이터 버퍼링


        flowable.observeOn(Schedulers.computation())
                .subscribe(new Subscriber<String>() {
                    private Subscription subscription;

                    @Override
                    public void onSubscribe(Subscription subscription) {
                        this.subscription = subscription;
                        this.subscription.request(1L);
                    }

                    @Override
                    public void onNext(String data) {
                        String threadName = Thread.currentThread().getName();
                        Log.e("###onNext : ", threadName + ": " + data);
                        this.subscription.request(1L);
                    }

                    @Override
                    public void onError(Throwable error) {
                        error.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        String threadName = Thread.currentThread().getName();
                        Log.e("###onComplete : ", threadName);
                    }
                });
    }
}