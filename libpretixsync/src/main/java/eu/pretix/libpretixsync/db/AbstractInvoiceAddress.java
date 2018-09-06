package eu.pretix.libpretixsync.db;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import io.requery.Column;
import io.requery.Entity;
import io.requery.ForeignKey;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToOne;
import io.requery.Nullable;
import io.requery.OneToOne;
import io.requery.ReferentialAction;

@Entity(cacheable = false)
public class AbstractInvoiceAddress implements RemoteObject {

    @Generated
    @Key
    public Long id;

    @Column(name="order_ref")
    @ForeignKey(update = ReferentialAction.CASCADE)
    @OneToOne
    public Order order;

    @Nullable
    public Date last_modified;

    @Nullable
    public String company;

    /**
     * From api doc: Business or individual customers (always False for orders created before pretix 1.7, do not rely on it).
     */
    public boolean is_business;

    public String name;

    public String street;

    public String zipcode;

    public String city;

    public String country;

    @Nullable
    public String internal_reference;

    @Nullable
    public String vat_id;

    public String json_data;

    @Override
    public JSONObject getJSON() throws JSONException {
        return new JSONObject(json_data);
    }

    public void fromJSON(JSONObject data) throws JSONException {
        last_modified = getModifiedDate();
        company = data.optString("company");
        is_business = data.optBoolean("is_business");
        name = data.getString("name");
        street = data.getString("street");
        zipcode = data.getString("zipcode");
        city = data.getString("city");
        country = data.getString("country");
        internal_reference = data.optString("internal_reference");
        vat_id = data.optString("vat_id");
        json_data = data.toString();
    }

    public boolean isCheckedIn() {
        try {
            JSONArray checkinArray = getJSON().optJSONArray("checkins");
            return (checkinArray != null && checkinArray.length() > 0);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Date getModifiedDate() {
        try {
            JSONArray checkinArray = getJSON().optJSONArray("checkins");
            if (checkinArray != null && checkinArray.length() > 0) {
                JSONObject firstCheckIn = checkinArray.getJSONObject(0);
                String dateString = firstCheckIn.getString("datetime");

                TimeZone tz = TimeZone.getTimeZone("UTC");
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
                df.setTimeZone(tz);

                return df.parse(dateString);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
}
