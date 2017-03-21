package com.dante.diary.model;


import android.support.annotation.Nullable;

import com.dante.diary.base.Constants;
import com.dante.diary.utils.DateUtil;

import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Deals with cache, data
 */
public class DataBase {
    private static Realm initRealm(Realm realm) {
        if (realm == null || realm.isClosed()) {
            realm = Realm.getDefaultInstance();
        }
        return realm;
    }

    public static <T extends RealmObject> void save(Realm realm, List<T> realmObjects) {
        realm = initRealm(realm);
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(realmObjects);
        realm.commitTransaction();
    }

    public static <T extends RealmObject> T getById(Realm realm, int id, Class<T> realmObjectClass) {
        realm = initRealm(realm);
        return realm.where(realmObjectClass).equalTo(Constants.ID, id).findFirst();
    }

    private static <T extends RealmObject> RealmResults<T> findAll(Realm realm, Class<T> realmObjectClass) {
        realm = initRealm(realm);
        return realm.where(realmObjectClass).findAll();
    }

    public static void save(Realm realm, RealmObject realmObject) {
        if (realmObject == null) {
            return;
        }
        realm = initRealm(realm);
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(realmObject);
        realm.commitTransaction();
    }


    public static Diary getById(Realm realm, int id) {
        return getById(realm, id, Diary.class);
    }

    public static boolean hasDiary(@Nullable Realm realm, String url) {
        return getByUrl(realm, url) != null;
    }


    public static Diary getByUrl(Realm realm, String url) {
        realm = initRealm(realm);
        return realm.where(Diary.class).equalTo(Constants.URL, url).findFirst();
    }

    public static RealmResults<Diary> allDiaries(Realm realm, String type) {
        realm = initRealm(realm);
//        if (Constants.FAVORITE.equals(type)) {
//            return findFavoriteDiaries(realm);
//        }
        return realm.where(Diary.class)
                .findAllSorted("created", Sort.DESCENDING);
    }

    public static RealmResults<Diary> findFavoriteDiaries(Realm realm) {
        realm = initRealm(realm);
        return realm.where(Diary.class)
                .equalTo("isLiked", true)
                .findAll();
    }

    public static void clearAllDiaries() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.delete(Diary.class);
        realm.commitTransaction();
    }

    public static User findUser(Realm realm, int id) {
        realm = initRealm(realm);
        return realm.where(User.class).equalTo(Constants.ID, id).findFirst();
    }

    public static RealmResults<Diary> findTodayDiaries(Realm realm) {
        realm = initRealm(realm);
        Date today = new Date();
        return realm.where(Diary.class)
                .between("created", DateUtil.getStartOfDate(today), DateUtil.getEndOfDate(today))
                .findAllSorted("created", Sort.DESCENDING);
    }

    public static RealmResults<Notebook> findNotebooks(Realm realm, int userId) {
        realm = initRealm(realm);

        return realm.where(Notebook.class).equalTo("userId", userId).findAll();
    }

    public static RealmResults<Diary> findDiariesOfNotebook(Realm realm, int notebookId) {
        realm = initRealm(realm);
        return realm.where(Diary.class).equalTo("notebookId", notebookId).findAll();
    }

    public static Diary findDiary(Realm realm, int diaryId) {
        realm = initRealm(realm);

        return realm.where(Diary.class).equalTo(Constants.ID, diaryId).findFirst();
    }

    public static List<Comment> findComments(Realm realm, int diaryId) {
        realm = initRealm(realm);

        return realm.where(Comment.class).equalTo("dairyId", diaryId).findAll();
    }

    public static Notebook findNotebook(Realm realm, int notebookId) {
        realm = initRealm(realm);
        return realm.where(Notebook.class).equalTo(Constants.ID, notebookId).findFirst();

    }
}
