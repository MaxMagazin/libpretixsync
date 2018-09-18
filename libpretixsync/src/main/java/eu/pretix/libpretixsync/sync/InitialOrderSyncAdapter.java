package eu.pretix.libpretixsync.sync;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import eu.pretix.libpretixsync.api.ApiException;
import eu.pretix.libpretixsync.api.PretixApi;
import eu.pretix.libpretixsync.api.ResourceNotModified;
import eu.pretix.libpretixsync.db.Item;
import eu.pretix.libpretixsync.db.Order;
import eu.pretix.libpretixsync.db.OrderPosition;
import eu.pretix.libpretixsync.db.ResourceLastModified;
import eu.pretix.libpretixsync.utils.JSONUtils;
import io.requery.BlockingEntityStore;
import io.requery.Persistable;

public class InitialOrderSyncAdapter extends OrderSyncAdapter {
    public InitialOrderSyncAdapter(BlockingEntityStore<Persistable> store, FileStorage fileStorage, String eventSlug, PretixApi api) {
        super(store, fileStorage, eventSlug, api);
    }

    public interface OnInitialOrderSyncProgressListener {
        void onInitialOrderSyncProgress(int percentage);
    }

    private OnInitialOrderSyncProgressListener mProgressListener;

    public void saveOrder(JSONObject jsonobj) throws JSONException {
        Order order = newEmptyObject();
        order.setEvent_slug(eventSlug);
        order.setCode(jsonobj.getString("code"));
        order.setStatus(jsonobj.getString("status"));
        order.setEmail(jsonobj.optString("email"));
        order.setCheckin_attention(jsonobj.optBoolean("checkin_attention"));
        order.setJson_data(jsonobj.toString());
        order.getCompanyNameFromJson();

        if (order.getId() == null) {
            store.insert(order);
        }

        JSONArray positionsArray = jsonobj.getJSONArray("positions");
        List<OrderPosition> inserts = new ArrayList<>();
        for (int i = 0; i < positionsArray.length(); i++) {
            JSONObject posjson = positionsArray.getJSONObject(i);

            OrderPosition orderPosition = new OrderPosition();
            orderPosition.setOrder(order);

            updatePositionObject(orderPosition, posjson);
            inserts.add(orderPosition);
        }
        store.insert(inserts);
    }


    public void initialDownload() throws JSONException, ApiException {
        List<Order> existingOrders = store.select(Order.class).limit(5).get().toList();

        if (existingOrders == null || existingOrders.isEmpty()) { //use custom logic for initial download

            String url = api.eventResourceUrl(getResourceName());

            boolean isFirstPage = true;
            int processedOrdersNumber = 0;
            while (true) {

                JSONObject page = null;
                try {
                    page = downloadPage(url, isFirstPage);
                } catch (ResourceNotModified e) {
                    e.printStackTrace();
                    break;
                }

                if (page != null) {
                    if (page.has("results")) {
                        processPage(page.getJSONArray("results"));
                        processedOrdersNumber += page.getJSONArray("results").length();
                    }

                    if (page.isNull("next")) {
                        break;
                    }

                    url = page.getString("next");
                    isFirstPage = false;

                    if (mProgressListener != null) {
                        mProgressListener.onInitialOrderSyncProgress(processedOrdersNumber/page.getInt("count"));
                    }
                } else {
                    break;
                }
            }

        } else { //use previous (default logic)
            super.download();
        }
    }

    private void processPage(final JSONArray ordersArray) {
        if (ordersArray == null || ordersArray.length() == 0) {
            return;
        }

        store.runInTransaction(new Callable<Void>() {
            @Override
            public Void call() {
                long savingStart = System.nanoTime();
                for (int i = 0; i < ordersArray.length(); i++) {
                    Set<String> seen = new HashSet<>();

                    try {
                        JSONObject jsonObj = ordersArray.getJSONObject(i);

                        String jsonId = getId(jsonObj);

                        if (seen.contains(jsonId)) {
                            System.out.println("saving continue: ");
                            continue;
                        }

                        saveOrder(jsonObj);
                        seen.add(jsonId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("saving time: " + (System.nanoTime() - savingStart) / 1000000f + "ms");

                return null;
            }
        });
    }
}
