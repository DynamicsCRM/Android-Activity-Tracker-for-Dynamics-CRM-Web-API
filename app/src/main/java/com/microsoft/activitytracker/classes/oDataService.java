package com.microsoft.activitytracker.classes;

import com.microsoft.xrm.BaseResponse;
import com.microsoft.xrm.DefinitionMetadataResponse;
import com.microsoft.xrm.Entity;
import com.microsoft.xrm.EntityCollection;

import java.util.Map;
import java.util.UUID;

import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;
import rx.Observable;

/**
 * oData Service for Web Api Version 2, Responses are as follows:
 * 200 : OKAY
 * 201 : CREATED
 * 202 : ACCEPTED (accepted but not finished)
 * 204 : NO CONTENT
 * 3xx : REDIRECTION
 * 304 : NOT MODIFIED
 */
public interface oDataService {

    @GET("/api/data/v8.0/{logicalName}")
    Observable<Response<EntityCollection>> retrieveMultiple(@Path("logicalName") String logicalName,
                                                            @QueryMap Map<String, String> query);

    /**
     * Used for paging through results retrieve multiple returns a reference url that can be used to
     * get the next page, this is the endpoint to use for that.
     * @param url located in the next page parameter of a Retrieve Multiple request
     * @return Next page's collection of entities
     */
    @GET
    Observable<Response<EntityCollection>> retrieveMultiplePage(@Url String url);

    @GET("/api/data/v8.0/{logicalName}({guid})")
    Observable<Response<Entity>> retrieve(@Path("logicalName") String logicalName,
                                          @Path("guid") UUID guid,
                                          @QueryMap Map<String, String> query);

    /**
     * @param logicalName of the entity you are looking for
     * @param guid id of the entity you want the attribute from
     * @param attributeName name of the attribute or reference you want to retrieve
     * @return the single attribute you specify in the path
     */
    @GET("/api/data/v8.0/{logicalName}({guid})/{relationship}")
    Observable<Response<EntityCollection>> retrieveRelationship(@Path("logicalName") String logicalName,
                                              @Path("guid") UUID guid,
                                              @Path("relationship") String attributeName,
                                              @QueryMap Map<String, String> query);

    /**
     *
     * @param eTag the eTag that was returned for the request
     * @return returns a response, if it is 304 then it is unmodified
     */
    @GET("/api/data/v8.0/{logicalName}({guid})")
    Observable<Response<Entity>> hasChanged(@Path("logicalName") String logicalName,
                                              @Path("guid") UUID guid,
                                              @QueryMap Map<String, String> query,
                                              @Header("If-None-Match") String eTag);


    @Headers({ "Content-Type: application/json; charset=utf-8" })
    @POST("/api/data/v8.0/{logicalName}")
    Observable<Response<BaseResponse>> create(@Path("logicalName") String logicalName,
                                              @Body Object newEntity);

    @Headers({ "Content-Type: application/json" })
    @PATCH("/api/data/v8.0/{logicalName}({guid})")
    Observable<Response<?>> update(@Path("logicalName") String logicalName,
                                   @Path("guid") UUID guid,
                                   @Body Object object);

    @Headers({ "Content-Type: application/json" })
    @PUT("/api/data/v8.0/{logicalName}({guid})/{attribute}")
    Observable<Response<?>> updateAttribute(@Path("logicalName") String logicalName,
                                            @Path("guid") UUID guid,
                                            @Path("attribute") String attributeName,
                                            @Body Object value);

    @Headers({ "Content-Type: application/json" })
    @DELETE("/api/data/v8.0/{logicalName}({guid})")
    Observable<Response<?>> delete(@Path("logicalName") String logicalName,
                                   @Path("guid") UUID guid);

    @Headers({ "Content-Type: application/json" })
    @DELETE("/api/data/v8.0/{logicalName}({guid})/{attribute}")
    Observable<Response<?>> deleteAttribute(@Path("logicalName") String logicalName,
                                            @Path("guid") UUID guid,
                                            @Path("attribute") String attributeName);

    @Headers({
        "Content-Type: application/json",
        "If-Match: \"*\""
    })
    @PATCH("/api/data/v8.0/{logicalName}({guid})")
    Observable<Response<?>> upsert(@Path("logicalName") String logicalName,
                                   @Path("guid") UUID guid,
                                   @Body Object object);

    @Headers({
        "Content-Type: application/json",
        "If-None-Match: \"*\""
    })
    @PATCH("/api/data/v8.0/{logicalName}({guid})")
    Observable<Response<?>> upsertNoUpdate(@Path("logicalName") String logicalName,
                                           @Path("guid") UUID guid,
                                           @Body Object object);

    @GET("/api/data/v8.0/EntityDefinitions")
    Observable<Response<DefinitionMetadataResponse>> getEntityDefinitions(@QueryMap Map<String, String> query);

}
