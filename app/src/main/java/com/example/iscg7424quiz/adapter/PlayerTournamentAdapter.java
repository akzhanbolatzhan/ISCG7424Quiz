package com.example.iscg7424quiz.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iscg7424quiz.R;
import com.example.iscg7424quiz.model.Tournament;

import java.util.ArrayList;
import java.util.List;

public class PlayerTournamentAdapter extends RecyclerView.Adapter<PlayerTournamentAdapter.ViewHolder> {
    private List<Tournament> tournamentList;
    private String currentStatus = "Ongoing";
    private onItemClickListener listener;
    public interface onItemClickListener {
        void onItemClick(Tournament tournament);
    }
    public PlayerTournamentAdapter(List<Tournament> tournamentList, onItemClickListener listener) {
        this.tournamentList = tournamentList;
        this.listener = listener;
    }
    public void updateData(List<Tournament> newList, String status) {
        tournamentList.clear();
        tournamentList.addAll(newList);
        currentStatus = status;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_player_tournament, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tournament tournament = tournamentList.get(position);
        holder.tvTournamentName.setText(tournament.getName());
        holder.tvCategory.setText("Category: " + tournament.getCategory());
        holder.tvDifficulty.setText("Difficulty: " + tournament.getDifficulty());
        holder.tvStartDate.setText("Start Date: " + formatDate(tournament.getStartDate()));
        holder.tvEndDate.setText("End Date: " + formatDate(tournament.getEndDate()));
        if (currentStatus.equals("Ongoing")) {
            holder.btnJoin.setVisibility(View.VISIBLE);
            holder.btnJoin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onItemClick(tournament);
                    }
                }
            });
        } else {
            holder.btnJoin.setVisibility(View.GONE);
            holder.btnJoin.setOnClickListener(null);
        }
    }
    private String formatDate(String date) {
        if (date == null) {
            return "";
        }
        if (date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return date.substring(8, 10) + "/" + date.substring(5, 7) + "/" + date.substring(0, 4);
        }
        return date;
    }
    @Override
    public int getItemCount() {
        return tournamentList.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTournamentName, tvCategory, tvDifficulty, tvStartDate, tvEndDate;
        Button btnJoin;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTournamentName = itemView.findViewById(R.id.tvTournamentName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDifficulty = itemView.findViewById(R.id.tvDifficulty);
            tvStartDate = itemView.findViewById(R.id.tvStartDate);
            tvEndDate = itemView.findViewById(R.id.tvEndDate);
            btnJoin = itemView.findViewById(R.id.btnJoin);
        }
    }
}