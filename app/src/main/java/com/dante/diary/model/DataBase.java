package com.dante.diary.model;


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

    public Diary getById(int id) {
        return getById(id, Diary.class);
    }

    private <T extends RealmObject> RealmResults<T> findAll(Class<T> realmObjectClass) {
        realm = initRealm(realm);
        return realm.where(realmObjectClass).findAll();
    }

    public void save(RealmObject realmObject) {
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(realmObject);
        realm.commitTransaction();
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
        realm.beginTransaction();
        realm.delete(Diary.class);
        realm.commitTransaction();
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
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(realmObjects);
        realm.commitTransaction();
    }

    public void close() {
        realm.removeAllChangeListeners();
        realm.close();
    }
}
