package Module;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class TimHuongDi {
    private static final String DIRECTION_URL_API="https://maps.googleapis.com/maps/api/directions/json?";
    private static final String GOOGLE_API_KEY="AIzaSyA8-X95U-yVJ8ST6y4dQo2pYEXKBPNtxQQ";
    //private TimHuongDiListener;
    private TimHuongDiListener listener;
    private String diemkhoihanh;
    private String diemketthuc;

    public TimHuongDi(TimHuongDiListener listener, String diemkhoihanh, String diemketthuc) {
        this.listener = listener;
        this.diemkhoihanh = diemkhoihanh;
        this.diemketthuc = diemketthuc;
    }

    public void thucthu() throws UnsupportedEncodingException{
        listener.onDirectionFinderStart();
        new DownloadRawData().execute(createUrl());
    }

    private String createUrl() throws UnsupportedEncodingException {
        String urlKhoiHanh=URLEncoder.encode(diemkhoihanh, "UTF-8");
        String urlKetThuc=URLEncoder.encode(diemketthuc,"UTF-8");

        return DIRECTION_URL_API+"origin="+urlKhoiHanh+"&destination="+urlKetThuc+"&key="+GOOGLE_API_KEY;
    }

    private class DownloadRawData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... param) {
            String link=param[0];
            try{
                URL url=new URL(link);
                InputStream is= url.openConnection().getInputStream();
                StringBuffer buffer=new StringBuffer();
                BufferedReader reader= new BufferedReader(new InputStreamReader(is));

                String line;
                while((line=reader.readLine())!=null){
                    buffer.append(line+"\n");
                }
                return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String res) {
            try{
                parseJSon(res);

            }catch (JSONException e){
                    e.printStackTrace();
            }
        }
    }

    private void parseJSon(String data) throws JSONException {
        if(data==null){
            return;
        }

        List<HuongDi> routes = new ArrayList<HuongDi>();
        JSONObject jsonData = new JSONObject(data);
        JSONArray jsonRoutes = jsonData.getJSONArray("routes");
        for (int i = 0; i < jsonRoutes.length(); i++) {
            JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
            HuongDi route = new HuongDi();

            JSONObject overview_polylineJson = jsonRoute.getJSONObject("overview_polyline");
            JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
            JSONObject jsonLeg = jsonLegs.getJSONObject(0);
            JSONObject jsonDistance = jsonLeg.getJSONObject("distance");
            JSONObject jsonDuration = jsonLeg.getJSONObject("duration");
            JSONObject jsonEndLocation = jsonLeg.getJSONObject("end_location");
            JSONObject jsonStartLocation = jsonLeg.getJSONObject("start_location");

            route.quangDuong = new QuangDuong(jsonDistance.getString("text"), jsonDistance.getInt("value"));
            route.thoiGian = new ThoiGian(jsonDuration.getString("text"), jsonDuration.getInt("value"));
            route.diachiKetThuc = jsonLeg.getString("end_address");
            route.diachiKhoiHanh = jsonLeg.getString("start_address");
            route.toadoKhoiHanh = new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng"));
            route.toadoKetThuc = new LatLng(jsonEndLocation.getDouble("lat"), jsonEndLocation.getDouble("lng"));
            route.Diem = decodePolyLine(overview_polylineJson.getString("points"));

            routes.add(route);
        }

        listener.onDirectionFinderSuccess(routes);
    }

    private List<LatLng> decodePolyLine(final  String poly) {
        int len = poly.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<LatLng>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng(
                    lat / 100000d, lng / 100000d
            ));
        }

        return decoded;

    }
}
