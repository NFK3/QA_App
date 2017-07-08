package jp.techacademy.naoto.fukuda.qa_app;

import java.io.Serializable;
/**
 * Created by naotofukuda on 26/6/2017.
 */
public class Bookmark implements Serializable {

    private String mUid;
    private String mQuestionUid;

    public Bookmark(String uid, String questionUid) {

        mUid = uid;
        mQuestionUid = questionUid;

    }

    public String getUid() {

        return mUid;
    }

    public String getQuestionUid() {

        return mQuestionUid;
    }





}

