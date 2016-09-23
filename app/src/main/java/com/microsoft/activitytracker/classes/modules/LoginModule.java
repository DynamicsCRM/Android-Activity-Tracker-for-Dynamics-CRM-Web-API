package com.microsoft.activitytracker.classes.modules;

import android.support.annotation.NonNull;

import com.microsoft.activitytracker.classes.scopes.LoginScope;
import com.microsoft.activitytracker.util.StringConverterFactory;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import rx.Observable;

@Module
public class LoginModule {

    private String authority;

    public LoginModule(@NonNull String authority) {
        this.authority = authority;
    }

    public interface AuthorityService {
        @Headers({
                "Accept: application/x-www-form-urlencoded",
                "Authorization: Bearer" })
        @GET("/api/data/v8.0/")
        Observable<Response<String>> getAuthority();
    }

    @Provides
    @LoginScope
    OkHttpClient provideOkHttpClient() {
        return new OkHttpClient.Builder()
                .build();
    }

    @Provides
    @LoginScope
    Retrofit provideRetrofit(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
            .baseUrl(authority.endsWith("/") ? authority : authority + "/")
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(new StringConverterFactory())
            .client(okHttpClient)
            .build();
    }

    @Provides
    @LoginScope
    AuthorityService provideAuthorityEndpoint(Retrofit retrofit) {
        return retrofit.create(AuthorityService.class);
    }

}
