package com.example.project_cgoq;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationSet;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;


public class GameActivity extends AppCompatActivity {
    ObjectAnimator redNodeAnim[];
    ObjectAnimator greenNodeAnim[];
    ObjectAnimator blueNodeAnim[];
    ObjectAnimator yellowNodeAnim[];
    ObjectAnimator ButtonAnim;
    //애니메이션
    ImageView RedNode[];
    ImageView BlueNode[];
    ImageView GreenNode[];
    ImageView YellowNode[];

    //노드 이미지
    Handler RedNode_Handler;
    RedNode_Runnable redNode_runnable;
    //빨강 노드
    Handler GreenNode_Handler;
    GreenNode_Runnable greenNode_runnable;
    //초록 노드
    Handler BlueNode_Handler;
    BlueNode_Runnable blueNode_runnable;
    //파랑 노드
    Handler YellowNode_Handler;
    YellowNode_Runnable yellowNode_runnable;
    //노랑 노드

    LinearLayout Buttonlayout;
    TextView textView;
    int NodeTime;
    int point;
    int NodeLayout_Height;
    int Node_Height;
    int time;
    int size;

    int RedNode_Check;
    int RedNode_Start;

    int GreenNode_Check;
    int GreenNode_Start;

    int BlueNode_Check;
    int BlueNode_Start;

    int YellowNode_Check;
    int YellowNode_Start;

    int[] array;
    String node_name;
    String song_name;
    boolean isRunning = true;
    boolean node_set[][];
    boolean Game_Check = true;
    MediaPlayer song;

    ImageButton GameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);

        GameButton = (ImageButton) findViewById(R.id.GameButton);

        node_name = pref.getString("node_name", "0");
        song_name = pref.getString("song_name", "0");
        // 음악 실행행
       try {
            song = new MediaPlayer();
            song.setAudioStreamType(AudioManager.STREAM_MUSIC);
            song.setDataSource(song_name);
            song.prepare();
            song.setVolume(0.2f, 0.2f);
            song.setLooping(false);
            song.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer arg0) {
                    Game_end();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 노드 생성
        try {
            Scanner in = new Scanner(new File(node_name));
            array = new int[in.nextInt()];
            for (int i = 0; i < array.length; i++)
                array[i] = in.nextInt();
        } catch (Exception e) {
            e.printStackTrace();
        }
        size = array[1];
        node_set = new boolean[4][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < 4; j++) {
                node_set[j][i] = (array[j * size + i + 2] != 0);
                System.out.println("node_set[" + Integer.toString(j) + "][" + Integer.toString(i) + "] = " + node_set[j][i]);
            }
        }
        // 다음 노드를 읽기까지의 시간
        time = array[0];
        // 노드가 내려오는 시간 선언
        NodeTime = time * 30;
        // 점수 초기화
        point = 0;
        // 각각의 노드 이미지 선언
        RedNode = new ImageView[3];
        GreenNode = new ImageView[3];
        BlueNode = new ImageView[3];
        YellowNode = new ImageView[3];
        //각각 노드 이미지 아이디 부여
        RedNode[0] = (ImageView) findViewById(R.id.RedNode0);
        RedNode[1] = (ImageView) findViewById(R.id.RedNode1);
        RedNode[2] = (ImageView) findViewById(R.id.RedNode2);
        GreenNode[0] = (ImageView) findViewById(R.id.GreenNode0);
        GreenNode[1] = (ImageView) findViewById(R.id.GreenNode1);
        GreenNode[2] = (ImageView) findViewById(R.id.GreenNode2);
        BlueNode[0] = (ImageView) findViewById(R.id.BlueNode0);
        BlueNode[1] = (ImageView) findViewById(R.id.BlueNode1);
        BlueNode[2] = (ImageView) findViewById(R.id.BlueNode2);
        YellowNode[0] = (ImageView) findViewById(R.id.YellowNode0);
        YellowNode[1] = (ImageView) findViewById(R.id.YellowNode1);
        YellowNode[2] = (ImageView) findViewById(R.id.YellowNode2);

        Buttonlayout = (LinearLayout) findViewById(R.id.ButtonLayout);
        textView = (TextView) findViewById(R.id.textView);
        RedNode_Handler = new Handler();
        redNode_runnable = new RedNode_Runnable();

        GreenNode_Handler = new Handler();
        greenNode_runnable = new GreenNode_Runnable();

        BlueNode_Handler = new Handler();
        blueNode_runnable = new BlueNode_Runnable();

        YellowNode_Handler = new Handler();
        yellowNode_runnable = new YellowNode_Runnable();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        NodeLayout_Height = displayMetrics.heightPixels * 10 / 22;
        Node_Height = NodeLayout_Height / 12;
        ButtonAnim = ObjectAnimator.ofFloat(Buttonlayout, "rotation", 360);
        ButtonAnim.setRepeatCount(ObjectAnimator.INFINITE);
        ButtonAnim.setDuration(8000).setInterpolator(new LinearInterpolator());

        ButtonAnim.start();

        // 각각의 노드의 이미치 출력 및 체크 용 변수 선언
        RedNode_Check = 0;
        RedNode_Start = 0;

        GreenNode_Check = 0;
        GreenNode_Start = 0;

        BlueNode_Check = 0;
        BlueNode_Start = 0;

        YellowNode_Check = 0;
        YellowNode_Start = 0;
        // 각 노드의 애니메이션 선언
        redNodeAnim = new ObjectAnimator[3];
        greenNodeAnim = new ObjectAnimator[3];
        blueNodeAnim = new ObjectAnimator[3];
        yellowNodeAnim = new ObjectAnimator[3];
        // 각 노드의 애니메이션에 대한 애니메이션 정보 할당
        for (int i = 0; i < 3; i++)
        {
            redNodeAnim[i] = ObjectAnimator.ofFloat(RedNode[i], "translationY", -Node_Height, NodeLayout_Height);
            redNodeAnim[i].setDuration(NodeTime).setInterpolator(new LinearInterpolator());
            redNodeAnim[i].addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    // 애니메이션 시작 시 할당된 노드의 활성화
                    RedNode[RedNode_Start].setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    // 애니메이션 종료 시 할당된 노드의 위치 확인
                    float animValue = (float)(redNodeAnim[RedNode_Check]).getAnimatedValue();
                    int check = (int)animValue;
                    // 노드의 위치에 따른 점수 추가
                    if(check >= NodeLayout_Height)
                        point += 0;
                    else if(check >= NodeLayout_Height - (Node_Height/ 2) && check < NodeLayout_Height)
                        point += 200;
                    else if(check >= NodeLayout_Height - (3*Node_Height/2) && check < NodeLayout_Height - (Node_Height/ 2))
                        point += 400;
                    else if(check >= NodeLayout_Height - (2*Node_Height) && check < NodeLayout_Height - (3*Node_Height/2) )
                        point += 200;
                    else
                        point += 0;
                    // 변경된 점수를 출력
                    textView.setText("점수 : " + point);
                    // 종료된 노드의 비활성화
                    RedNode[RedNode_Check].setVisibility(View.INVISIBLE);
                    // 다음에 활성화 될 노드 값으로 체크 값을 변경
                    RedNode_Check = (RedNode_Check + 1) % 3;
                }
            });
            // 레드와 동일한 내용
            greenNodeAnim[i] = ObjectAnimator.ofFloat(GreenNode[i], "translationY", -90, NodeLayout_Height);
            greenNodeAnim[i].setDuration(NodeTime).setInterpolator(new LinearInterpolator());
            greenNodeAnim[i].addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    GreenNode[GreenNode_Start].setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    float animValue = (float)greenNodeAnim[GreenNode_Check].getAnimatedValue();
                    int check = (int)animValue;
                    if(check >= NodeLayout_Height)
                        point += 0;
                    else if(check >= NodeLayout_Height - (Node_Height/ 2) && check < NodeLayout_Height)
                        point += 200;
                    else if(check >= NodeLayout_Height - (3*Node_Height/2) && check < NodeLayout_Height - (Node_Height/ 2))
                        point += 400;
                    else if(check >= NodeLayout_Height - (2*Node_Height) && check < NodeLayout_Height - (3*Node_Height/2) )
                        point += 200;
                    else
                        point += 0;

                    textView.setText("점수 : " + point);

                    GreenNode[GreenNode_Check].setVisibility(View.INVISIBLE);

                    GreenNode_Check = (GreenNode_Check + 1) % 3;
                }
            });

            blueNodeAnim[i] = ObjectAnimator.ofFloat(BlueNode[i], "translationY", -90, NodeLayout_Height);
            blueNodeAnim[i].setDuration(NodeTime).setInterpolator(new LinearInterpolator());
            blueNodeAnim[i].addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    BlueNode[BlueNode_Start].setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    float animValue = (float)blueNodeAnim[BlueNode_Check].getAnimatedValue();
                    int check = (int)animValue;
                    if(check >= NodeLayout_Height)
                        point += 0;
                    else if(check >= NodeLayout_Height - (Node_Height/ 2) && check < NodeLayout_Height)
                        point += 200;
                    else if(check >= NodeLayout_Height - (3*Node_Height/2) && check < NodeLayout_Height - (Node_Height/ 2))
                        point += 400;
                    else if(check >= NodeLayout_Height - (2*Node_Height) && check < NodeLayout_Height - (3*Node_Height/2) )
                        point += 200;
                    else
                        point += 0;

                    textView.setText("점수 : " + point);

                    BlueNode[BlueNode_Check].setVisibility(View.INVISIBLE);

                    BlueNode_Check = (BlueNode_Check + 1) % 3;
                }
            });

            yellowNodeAnim[i] = ObjectAnimator.ofFloat(YellowNode[i], "translationY", -90, NodeLayout_Height);
            yellowNodeAnim[i].setDuration(NodeTime).setInterpolator(new LinearInterpolator());
            yellowNodeAnim[i].addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    YellowNode[YellowNode_Start].setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    float animValue = (float)yellowNodeAnim[YellowNode_Check].getAnimatedValue();
                    int check = (int)animValue;
                    if(check >= NodeLayout_Height)
                        point += 0;
                    else if(check >= NodeLayout_Height - (Node_Height/ 2) && check < NodeLayout_Height)
                        point += 200;
                    else if(check >= NodeLayout_Height - (3*Node_Height/2) && check < NodeLayout_Height - (Node_Height/ 2))
                        point += 400;
                    else if(check >= NodeLayout_Height - (2*Node_Height) && check < NodeLayout_Height - (3*Node_Height/2) )
                        point += 200;
                    else
                        point += 0;

                    textView.setText("점수 : " + point);

                    YellowNode[YellowNode_Check].setVisibility(View.INVISIBLE);

                    YellowNode_Check = (YellowNode_Check + 1) % 3;
                }
            });
        }
        // 각각 노드 이미지 비활성화
        for(int i = 0; i < 3 ; i ++)
        {
            RedNode[i].setVisibility(View.INVISIBLE);
            GreenNode[i].setVisibility(View.INVISIBLE);
            BlueNode[i].setVisibility(View.INVISIBLE);
            YellowNode[i].setVisibility(View.INVISIBLE);
        }
        // 초기화 된 점수를 출력
        textView.setText("점수 : " + point);
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart(){
        super.onStart();
        // 게임이 실행 중일 때, 곡을 실행하고 쓰레드를 선언
        if(isRunning)
        {
            song.start();

            Thread NodeThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        for(int i = 29; i < size ; ){
                            // 일정 시간마다 각 노드의 정보를 확인하고 애니메이션을 실행하는 runnable을 실행
                            if(isRunning) {
                                if (node_set[0][i])
                                    RedNode_Handler.post(redNode_runnable);
                                if (node_set[1][i])
                                    GreenNode_Handler.post(greenNode_runnable);

                                if (node_set[2][i])
                                    BlueNode_Handler.post(blueNode_runnable);

                                if (node_set[3][i])
                                    YellowNode_Handler.post(yellowNode_runnable);
                                i++;
                                Thread.sleep(time);
                            }
                            else
                            {
                                Thread.sleep(1);    // 게임이 실행 중이 아닐 때, 쓰레드를 멈춘다.
                            }
                        }
                    }catch (Exception ex){
                        Log.e("MainActivity", "Exception in processing message.", ex);
                    }
                }
            });

            NodeThread.start();
        }
    }

    @Override
    public void onPause(){
        Game_Stop();
        super.onPause();
    }
    @Override
    public void onStop(){
        Game_Stop();
        super.onStop();
    }
    @Override
    public void onBackPressed(){
        Game_Stop();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("[ 찾기 화면으로 되돌아 가시겠습니까? ]");
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                end();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public class RedNode_Runnable implements Runnable {
        public void run() {
            try{
                redNodeAnim[RedNode_Start].start();
                RedNode_Start = (RedNode_Start + 1)% 3;
            }catch (Exception ex){
                Log.e("MainActivity", "Exception in processing message.", ex);
            }
        }
    }

    public class GreenNode_Runnable implements Runnable {
        public void run() {
            try{
                greenNodeAnim[GreenNode_Start].start();
                GreenNode_Start = (GreenNode_Start + 1)% 3;
            }catch (Exception ex){
                Log.e("MainActivity", "Exception in processing message.", ex);
            }
        }
    }

    public class BlueNode_Runnable implements Runnable {
        public void run() {
            try{
                blueNodeAnim[BlueNode_Start].start();
                BlueNode_Start = (BlueNode_Start + 1)% 3;
            }catch (Exception ex){
                Log.e("MainActivity", "Exception in processing message.", ex);
            }
        }
    }

    public class YellowNode_Runnable implements Runnable {
        public void run() {
            try{
                yellowNodeAnim[YellowNode_Start].start();
                YellowNode_Start = (YellowNode_Start + 1)% 3;
            }catch (Exception ex){
                Log.e("MainActivity", "Exception in processing message.", ex);
            }
        }
    }

    public void onClick_redNode_button(View v)
    {
        if(isRunning)
            redNodeAnim[RedNode_Check].cancel();
    }

    public void onClick_greenNode_button(View v)
    {
        if(isRunning)
            greenNodeAnim[GreenNode_Check].cancel();
    }

    public void onClick_blueNode_button(View v)
    {
        if(isRunning)
            blueNodeAnim[BlueNode_Check].cancel();
    }

    public void onClick_yellowNode_button(View v)
    {
        if(isRunning)
            yellowNodeAnim[YellowNode_Check].cancel();
    }

    public void onClick_Game_button(View v) {
        if(Game_Check)
        {
            Game_Stop();
        }
        else {
            Game_Restart();
        }
    }

    public void Game_end()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("[" + point + "]");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                end();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void Game_Stop()
    {
        GameButton.setImageResource(R.drawable.restart);
        isRunning = false;
        Game_Check = false;
        for(int i = 0; i < 3 ; i++)
        {
            redNodeAnim[i].pause();
            greenNodeAnim[i].pause();
            blueNodeAnim[i].pause();
            yellowNodeAnim[i].pause();
        }


        ButtonAnim.pause();
        song.pause();
    }

    public void Game_Restart()
    {
        GameButton.setImageResource(R.drawable.pause);
        isRunning = true;
        Game_Check = true;
        for(int i = 0; i < 3 ; i++)
        {
            redNodeAnim[i].resume();
            greenNodeAnim[i].resume();
            blueNodeAnim[i].resume();
            yellowNodeAnim[i].resume();
        }
        ButtonAnim.resume();
        song.start();
    }

    public void end()
    {
        Intent intent = new Intent(this, FindActivity.class);
        intent.setFlags(Intent. FLAG_ACTIVITY_SINGLE_TOP );
        startActivity(intent);
        finish();
    }
}
