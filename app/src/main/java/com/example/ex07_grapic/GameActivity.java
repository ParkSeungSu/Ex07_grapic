package com.example.ex07_grapic;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity implements SensorEventListener {
    Drawable backImg;    //배경
    Drawable gunship;    //사용자 비행기 이미지
    Drawable missile;    //총알 이미지
    Drawable mDouble;
    Drawable missile2;   //적 총알 이미지
    Drawable enemy;      //적 이미지
    Drawable explousure; //폭발이미지
    Drawable item;       //아이템 이미지
    // SoundPool 사운드(1m), MediaPlayer 사운드(1m이상),동영상

    MediaPlayer fire;    //발사음
    MediaPlayer hit;     //타격음
    MediaPlayer bgmusic; //배경음악
    int mspeed = 10;//미사일 속도
    int speed = 0;
    int bulletcount = 5;
    int width, height;   //화면 가로,세로
    int gunshipWidth, gunshipHeight;  //사용자 비행기 가로,세로
    int missileWidth, missileHeight;     //미사일 가로,세로
    int mdWidth, mdHeight;
    int enemyWidth, enemyHeight;        //적 가로, 세로
    int hitWidth, hitHeight;             //폭발 이미지 가로, 세로
    int itemWidth, itemHeight;           //아이템 이미지 가로,세로
    int x,y;                             //비행기좌표
    int mx2, my2;                       // 적 미사일 좌표
    int mx,my;                           //미사일좌표
    int ey;                           //적좌표
    int hx,hy;                           //폭발좌표
    int point;                         //점수
    boolean isFire;                      //총알발사 여부
    boolean isHit;                       //폭발 여부
    boolean maker = false;              //아이템 생성 함수 결정자
    boolean intomaker = false;            //점수에 따른 아이템 생성 여부 결정자
    String tag;
    private String player_id; // main에서 받은 플레이어 id 저장할 변수

    private SensorManager sensorManager;//센서메니저
    private Sensor gravitySensor;          //센서값 읽어오기 자이로(기울기)
    List<Missile> mlist;                 //총알 리스트
    List<Missile2> mlist2;              // 적 총알 리스트
    List<Enemy>elist;                    //적 리스트
    List<Item> itemList;                 //아이템 리스트

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   //엑티비티 실행중 화면 꺼지는 걸 방지
        Intent reciver = getIntent();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);  //센서 메니저 얻기
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY); //그레비티 (회전)센서
        if (reciver.getExtras() != null) {
            point = reciver.getExtras().getInt("point");
            player_id = reciver.getStringExtra("id");
        }

            MyView view = new MyView(this);
            view.setFocusable(true); //키 이벤트를 받을 수 있도록 설정
            requestWindowFeature(Window.FEATURE_NO_TITLE); //타이틀 바를 숨김
            setContentView(view);


        //xml이 아닌 내부뷰 (커스텀 뷰)로 화면 이용

    }

    //센서값 얻어오기
    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if (sensor == gravitySensor) {
            int xaxis = (int) event.values[0];
            float yaxis = event.values[1];
            if (xaxis < 0) {
                x = x + 5 + speed;
                x = Math.min(width - gunshipWidth, x);
            }
            if (xaxis > 0) {
                x = x - 5 - speed;
                x = Math.max(0, x);   //큰값
            }
            if (yaxis < 6.9 && y > 0) {
                y = y - 5 - speed;
                y = Math.min(height - gunshipHeight, y);
            }
            if (yaxis > 6.9 && y < (height - gunshipHeight)) {
                y = y + 5 + speed;
                y = Math.max(0, y);
            }
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //리스너 등록
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_GAME);

    }

    //리스너 해제
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //내부 클래스
    class MyView extends View implements Runnable{
        boolean stopped=false;

        //생성자
        public MyView(Context context) {
            super(context);
            //img생성
            backImg=getResources().getDrawable(R.drawable.back0);
            gunship=getResources().getDrawable(R.drawable.gunship);
            missile=getResources().getDrawable(R.drawable.missile);
            missile2 = getResources().getDrawable(R.drawable.missile2);
            enemy=getResources().getDrawable(R.drawable.enemy);
            explousure=getResources().getDrawable(R.drawable.hit);
            item = getResources().getDrawable(R.drawable.mitem);
            mDouble = getResources().getDrawable(R.drawable.missiledouble);
            //sound생성
            fire=MediaPlayer.create(GameActivity.this,R.raw.fire);
            hit=MediaPlayer.create(GameActivity.this,R.raw.hit);
            bgmusic = MediaPlayer.create(GameActivity.this, R.raw.gamemusic);
            //리스트 생성
            mlist=new ArrayList<>();
            mlist2 = new ArrayList<>();
            elist=new ArrayList<>();
            itemList = new ArrayList<>();
            //백그라운드 스레드 생성 -> 그러면 run이 돌아간다.
            Thread th = new Thread(this);
            bgmusic.setLooping(true);
            bgmusic.start();
            th.start();
        }

        @Override
        public void run() {
            while (!stopped){
                Log.d(tag,"스레드시작");
                //적 좌표
                //사용자의 사각영역
                Rect rectG=new Rect(x,y,x+gunshipWidth,y+gunshipHeight);
                try {Log.d(tag,"적기 이동시작");
                    for (int enemy = 0; enemy < elist.size(); enemy++) {

                        Enemy e = elist.get(enemy);

                        //i번째 적    적을 생성 하게 되면 어레이리스트에 계속 쌓인다.
                        e.setEx(e.getEx()+e.getEnemyGo());
                        e.setEy(e.getEy() + e.getEnemyDown());
                        Random random = new Random();
                        if (1 == random.nextInt(100)) {   // 적 기체가 이동할때마다 1~99 랜덤변수를 출력하고 그것이 1과 같으면 미사일 발사
                            mx2 = e.getEx() + 20;   //
                            my2 = e.getEy();
                            Missile2 m2 = new Missile2(e.getEx() + 20, e.getEy() - 20);
                            mlist2.add(m2);
                        }
                        if (e.getEx() > width - enemyWidth) {
                            e.changeGo();//enemygo값이 -1을 곱한값이 됨 = 방향전환
                            //x좌표가 우측벽에 닿으면
                        }
                        if (e.getEx()<=0){
                            e.changeGo();
                            //x좌표가 좌측벽에 닿으면 방향전환
                        }
                        if (e.getEy() > height - enemyHeight) {  //y좌표가 맨 아래로 내려오면 다시 위로
                            e.setEy(ey);
                            e.changeDown();
                            e.seteState();                       //적의 횟수를 기록
                        }
                        Rect rectE = new Rect(e.getEx(), e.getEy(), e.getEx() + enemyWidth, e.getEy() + enemyHeight);
                        if (rectG.intersect(rectE)) { //적기와 내가 박았다?
                            hit.start();
                            isHit = true;
                            hx = x;
                            hy = y;//폭발한 x,y좌표 저장
                            stop();//일단 사용자가 박으면 스레드,배경 음악을 멈추게 함
                        }

                    }
                }catch (IndexOutOfBoundsException e){
                    e.printStackTrace();
                }
                //미사일 좌표
                for (int i = 0; i < mlist.size(); i++){
                    Missile m= mlist.get(i);  //i번째 총알    총알을 발사하게 되면 어레이리스트에 계속 쌓인다.
                    m.setMy(m.getMy() - mspeed);  //y좌표 감소 처리
                    if (m.getMy()<0){
                        mlist.remove(i);
                        //y좌표가 0보다 작으면 리스트에서 제거(총알이 맨 위에까지 올라가면)
                    }
                    //충돌여부 판정
                    //총알의 사각영역
                    Rect rectM=new Rect(m.getMx(),m.getMy(),m.getMx()+missileWidth,m.getMy()+missileHeight);
                    try {
                        for (int e=0; e<elist.size(); e++){
                            Enemy eCheck = elist.get(e);
                            Rect rectE = new Rect(eCheck.getEx(), eCheck.getEy(), eCheck.getEx() + enemyWidth, eCheck.getEy() + enemyHeight);
                            //겹치는 걸로 충돌 판정
                            if (rectE.intersect(rectM)) {  //겹쳐졌다?=>충돌
                                hit.start();             //폭발음 플레이
                                isHit = true;            //폭발 상태로 변경
                                point += 1;             //점수 증가
                                intomaker = true;
                                hx = eCheck.getEx();
                                hy = eCheck.getEy();
                                //폭발한 x,y좌표 저장
                                if(mlist.get(i)!=null) {
                                    mlist.remove(i);
                                }
                                //총알 리스트에서 총알을 제거
                                if(elist.get(e)!=null){
                                    elist.remove(e);}
                            }
                        }
                    }catch (IndexOutOfBoundsException e){
                        e.printStackTrace();
                    }
                }
                for (int k = 0; k < mlist2.size(); k++) {    //                                                        ## 수정된 부분 (301~324라인까지)
                    Missile2 m2 = mlist2.get(k);  //i번째 총알 적이 총알을 발사하게 되면 어레이리스트에 계속 쌓인다.
                    m2.setMy(m2.getMy() + 10);  //y좌표 증가 처리
                    if (m2.getMy() > height) {
                        mlist2.remove(k);
                        //y좌표가 화면보다 크면 리스트에서 제거(총알이 맨 아래까지 내려가면)
                    }
                    //충돌여부 판정
                    //총알의 사각영역
                    Rect rectM2 = new Rect(m2.getMx(), m2.getMy(), m2.getMx() + missileWidth, m2.getMy() + missileHeight);
                    try {
                        Rect rectU = new Rect(x, y, x + gunshipWidth, y + gunshipHeight);
                        //겹치는 걸로 충돌 판정
                        if (rectU.intersect(rectM2)) {  //겹쳐졌다?=>충돌
                            hit.start();             //폭발음 플레이
                            isHit = true;            //폭발 상태로 변경
                            intomaker = true;
                            hx = x;
                            hy = y;
                            //폭발한 x,y좌표 저장
                            if (mlist2.get(k) != null) {
                                mlist2.removeAll(mlist2);
                            }
                            stop();
                        }
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }

                //아이템 이동
                try {
                    for (int p = 0; p <= itemList.size(); p++) {
                        Item itemmov = itemList.get(p);
                        itemmov.setIy(itemmov.getIy() + 3);
                        if (itemmov.getIy() > height) {
                            itemList.remove(p);
                        }
                        Rect itemrect = new Rect(itemmov.getIx(), itemmov.getIy(), itemmov.getIx() + itemWidth, itemmov.getIy() + itemHeight);
                        if (itemrect.intersect(rectG)) {

                            switch (itemmov.getState()) {
                                case 0:
                                    bulletcount = 15;
                                    if (mspeed <= 20) {
                                        mspeed += 5;
                                    }
                                    break;
                                case 1:
                                    speed++;


                                    break;
                            }
                            itemList.remove(p);
                        }

                    }
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(15);               //잠깐 화면을 멈추고
                }catch (Exception e){
                    e.printStackTrace();
                }
                postInvalidate();
                //화면 갱신 요청 => onDraw()가 호출됨 그림을 다시 새로 스아악 그린다.
            }


        }

        public void itemMake() {
            Random random = new Random();
            Item item = new Item(random.nextInt(width - itemWidth), 0, random.nextInt(2));
            itemList.add(item);
            maker = false;
        }


        public void stop(){
            stopped = true;
            bgmusic.release();
            Intent sender = new Intent(getApplicationContext(), Gameover.class);
            sender.putExtra("point", point);
            sender.putExtra("id", player_id);
            startActivityForResult(sender, 101);
            startActivity(sender);
            //게임오버 화면 전환 애니메이션 없애기
            finish();                                                           //현재 엑티비티 종료

        }

        //화면 사이즈가 변경될 때( 최초 가로, 최초 세로, 전환 가로, 전환 세로)
        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            //화면의 가로,세로 폰을 기준으로 맞춘다.
            width=getWidth();
            height=getHeight();

            //이미지의 가로,세로 길이
            gunshipWidth=gunship.getIntrinsicWidth();
            gunshipHeight=gunship.getIntrinsicHeight();
            missileWidth=missile.getIntrinsicWidth();
            missileHeight=missile.getIntrinsicHeight();
            mdWidth = mDouble.getIntrinsicWidth();
            mdHeight = mDouble.getIntrinsicHeight();
            enemyWidth=enemy.getIntrinsicWidth();
            enemyHeight=enemy.getIntrinsicHeight();
            hitWidth=explousure.getIntrinsicWidth();
            hitHeight=explousure.getIntrinsicHeight();
            itemWidth = item.getIntrinsicWidth();
            itemHeight = item.getIntrinsicHeight();


            //비행기 좌표
            x = (width/2) - (gunshipWidth/2);   //정중앙
            y = height - gunshipHeight;

            //미사일 초기좌표
            mx = x+20;
            my = y;
            //적 초기 좌표
            ey = 0;
            //초기에 적 한마리 생성
            if (elist.size() <= 0) {
                Random random = new Random();
                Enemy e = new Enemy(random.nextInt(width - enemyWidth) + 1, ey, random.nextInt(3));
                elist.add(e);
            }
        }

        @Override
        protected  void onDraw(Canvas canvas) {
            Log.d(tag,"드로우 시작");
            if (bulletcount <= 0) {
                bulletcount = 0;
            }
            //배경 이미지 출력
            //setBounds(x1,y1,x2,y2) 영역 지정
            backImg.setBounds(0,0,width,height);
            backImg.draw(canvas); // 배경을 캔버스에 출력
            //사용자 비행기 출력
            gunship.setBounds(x, y, x + gunshipWidth, y + gunshipHeight);
            gunship.draw(canvas);

            if(isHit) {      //폭발 상태
                //폭발 이미지 출력
                explousure.setBounds(hx - 20, hy - 20, hx + hitWidth - 20, hy + hitHeight - 20);
                explousure.draw(canvas);
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                isHit = false; //폭발하지 않은 상태로 전환
            }
            //적 생성
            if (elist.size() <= 0) {
                for (int j = 0; j <= point / 2; j++) {
                    Random random = new Random();
                    Enemy enemy = new Enemy(random.nextInt(width - enemyWidth) + 1, ey, random.nextInt(3));
                    Missile2 m2 = new Missile2(enemy.getEx() + 20, ey);
                    mlist2.add(m2);
                    if (j % 2 != 1) {
                        enemy.changeGo();
                    }
                    if (point != 0 && point % 2 == 0) {
                        enemy.changeSetDown((point / 2));
                    }
                    enemy.changeSetDown(j);
                    elist.add(enemy);
                }//적 기체가 완전히 없어졌을때 다시 점수에 비례해서 그림
            }
            //적 출력
            for(int i=0; i<elist.size(); i++){
                Enemy e=elist.get(i);
                Drawable tenemy = getResources().getDrawable(R.drawable.enemy);
                switch (e.geteType()) {
                    case 0:
                        tenemy = getResources().getDrawable(R.drawable.enemy);
                        break;
                    case 1:
                        tenemy = getResources().getDrawable(R.drawable.enemy2);
                        break;
                    case 2:
                        tenemy = getResources().getDrawable(R.drawable.enemy3);
                        break;
                }
                tenemy.setBounds(e.getEx(), e.getEy(), e.getEx() + enemyWidth, e.getEy() + enemyHeight);
                tenemy.draw(canvas);
                if (e.geteState() >= 3) {
                    e.reseteState();
                    maker = true;///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                }
            }
            if (maker) {
                itemMake();
            }
            try {
                for (int i = 0; i <= itemList.size(); i++) {
                    Item ie = itemList.get(i);
                    switch (ie.getState()) {
                        case 0:
                            item = getResources().getDrawable(R.drawable.mitem);
                            break;
                        case 1:
                            item = getResources().getDrawable(R.drawable.speed);
                            break;
                    }
                    item.setBounds(ie.getIx(), ie.getIy(), ie.getIx() + itemWidth, ie.getIy() + itemHeight);
                    item.draw(canvas);
                }
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
            //총알 출력

            for (int i = 0; i< mlist.size(); i++){
                Missile m = mlist.get(i);
                switch (m.getType()) {//i번쨰 총알
                    case 1:
                        missile.setBounds(m.getMx(), m.getMy(), m.getMx() + missileWidth, m.getMy() + missileHeight); //총알 이미지 출력 범위
                        missile.draw(canvas);
                        break;
                    case 2:
                        mDouble.setBounds(m.getMx(), m.getMy(), m.getMx() + mdWidth, m.getMy() + mdHeight);
                        mDouble.draw(canvas);
                        break;
                }// i번째 총알 츨력
            }
            //적총알
            for (int k = 0; k < mlist2.size(); k++) {      //                                     ## 수정된 부분  (522~526 라인까지)
                Missile2 m2 = mlist2.get(k);      //k번쨰 총알
                missile2.setBounds(m2.getMx(), m2.getMy(), m2.getMx() + missileWidth, m2.getMy() + missileHeight); //총알 이미지 출력 범위
                missile2.draw(canvas);      // i번째 총알 츨력
            }



            //점수 출력
            String str = "POINT : "+ point;
            String mcount = "BULLET : " + bulletcount;
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setTextSize(60);      //폰트 사이즈
            canvas.drawText(str,width/2,40,paint);
            canvas.drawText(mcount, 0, 40, paint);
            Log.d(tag,"드로우 끝");

            if (point > 0 && point % 3 == 0) {
                if (intomaker) {
                    maker = true;
                    intomaker = false;
                }
            }
            super.onDraw(canvas);
        }
        //키 이벤트 처리

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            Log.d(tag,"터치");
            if (bulletcount > 0) {
                isFire = true;  //발사로 전환
                fire.start();   //발사 소리 출력
                Missile ms = new Missile(x + gunshipWidth / 2, y);
                bulletcount -= 1;
                if (mspeed > 20) {
                    ms.setType();
                }
                mlist.add(ms);
            }
            postInvalidate();
            return super.onTouchEvent(event);
        }
    }

}
