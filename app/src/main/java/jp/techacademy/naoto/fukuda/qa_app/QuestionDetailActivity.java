package jp.techacademy.naoto.fukuda.qa_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class QuestionDetailActivity extends AppCompatActivity {

    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;

    FloatingActionButton fab;
    FloatingActionButton fab2;

    private DatabaseReference dataBaseReference;
    private DatabaseReference mAnswerRef;
    private DatabaseReference mBookmarkRef;
    private String mUid;
    private String mQuestionUid;
    private int mGenre = 0;
    private boolean bookmarkFlag = false; // true=お気に入り登録済み。

    private ChildEventListener mBookmarkEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            String Bookmarkid = (String) dataSnapshot.getValue();
            if(Bookmarkid != null ){ //想定通りBookmarkidがonClickで追加されていた時。
                bookmarkFlag = true;
                fab2.setPressed(true);
                findViewById(R.id.goldstar).setVisibility(View.VISIBLE);

                Log.d("ANDROID", "bookmark Flag @ChildAdded=" + bookmarkFlag);
                Log.d("ANDROID", "Bookmarkid @ChildAdded=" + Bookmarkid);

            }else {
                bookmarkFlag = false;
                fab2.setPressed(false);
                findViewById(R.id.goldstar).setVisibility(View.GONE);
                Log.d("ANDROID", "bookmark Flag @ChildAdded=" + bookmarkFlag);
                Log.d("ANDROID", "Bookmarkid @ChildAdded=" + Bookmarkid);
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            String Bookmarkid = (String) dataSnapshot.getValue();
            if(Bookmarkid != null){ //想定通りBookmarkidがonClickでremoveされていた時。
                bookmarkFlag = false;
                fab2.setPressed(false);
                findViewById(R.id.goldstar).setVisibility(View.GONE);
                Log.d("ANDROID", "bookmark Flag @ChildRemoved=" + bookmarkFlag);
                Log.d("ANDROID", "Bookmarkid @ChildRemoved=" + Bookmarkid);

            }else {
                bookmarkFlag = true;
                fab2.setPressed(true);
                findViewById(R.id.goldstar).setVisibility(View.VISIBLE);
                Log.d("ANDROID", "bookmark Flag @ChildRemoved=" + bookmarkFlag);
                Log.d("ANDROID", "Bookmarkid @ChildRemoved=" + Bookmarkid);
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            String answerUid = dataSnapshot.getKey();

            for(Answer answer : mQuestion.getAnswers()) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid.equals(answer.getAnswerUid())) {
                    return;
                }
            }

            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");

            Answer answer = new Answer(body, name, uid, answerUid);
            mQuestion.getAnswers().add(answer);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        // 渡ってきたQuestionのオブジェクトを保持する
        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");

        setTitle(mQuestion.getTitle());

        // ログイン済みのユーザーを取得する
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            // ログインしていなければお気に入り用ボタン、星を消す。
            findViewById(R.id.fab2).setVisibility(View.GONE);
            findViewById(R.id.goldstar).setVisibility(View.GONE);
        } else {
            DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
            mBookmarkRef = dataBaseReference.child(Const.BookmarkPATH).child(user.getUid()).child(mQuestion.getQuestionUid());
            mBookmarkRef.addChildEventListener(mBookmarkEventListener);

            if(bookmarkFlag != true){ //フラグ別星の有無
                findViewById(R.id.goldstar).setVisibility(View.GONE);
            }else{
                findViewById(R.id.goldstar).setVisibility(View.VISIBLE);
            }


        }

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionDetailListAdapter(this, mQuestion);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ログイン済みのユーザーを取得する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    // Questionを渡して回答作成画面を起動する
                    // --- ここから ---
                    Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
                    intent.putExtra("question", mQuestion);
                    startActivity(intent);
                    // --- ここまで ---
                }
            }
        });

        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bookmarkFlag) {
                    /*Firebase datareferenceのメソッドremoveValue()でdataをFirebaseから削除。
                    onClickメソッドが全て終わった時点でChildEventListener onChildRemovedへ移行。*/
                    mBookmarkRef.removeValue();
                    Snackbar.make(findViewById(android.R.id.content), "お気に入り解除しました。", Snackbar.LENGTH_LONG).show();

                    Log.d("ANDROID", "bookmark Flag @onClick=" + bookmarkFlag);
                }else {

                    //bookmarkFlag = false 即ち bookmarkされていない場合。
                    //HashMapクラスのインスタンスを作り、キーと値をputメソッドで格納(登録)
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("BookmarkQid", mQuestion.getQuestionUid());

                    /*Firebase datareferenceのメソッドでdataをFirebaseへ登録。
                    onClickメソッドが全て終わった時点でChildEventListener onChildAddedへ移行。*/
                    mBookmarkRef.setValue(data);
                    Snackbar.make(findViewById(android.R.id.content), "お気に入りに登録しました。", Snackbar.LENGTH_LONG).show();
                    Log.d("ANDROID", "bookmark Flag @onClick=" + bookmarkFlag);

                }
            }
        });

        DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
        mAnswerRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
        mAnswerRef.addChildEventListener(mEventListener);

    }
}
