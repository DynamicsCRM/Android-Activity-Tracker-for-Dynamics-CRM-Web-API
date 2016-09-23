package com.microsoft.activitytracker.data.loaders;

import android.content.Context;
import android.support.v4.util.ArrayMap;

import com.microsoft.activitytracker.classes.models.Entity;
import com.microsoft.activitytracker.data.BaseDataManager;

import com.microsoft.activitytracker.data.storage.DefinitionEntry;
import com.microsoft.xrm.EntityCollection;
import com.microsoft.xrm.EntityDefinition;


import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;
import rx.functions.Action1;

public abstract class BaseEntityLoader extends BaseDataManager {

    public BaseEntityLoader(Context context) {
        super(context);
    }

    public abstract void onDataLoaded(List<Entity> retrieveResponse);

    Action1<Response<EntityCollection>> retrieveMultipleResponse = response -> {
        if (response.isSuccessful() && response.body() != null) {
            EntityCollection collection = response.body();

            EntityDefinition definition = null;
            String collectionName = collection.getContext().split("#|\\?|\\(|\\)")[1];
            ArrayMap<String, EntityDefinition> definitions = (ArrayMap<String, EntityDefinition>)DefinitionEntry.getDefinitions();

            if (collection.getEntities().size() > 0 && !collectionName.equals("activitypointers")) {
                int size = definitions.size();

                for (int i = 0; i < size; i++) {
                    EntityDefinition entityDefinition = definitions.valueAt(i);
                    if (entityDefinition.getLogicalCollectionName().equals(collectionName)) {
                        definition = entityDefinition;
                        break;
                    }
                }
            }

            int size = collection.getEntities().size();
            List<Entity> entities = new ArrayList<>(size);
            for (int i = 0; i< size; i++) {
                com.microsoft.xrm.Entity entity = collection.getEntities().get(i);

                if (definition == null || collectionName.equals("activitypointers")) {
                    definition = definitions.get(entity.get("activitytypecode").toString());
                }

                entities.add(new Entity.Builder()
                    .setLogicalName(definition.getLogicalName())
                    .setEntitySuper(entity)
                    .setEntityDefinition(definition)
                    .build());
            }

            decrementLoadingCount();
            onDataLoaded(entities);
            loadFinished();
        }
        else {
            decrementLoadingCount();
            try {
                loadFailed(response.errorBody().string());
            }
            catch(Exception ex) {
                loadFailed(ex.getMessage());
            }
        }
    };

}
