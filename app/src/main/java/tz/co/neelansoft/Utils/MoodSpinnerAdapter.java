package tz.co.neelansoft.Utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import tz.co.neelansoft.diary.R;

/**
 * Created by landre on 28/06/2018.
 */

public class MoodSpinnerAdapter extends ArrayAdapter<Mood> {
    private Context mContext;
    private List<Mood> mMoods = new ArrayList<>();

    public MoodSpinnerAdapter(@NonNull Context context, List<Mood> moods) {
        super(context, R.layout.spinner_item_layout);

        mContext = context;
        mMoods  = moods;
    }

    @Override
    public int getCount(){
        return mMoods.size();
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent){
        return getCustomView(position,convertView,parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View customView = inflater.inflate(R.layout.spinner_item_layout, parent, false);

        TextView title = (TextView) customView.findViewById(R.id.tvTitle);
        ImageView icon = (ImageView) customView.findViewById(R.id.ivIcon);
        Mood moodItem = mMoods.get(position);
        title.setText(moodItem.getTitle());
        icon.setImageBitmap(moodItem.getMoodIcon(mContext));
        return customView;
    }
}
