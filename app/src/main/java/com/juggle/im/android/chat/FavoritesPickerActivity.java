package com.juggle.im.android.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.juggle.im.android.R;
import com.juggle.im.android.model.FavoriteItem;
import com.juggle.im.android.model.FavoritesRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoritesPickerActivity extends AppCompatActivity {

    public static final String EXTRA_SELECTED = "extra_selected";

    private RecyclerView rvFavorites;
    private TextView tvEmpty;
    private Button btnSend;
    private FavoritesAdapter adapter;
    private final Set<String> selectedIds = new HashSet<>();
    private List<FavoriteItem> allItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites_picker);

        ImageView btnBack = findViewById(R.id.btn_back);
        rvFavorites = findViewById(R.id.rv_favorites);
        tvEmpty = findViewById(R.id.tv_empty);
        btnSend = findViewById(R.id.btn_send);

        btnBack.setOnClickListener(v -> finish());

        // 子线程读 SP，避免主线程 DiskReadViolation
        new Thread(() -> {
            FavoritesRepository repo = new FavoritesRepository(FavoritesPickerActivity.this);
            final java.util.List<FavoriteItem> list = repo.getAll();
            runOnUiThread(() -> {
                allItems = list != null ? list : new ArrayList<>();
                if (allItems.isEmpty()) {
                    rvFavorites.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    btnSend.setEnabled(false);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    btnSend.setEnabled(false);
                    rvFavorites.setLayoutManager(new LinearLayoutManager(FavoritesPickerActivity.this));
                    adapter = new FavoritesAdapter(allItems, selectedIds, (item, selected) -> {
                        if (selected) selectedIds.add(item.getId());
                        else selectedIds.remove(item.getId());
                        btnSend.setEnabled(!selectedIds.isEmpty());
                    });
                    rvFavorites.setAdapter(adapter);
                }
            });
        }).start();

        btnSend.setOnClickListener(v -> {
            if (selectedIds.isEmpty()) {
                Toast.makeText(this, R.string.favorites_empty, Toast.LENGTH_SHORT).show();
                return;
            }
            ArrayList<FavoriteItem> selected = new ArrayList<>();
            for (FavoriteItem item : allItems) {
                if (selectedIds.contains(item.getId())) selected.add(item);
            }
            Intent data = new Intent();
            data.putExtra(EXTRA_SELECTED, selected);
            setResult(RESULT_OK, data);
            finish();
        });
    }
}
