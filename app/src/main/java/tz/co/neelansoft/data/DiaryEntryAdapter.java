package tz.co.neelansoft.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import tz.co.neelansoft.diary.R;

/**
 * Created by landre on 27/06/2018.
 */

public class DiaryEntryAdapter extends RecyclerView.Adapter<DiaryEntryAdapter.ViewHolder> {

    private static final String DATE_FORMAT = "dd/MM/yyyy";
    private List<DiaryEntry> mDiaryEntries = new ArrayList<>();
    private final Context mContext;
    private final ItemClickListener mItemClickListener;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
    //public class constructor
    public DiaryEntryAdapter(Context _context, ItemClickListener listener){

        this.mContext = _context;
        this.mItemClickListener = listener;
    }

    @NonNull
    @Override
    public DiaryEntryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_diary_entry, parent, false);

        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull DiaryEntryAdapter.ViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public int getItemCount(){
        return mDiaryEntries.size();
    }

    public void setDiaryEntries(List<DiaryEntry> entries){
        for(DiaryEntry de: entries){
            Log.e("DiaryAdapter ","Entry id: "+de.getId());
        }
        mDiaryEntries = entries;
        notifyDataSetChanged();
    }

    public List<DiaryEntry> getDiaryEntries(){
        return mDiaryEntries;
    }
    public interface ItemClickListener{
        void onItemClickListener(int itemId);

    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        final ImageView ivIcon;
        final TextView tvTitle;
        final TextView tvDate;


        //default constructor
        public ViewHolder(View view){
            super(view);

            ivIcon = view.findViewById(R.id.ivIcon);
            tvTitle = view.findViewById(R.id.tvTitle);
            tvDate  = view.findViewById(R.id.tvDate);

            view.setOnClickListener(this);
        }

        public void bind(int position){
            DiaryEntry entry = mDiaryEntries.get(position);
            String title = entry.getTitle();
            String timeUpdated = dateFormat.format(entry.getTimeUpdated());

            Bitmap moodIcon = entry.getMoodIcon(mContext);

            ivIcon.setImageBitmap(moodIcon);

            ivIcon.setContentDescription("Mood "+mContext.getResources().getStringArray(R.array.moods)[entry.getMood()]);
            tvTitle.setText(title);
            tvDate.setText(timeUpdated);


        }

        @Override
        public void onClick(View v){
            int entryId = mDiaryEntries.get(getAdapterPosition()).getId();
            mItemClickListener.onItemClickListener(entryId);
        }
    }
}
