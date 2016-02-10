package xz.watchface01;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by xz278 on 1/27/16.
 * An instance is a set of suggestions.
 */
public class Suggestion {
    private ArrayList<UUID> ids;
    private ArrayList<Integer> calories;
    private ArrayList<String> locations;
    private ArrayList<String> types;
    private ArrayList<String> suggestions;

    public Suggestion(){
        ids = new ArrayList<UUID>();
        calories = new ArrayList<Integer>();
        locations = new ArrayList<String>();
        types = new ArrayList<String>();
        suggestions = new ArrayList<String>();
    }

    public Suggestion(String jsonForm){
        ids = new ArrayList<UUID>();
        calories = new ArrayList<Integer>();
        locations = new ArrayList<String>();
        types = new ArrayList<String>();
        suggestions = new ArrayList<String>();
        try{
            JSONArray jsonArray = new JSONArray(jsonForm);
            for (int i=0;i<jsonArray.length();i++){
                String str = jsonArray.getString(i);
                add(str);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    public int getCount(){
        return ids.size();
    }

    public boolean contains(UUID uuid){
        return ids.contains(uuid);
    }

    public void add(String jsonForm){
        try{
            JSONObject jsonObject = new JSONObject(jsonForm);
            UUID uuid = UUID.fromString(jsonObject.getString("id"));
            if (ids.contains(uuid)) return;
            ids.add(uuid);
            calories.add(jsonObject.getInt("calorie"));
            locations.add(jsonObject.getString("location"));
            types.add(jsonObject.getString("type"));
            suggestions.add(jsonObject.getString("suggestion"));
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    @Override
    public String toString(){
        JSONArray jsonArray = new JSONArray();
        int l = ids.size();
        for (int i=0;i<l;i++){
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", ids.get(i).toString());
                jsonObject.put("type",types.get(i));
                jsonObject.put("location",locations.get(i));
                jsonObject.put("calorie",calories.get(i));
                jsonObject.put("suggestion",suggestions.get(i));
                jsonArray.put(jsonObject);
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return jsonArray.toString();
    }

    public void remove(String jsonForm){
        try{
            JSONObject jsonObject = new JSONObject(jsonForm);
            UUID uuid = UUID.fromString(jsonObject.getString("id"));
            int i = ids.indexOf(uuid);
            if (i==-1) return;
            ids.remove(i);
            locations.remove(i);
            calories.remove(i);
            suggestions.remove(i);
            types.remove(i);
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    public void remove(int index){
        ids.remove(index);
        locations.remove(index);
        calories.remove(index);
        suggestions.remove(index);
        types.remove(index);
    }

    public UUID getID(int index){
        return ids.get(index);
    }

    public int getCalorie(int index){
        return calories.get(index);
    }

    public String getLocation(int index){
        return locations.get(index);
    }

    public String getType(int index){
        return types.get(index);
    }

    public String getSuggestion(int index){
        return suggestions.get(index);
    }

    public int getWalkCount(){
        int c = 0;
        for (int i=0;i<types.size();i++){
            if (types.get(i).equals("Walk")) c++;
        }
        return c;
    }

    public int getWorkoutCount(){
        int c = 0;
        for (int i=0;i<types.size();i++){
            if (types.get(i).equals("Workout")) c++;
        }
        return c;
    }

    public void formWalkSuggestion(){
        int i = 0;
        int l = types.size();
        while (i<l){
            if ((types.get(i)).equals("Walk")) {
                i ++;
                continue;
            } else {
                l --;
                remove(i);
            }
        }
    }

    public void formWorkoutSuggestion(){
        int i = 0;
        int l = types.size();
        while (i<l){
            if ((types.get(i)).equals("Workout")) {
                i ++;
                continue;
            } else {
                l --;
                remove(i);
            }
        }
    }

    public int[] getCalories(){
        int size = calories.size();
        int[] ans = new int[size];
        for (int i=0;i<size;i++){
            ans[i] = calories.get(i);
        }
        return ans;
    }

    public String[] getLocations(){
        int size = locations.size();
        String[] ans = new String[size];
        for (int i=0;i<size;i++){
            ans[i] = locations.get(i);
        }
        return ans;
    }

    public String[] getSuggestions(){
        int size = suggestions.size();
        String[] ans = new String[size];
        for (int i=0;i<size;i++){
            ans[i] = suggestions.get(i);
        }
        return ans;
    }
    public String[] getTypes(){
        int size = types.size();
        String[] ans = new String[size];
        for (int i=0;i<size;i++){
            ans[i] = types.get(i);
        }
        return ans;
    }
}
