package com.dante.diary.model;


import android.support.annotation.Nullable;

import com.dante.diary.base.Constants;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

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

    public static RealmResults<Diary> findDiaries(Realm realm, String type) {
        realm = initRealm(realm);
//        if (Constants.FAVORITE.equals(type)) {
//            return findFavoriteDiaries(realm);
//        }
        return realm.where(Diary.class)
                .findAllSorted("created");
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
}
