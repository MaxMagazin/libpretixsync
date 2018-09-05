package eu.pretix.libpretixsync.db;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.requery.Entity;
import io.requery.Generated;
import io.requery.JunctionTable;
import io.requery.Key;
import io.requery.ManyToMany;
import io.requery.OneToMany;
import io.requery.OneToOne;
import io.requery.Table;

@Table(name = "orders")
@Entity(cacheable = false)
public class AbstractOrder implements RemoteObject {

    @Generated
    @Key
    public Long id;

    public String event_slug;

    public String code;

    public String status;

    public String email;

    public boolean checkin_attention;

//    @OneToOne
//    public InvoiceAddress invoice_address; // TODO: 05.09.18 Maxim: finish implementation
    public String companyName;

    public String json_data;

    @OneToMany
    public List<OrderPosition> positions;

    public String getPaymentProvider() {
        try {
            return getJSON().getString("payment_provider");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public JSONObject getJSON() throws JSONException {
        return new JSONObject(json_data);
    }

    @Deprecated
    public String getCompanyNameFromJson() {
        if (companyName != null && !companyName.isEmpty()) {
            return companyName;
        }

        try {
            JSONObject invoiceAddressObj = getJSON().optJSONObject("invoice_address");
            if (invoiceAddressObj != null) {
                companyName = invoiceAddressObj.optString("company");
            }
            return companyName;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
