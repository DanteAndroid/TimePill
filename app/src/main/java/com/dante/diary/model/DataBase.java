package com.dante.diary.model;


import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.dante.diary.base.Constants;
import com.dante.diary.interfaces.QueryResultCallback;
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

    public Realm realm;

    private DataBase(Realm realm) {
        this.realm = realm;
    }

    public static DataBase getInstance() {
        return new DataBase(Realm.getDefaultInstance());
    }

    private static Realm initRealm(Realm realm) {
        if (realm == null || realm.isClosed()) {
            realm = Realm.getDefaultInstance();
        }
        return realm;
    }

    public static void findTimePillUser(int id, QueryResultCallback callback) {
        AVQuery<AVObject> query = new AVQuery<>(Constants.TP_USER);
        query.whereEqualTo(Constants.ID, id);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (list == null || list.isEmpty()) {
                    callback.notExist();
                } else {
                    callback.onExist();
                }
            }
        });
    }

    public Diary getById(int id) {
        return getById(id, Diary.class);
    }

    private <T extends RealmObject> RealmResults<T> findAll(Class<T> realmObjectClass) {
        realm = initRealm(realm);
        return realm.where(realmObjectClass).findAll();
    }

    public void save(RealmObject realmObject) {
        realm.executeTransaction(realm1 -> realm1.copyToRealmOrUpdate(realmObject));
    }

    public boolean hasDiary(String url) {
        return getByUrl(url) != null;
    }

    public Diary getByUrl(String url) {
        return realm.where(Diary.class).equalTo(Constants.URL, url).findFirst();
    }

    public RealmResults<Diary> findFavoriteDiaries() {
        return realm.where(Diary.class)
                .equalTo("isLiked", true)
                .findAll();
    }

    public void clearAllDiaries() {
        realm.executeTransaction(realm1 -> realm1.delete(Diary.class));
    }

    public void clearAll() {
        realm.executeTransaction(realm1 -> realm1.deleteAll());
    }

    public User findUser(int id) {
        return realm.where(User.class).equalTo(Constants.ID, id).findFirst();
    }

    public RealmResults<Diary> findTodayDiaries() {
        Date today = new Date();
        return realm.where(Diary.class)
                .between("created", DateUtil.getStartOfDate(today), DateUtil.getEndOfDate(today))
                .isNotNull("user")
                .findAllSorted("created", Sort.DESCENDING);
    }

    public RealmResults<Notebook> findNotebooks(int userId) {
        return realm.where(Notebook.class).equalTo("userId", userId).findAll();
    }

    public RealmResults<Diary> findDiariesOfNotebook(int notebookId) {
        return realm.where(Diary.class).equalTo("notebookId", notebookId).findAll();
    }

    public Diary findDiary(int diaryId) {
        return realm.where(Diary.class).equalTo(Constants.ID, diaryId).findFirst();
    }

    public List<Comment> findComments(int diaryId) {
        return realm.where(Comment.class).equalTo("dairyId", diaryId).findAll();
    }

    public Notebook findNotebook(int notebookId) {
        return realm.where(Notebook.class).equalTo(Constants.ID, notebookId).findFirst();

    }

    public <T extends RealmObject> T getById(int id, Class<T> realmObjectClass) {
        realm = initRealm(realm);
        return realm.where(realmObjectClass).equalTo(Constants.ID, id).findFirst();
    }

    public <T extends RealmObject> void save(List<T> realmObjects) {
        realm.executeTransaction(r -> r.copyToRealmOrUpdate(realmObjects));
    }

    public void close() {
        realm.removeAllChangeListeners();
        realm.close();
    }

    public void deleteDiary(int diaryId) {
        realm.executeTransaction(r ->
                r.where(Diary.class).equalTo(Constants.ID, diaryId).findAll().deleteAllFromRealm());
    }
}
