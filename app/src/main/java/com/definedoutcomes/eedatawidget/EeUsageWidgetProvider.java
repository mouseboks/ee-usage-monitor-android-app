package com.definedoutcomes.eedatawidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EeUsageWidgetProvider extends AppWidgetProvider {

    private String url = "http://ee-monitor.definedoutcomes.com:5002/eedata";  // This is the API base URL (GitHub API)
    RequestQueue requestQueue;

    public EeUsageWidgetProvider() {
          // This setups up a new request queue which we will need to make HTTP requests.

    }

        @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {

            ComponentName thisWidget = new ComponentName(context,
                    EeUsageWidgetProvider.class);

            for (int widgetId : appWidgetIds) {
                final int widgetIdToUse = widgetId;

                final RemoteViews remoteViews = new RemoteViews(context
                        .getPackageName(), R.layout.ee_usage_appwidget);

                // Get the data from the rest service
                RequestQueue queue = Volley.newRequestQueue(context);


                JsonArrayRequest arrReq = new JsonArrayRequest(Request.Method.GET, url,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    Log.e("Volley", "Received response " + response.toString());
                                    // Check the length of our response
                                    JSONObject jsonObj = response.getJSONObject(0);
                                    String phoneData = jsonObj.get("phone_data_remaining").toString();
                                    String mifiData = jsonObj.get("mifi_data_remaining").toString();
                                    String refreshTime = jsonObj.get("time").toString();
                                    //String lastUpdated = jsonObj.get("updated_at").toString();
                                    //updateEeDataView(phoneData + " of 38", mifiData + " of 64", refreshTime);
                                    remoteViews.setTextViewText(R.id.mifi_data,
                                            mifiData);
                                    remoteViews.setTextViewText(R.id.phone_data,
                                            phoneData);

                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                                    LocalDateTime dateTime = LocalDateTime.parse(refreshTime.substring(0, refreshTime.indexOf('.')), formatter);
                                    remoteViews.setTextViewText(R.id.status_time,
                                            dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

                                    appWidgetManager.updateAppWidget(widgetIdToUse, remoteViews);
                                } catch (JSONException e) {
                                    //// If there is an error then output this to the logs.
                                    Log.e("Volley", "Invalid JSON Object.");
                                    //this.tvRefreshDate.setText(refreshTime);
                                }



                            }
                        },

                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // If there a HTTP error then add a note to our repo list.
                                //updateEeDataView("Error while calling REST API");
                                Log.e("Volley", error.toString());
                            }
                        }
                );

                queue.add(arrReq);

                // Register an onClickListener
                Intent clickIntent = new Intent(context,
                        EeUsageWidgetProvider.class);

                clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                        appWidgetIds);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context, 0, clickIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                remoteViews.setOnClickPendingIntent(R.id.mifi_data, pendingIntent);
                appWidgetManager.updateAppWidget(widgetId, remoteViews);
            }
        }
}