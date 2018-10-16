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
import java.util.zip.CheckedInputStream;

import io.requery.Column;
import io.requery.Entity;
import io.requery.ForeignKey;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToOne;
import io.requery.Nullable;
import io.requery.ReferentialAction;

@Entity(cacheable = false)
public class AbstractOrderPosition implements RemoteObject {

    @Generated
    @Key
    public Long id;

    public Long server_id;

    @Column(name="order_ref")
    @ForeignKey(update = ReferentialAction.CASCADE)
    @ManyToOne
    public Order order;

    public Long positionid;

    @Nullable
    public String attendee_name;

    @Nullable
    public String attendee_email;

    @ForeignKey(update = ReferentialAction.SET_NULL)
    @ManyToOne
    public Item item;

    public String secret;

    public String json_data;

    public BigDecimal getPrice() {
        try {
            return new BigDecimal(getJSON().getString("price"));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public BigDecimal getTaxRate() {
        try {
            return new BigDecimal(getJSON().getString("tax_rate"));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public BigDecimal getTaxValue() {
        try {
            return new BigDecimal(getJSON().getString("tax_value"));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Long getTaxRule() {
        try {
            return getJSON().optLong("tax_rule", 0L);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Long getSubeventId() {
        try {
            return getJSON().optLong("subevent", 0L);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Long getVariationId() {
        try {
            return getJSON().optLong("variation", 0L);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public JSONObject getJSON() throws JSONException {
        return new JSONObject(json_data);
    }

    public void fromJSON(JSONObject data) throws JSONException {
        server_id = data.getLong("server_id");
        positionid = data.getLong("position");
        attendee_name = data.getString("attendee_name");
        attendee_email = data.getString("attendee_email");
        secret = data.getString("secret");
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

    public Date getCheckInDate() {
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
