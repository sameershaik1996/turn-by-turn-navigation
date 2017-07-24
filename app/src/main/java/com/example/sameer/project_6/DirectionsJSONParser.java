package com.example.sameer.project_6;

/**
 * Created by sameer on 5/28/2017.
 */


        import android.content.Intent;
        import android.util.Log;

        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Map;

        import org.json.JSONArray;
        import org.json.JSONException;
        import org.json.JSONObject;

        import com.google.android.gms.maps.model.LatLng;

public class DirectionsJSONParser {
    public static int m=0,a=0;
    public static String[] offsets=new String[1000];
    public static String[] jHtml= new String[1000];
    /** Receives a JSONObject and returns a list of lists containing latitude and longitude */
    public List<List<HashMap<String,String>>> parse(JSONObject jObject){

        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String,String>>>() ;
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;
        String jDistance=null;


        try {

            jRoutes = jObject.getJSONArray("routes");

            /** Traversing all routes */
            for(int i=0;i<jRoutes.length();i++){
                jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");
                Log.e("leg",""+jLegs);


                List path = new ArrayList<HashMap<String, String>>();

                /** Traversing all legs */
                for(int j=0;j<jLegs.length();j++){
                    jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");

                    Log.e("step",""+jSteps);
                    //Log.e("legs",""+( (JSONObject)jLegs.get(j)).getJSONArray("html_instructions"));
                    /** Traversing all steps */
                    for(int k=0;k<jSteps.length();k++){
                        jHtml[a] =(String)(((JSONObject)jSteps.get(k)).get("html_instructions"));
                        jHtml[a]=jHtml[a].replaceAll("<(.*?)*>", "");
                        int iend = jHtml[a].indexOf("(");
                        if (iend != -1)
                            jHtml[a]= jHtml[a].substring(0 , iend);
                        a++;
                        jDistance=(String)((JSONObject)((JSONObject)jSteps.get(k)).get("distance")).get("text");
                        Log.e("dist",jDistance);

                       //jHtml=jHtml.replaceAll("((.*?)*)", "");
                        Log.e("html",""+jHtml[k]);
                        String polyline = "";
                        polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");
                        List<LatLng> list = decodePoly(polyline);

                        try {

                            if((((JSONObject) jSteps.get(k))).has("maneuver")) {
                                offsets[k] = "" + (((JSONObject) jSteps.get(k))).get("maneuver");
                                m++;
                                Log.e("value",k+"");
                                Log.e("man",offsets[m]);
                                if((((JSONObject) jSteps.get(k))).get("start_location")!=null&&(((JSONObject) jSteps.get(k))).get("end_location")!=null) {
                                    String start_lat =  ((JSONObject)(((JSONObject) jSteps.get(k))).get("start_location")).getString("lat");
                                    String start_lon = ((JSONObject)(((JSONObject) jSteps.get(k))).get("start_location")).getString("lng");

                                    String end_lat = ((JSONObject)(((JSONObject) jSteps.get(k))).get("end_location")).getString("lat");
                                    String end_lon = ((JSONObject)(((JSONObject) jSteps.get(k))).get("start_location")).getString("lat");
                                    Log.e("start",start_lat+" "+start_lon);
                                    Log.e("end",end_lat+" "+end_lon);
                                }

                            }
                        }catch (Exception e){}

                        // Traversing all points */
                        for(int l=0;l<list.size();l++){
                            HashMap<String, String> hm = new HashMap<String, String>();
                            hm.put("lat", Double.toString(((LatLng)list.get(l)).latitude) );
                            hm.put("lng", Double.toString(((LatLng)list.get(l)).longitude) );
                            path.add(hm);
                            //Log.e("Tag",""+Double.toString(((LatLng)list.get(l)).latitude)+"   "+Double.toString(((LatLng)list.get(l)).longitude));
                        }
                    }
                    routes.add(path);

                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e){
        }


        return routes;
    }


    /**
     * Method to decode polyline points
     * Courtesy : jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     * */
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

}