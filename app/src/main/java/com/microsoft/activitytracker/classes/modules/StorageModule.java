package com.microsoft.activitytracker.classes.modules;

import android.content.SharedPreferences;

import com.microsoft.activitytracker.classes.scopes.StorageScope;

import java.util.Date;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public class StorageModule {

    public static final String TOKEN = "tokenId";
    public static final String EXPIRES_ON = "expiresOn";
    public static final String REFRESH_TOKEN = "refreshId";
    public static final String ENDPOINT = "endpoint";
    public static final String USERNAME = "username";
    public static final String AUTHORITY = "authority";

    public StorageModule() {

    }

    @Provides @Named(TOKEN)
    @StorageScope
    String providesToken(SharedPreferences sharedPreferences) {
        return sharedPreferences.getString(TOKEN, "");
    }

    @Provides
    long providesExpiresOn(SharedPreferences sharedPreferences) {
        return sharedPreferences.getLong(EXPIRES_ON, new Date().getTime());
    }

    @Provides @Named(REFRESH_TOKEN)
    @StorageScope
    String providesRefreshToken(SharedPreferences sharedPreferences) {
        return sharedPreferences.getString(REFRESH_TOKEN, "");
    }

    @Provides @Named(ENDPOINT)
    @StorageScope
    String providesEndpoint(SharedPreferences sharedPreferences) {
        return sharedPreferences.getString(ENDPOINT, "");
    }

    @Provides @Named(USERNAME)
    @StorageScope
    String providesUsername(SharedPreferences sharedPreferences) {
        return sharedPreferences.getString(USERNAME, "");
    }

    @Provides @Named(AUTHORITY)
    @StorageScope
    String providesAuthority(SharedPreferences sharedPreferences) {
        return sharedPreferences.getString(AUTHORITY, "");
    }

}
