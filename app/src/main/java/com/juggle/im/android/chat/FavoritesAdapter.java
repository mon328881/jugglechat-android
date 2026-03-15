package com.juggle.im.android.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.juggle.im.android.R;
import com.juggle.im.android.model.FavoriteItem;

import java.util.List;
import java.util.Set;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.VH> {

    private final List<FavoriteItem> items;
    private final Set<String> selectedIds;
    private final OnSelectListener listener;

    public interface OnSelectListener {
        void onSelectChanged(FavoriteItem item, boolean selected);
    }

    public FavoritesAdapter(List<FavoriteItem> items, Set<String> selectedIds, OnSelectListener listener) {
        this.items = items;
        this.selectedIds = selectedIds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        FavoriteItem item = items.get(position);
        holder.tvContent.setText(item.getContent());
        holder.ivThumb.setVisibility(View.GONE);
        holder.ivIcon.setVisibility(View.GONE);

        if (FavoriteItem.TYPE_IMAGE.equals(item.getType())) {
            holder.ivThumb.setVisibility(View.VISIBLE);
            String path = item.getLocalPath();
            // 不在此处调用 File.exists()，避免主线程 DiskReadViolation；由 Glide 异步加载，失败时显示 placeholder
            Glide.with(holder.itemView)
                    .load(path != null ? path : "")
                    .placeholder(R.drawable.ic_default_img)
                    .error(R.drawable.ic_default_img)
                    .centerCrop()
                    .transform(new RoundedCorners(8))
                    .into(holder.ivThumb);
            holder.tvType.setText(holder.itemView.getContext().getString(R.string.msg_image));
        } else if (FavoriteItem.TYPE_FILE.equals(item.getType())) {
            holder.ivIcon.setVisibility(View.VISIBLE);
            holder.ivIcon.setImageResource(R.drawable.ic_file);
            holder.tvType.setText(holder.itemView.getContext().getString(R.string.msg_file));
        } else {
            holder.ivIcon.setVisibility(View.VISIBLE);
            holder.ivIcon.setImageResource(R.drawable.ic_menu);
            holder.tvType.setText(holder.itemView.getContext().getString(R.string.msg_text));
        }

        boolean selected = selectedIds.contains(item.getId());
        holder.ivCheck.setVisibility(View.VISIBLE);
        holder.ivCheck.setImageResource(selected ? R.drawable.ic_check_green : R.drawable.ic_checkbox_unselect);

        holder.itemView.setOnClickListener(v -> {
            boolean nowSelected = !selectedIds.contains(item.getId());
            if (nowSelected) selectedIds.add(item.getId());
            else selectedIds.remove(item.getId());
            holder.ivCheck.setImageResource(nowSelected ? R.drawable.ic_check_green : R.drawable.ic_checkbox_unselect);
            if (listener != null) listener.onSelectChanged(item, nowSelected);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivThumb, ivIcon, ivCheck;
        TextView tvContent, tvType;

        VH(View v) {
            super(v);
            ivThumb = v.findViewById(R.id.iv_thumb);
            ivIcon = v.findViewById(R.id.iv_icon);
            ivCheck = v.findViewById(R.id.iv_check);
            tvContent = v.findViewById(R.id.tv_content);
            tvType = v.findViewById(R.id.tv_type);
        }
    }
}
