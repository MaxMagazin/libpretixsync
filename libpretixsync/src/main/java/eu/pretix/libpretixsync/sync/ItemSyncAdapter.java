package eu.pretix.libpretixsync.sync;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import eu.pretix.libpretixsync.api.PretixApi;
import eu.pretix.libpretixsync.db.Item;
import io.requery.BlockingEntityStore;
import io.requery.Persistable;

public class ItemSyncAdapter extends BaseConditionalSyncAdapter<Item, Long> {
    public ItemSyncAdapter(BlockingEntityStore<Persistable> store, FileStorage fileStorage, String eventSlug, PretixApi api) {
        super(store, fileStorage, eventSlug, api);
    }

    @Override
    public void updateObject(Item obj, JSONObject jsonobj) throws JSONException {
        obj.setEvent_slug(eventSlug);
        obj.setServer_id(jsonobj.getLong("id"));
        obj.setPosition(jsonobj.getLong("position"));
        obj.setCategory_id(jsonobj.optLong("category"));
        obj.setAdmission(jsonobj.optBoolean("admission", false));
        obj.setActive(jsonobj.optBoolean("active", true));
        obj.setJson_data(jsonobj.toString());
    }

    @Override
    Iterator<Item> getKnownObjectsIterator() {
        return store.select(Item.class)
                .where(Item.EVENT_SLUG.eq(eventSlug))
                .get().iterator();
    }

    @Override
    String getResourceName() {
        return "items";
    }

    @Override
    Long getId(JSONObject obj) throws JSONException {
        return obj.getLong("id");
    }

    @Override
    Long getId(Item obj) {
        return obj.getServer_id();
    }

    @Override
    Item newEmptyObject() {
        return new Item();
    }
}
