package com.example.iscg7424quiz.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import java.util.HashMap;
import java.util.Map;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iscg7424quiz.R;
import com.example.iscg7424quiz.model.Tournament;

import java.util.List;

public class TournamentAdapter
        extends RecyclerView.Adapter<TournamentAdapter.TournamentViewHolder> {

    private List<Tournament> tournamentList;
    private OnTournamentClickListener listener;
    private Map<String, String> ratingTexts = new HashMap<>();
    public interface OnTournamentClickListener {
        void onEditClick(Tournament tournament);
        void onDeleteClick(Tournament tournament);
    }
    public TournamentAdapter(
            List<Tournament> tournamentList,
            OnTournamentClickListener listener) {
        this.tournamentList = tournamentList;
        this.listener = listener;
    }
    @NonNull
    @Override
    public TournamentViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tournament, parent, false);
        return new TournamentViewHolder(view);
    }
    @Override
    public void onBindViewHolder(
            @NonNull TournamentViewHolder holder, int position) {
        Tournament tournament = tournamentList.get(position);
        holder.tvTournamentName.setText(  tournament.getName()    );
        holder.tvCategory.setText(   "Category: " + tournament.getCategory()      );
        holder.tvDifficulty.setText( "Difficulty: " + tournament.getDifficulty()     );
        holder.tvStartDate.setText(   "Start Date: " + tournament.getStartDate()      );
        holder.tvEndDate.setText(      "End Date: " + tournament.getEndDate()   );
        String ratingText = ratingTexts.get(tournament.getId());
        if (ratingText == null) {    ratingText = "Rating: No ratings yet";      }
        holder.tvRating.setText(ratingText);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onEditClick(tournament);
                }
            }
        });
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onDeleteClick(tournament);
                }
            }
        });
    }
    public void updateRatings(Map<String, String> newRatings) {
        ratingTexts.clear();
        ratingTexts.putAll(newRatings);
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        return tournamentList.size();
    }
    public static class TournamentViewHolder   extends RecyclerView.ViewHolder {
        TextView tvTournamentName, tvCategory, tvDifficulty;
        TextView tvStartDate, tvEndDate, tvRating;
        Button btnDelete;
        public TournamentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTournamentName = itemView.findViewById(R.id.tvTournamentName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDifficulty = itemView.findViewById(R.id.tvDifficulty);
            tvStartDate = itemView.findViewById(R.id.tvStartDate);
            tvEndDate = itemView.findViewById(R.id.tvEndDate);
            tvRating = itemView.findViewById(R.id.tvRating);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}