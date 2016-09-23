package com.microsoft.activitytracker.classes.modules;

import android.support.annotation.NonNull;

import com.microsoft.activitytracker.classes.oDataService;
import com.microsoft.activitytracker.classes.scopes.OrgScope;

import dagger.Module;
import dagger.Provides;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Module
public class OrgServiceModule {

    private String endpoint;
    private String sessionToken;

    public OrgServiceModule(@NonNull String endpoint, @NonNull String sessionToken) {
        this.endpoint = endpoint;
        this.sessionToken = sessionToken;
    }

    @Provides
    @OrgScope
    Interceptor provideInterceptor() {
        return chain -> {
            Request request = chain.request();

            Request.Builder builder = request.newBuilder()
                .addHeader("Accept", "application/json")
                .addHeader("OData-MaxVersion", "4.0")
                .addHeader("OData-Version", "4.0")
                .addHeader("User-Agent", "Microsoft Android Sample")
                .addHeader("Prefer", "odata.include-annotations=\"*\"")
                .addHeader("Authorization", "Bearer " + sessionToken.replaceAll("(\\r|\\n)", ""));

            return chain.proceed(builder.build());
        };
    }

    @Provides
    @OrgScope
    OkHttpClient provideOkHttpClient(Interceptor interceptor) {
        return new OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build();
    }

    @Provides
    @OrgScope
    Retrofit provideRetrofit(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
            .baseUrl(endpoint.endsWith("/") ? endpoint : endpoint + "/")
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(JacksonConverterFactory.create())
            .client(okHttpClient)
            .build();
    }

    @Provides
    @OrgScope
    oDataService provideService(Retrofit retrofit) {
        return retrofit.create(oDataService.class);
    }

}
