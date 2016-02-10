package xz.watchface01;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;
import java.util.jar.JarException;

/**
 * Created by xz278 on 1/25/16.
 * ListView that contains a list of randomly generated suggestions.
 */
public class ListViewActivity extends ListActivity{

    String[] data = new String[0];

    public final static String SUGGESTION_DETAIL = "watchface.SUGGESTION_DETAIL";
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_list_view_activity);
        //ListView listView = (ListView) findViewById(R.id.listview);


        try {
            JSONArray jsonArray = new JSONArray(initializeData());
            int l = jsonArray.length();
            data = new String[l];
            for (int i=0;i<l;i++){
                data[i] = (String)jsonArray.get(i);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        setListAdapter(new SuggestionArrayAdapter(this, data));
    }

    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id){
        //String selectedData = (String)getListAdapter().getItem(position);
        String selectedData = data[position];
        Intent intent = new Intent(this, SuggestionDetailActivity.class);
        intent.putExtra(SUGGESTION_DETAIL, selectedData);
        startActivity(intent);
    }

    /* custom ArrayAdapter*/
    private class SuggestionArrayAdapter extends ArrayAdapter<String>{

        private final Context mContext;
        private final String[] data;

        public SuggestionArrayAdapter(Context context, String[] suggestions){
            super(context,-1,suggestions);
            mContext = context;
            data = suggestions;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.list_item, parent, false);
            TextView title = (TextView)rowView.findViewById(R.id.list_item_title);
            TextView goal = (TextView)rowView.findViewById(R.id.list_item_preview);
            try{
                JSONObject object = new JSONObject(data[position]);
                title.setText(object.getString("type"));
                goal.setText(object.getString("suggestion"));
            }catch (JSONException e){
                e.printStackTrace();
                title.setText("Title");
                goal.setText("goal");
            }
            return rowView;
        }
    }


    /* generate suggestions, return the string form of JSONArray*/
    private String initializeData(){
        JSONObject object1 = new JSONObject();
        JSONObject object2 = new JSONObject();
        JSONObject object3 = new JSONObject();
        JSONArray listData = new JSONArray();
        try{
            object1.put("type","Walk");
            object1.put("calorie",100);
            object1.put("location", "Gates Hall");
            object1.put("suggestion","Walk 4 min");
            UUID uuid1 = UUID.randomUUID();
            object1.put("id",uuid1.toString());
            object2.put("type","Workout");
            object2.put("calorie",500);
            object2.put("location","Gym");
            object2.put("suggestion","Workout 20 min");
            UUID uuid2 = UUID.randomUUID();
            object2.put("id",uuid2.toString());
            object3.put("type","Walk");
            object3.put("calorie",100);
            object3.put("location","Goldwin Smith Hall");
            object3.put("suggestion","Walk 3 min");
            UUID uuid3 = UUID.randomUUID();
            object3.put("id",uuid3.toString());
            listData.put(object1.toString());
            listData.put(object2.toString());
            listData.put(object3.toString());
        }catch (JSONException e){
            e.printStackTrace();
        }
        return listData.toString();
    }


}
