package com.arora.developer.task1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ArrayList<Uri> urls;
    ArrayList<Bitmap> bitmaps;
    Context context;
    Bitmap bm;
    ProgressBar progressBar;


    public Adapter(ArrayList<Uri> urls, Context context, ArrayList<Bitmap> imagesEncodedList, ProgressBar progressBar) {
        this.urls = urls;
        this.bitmaps = imagesEncodedList;
        this.context = context;
        this.progressBar = progressBar;
    }

    @NonNull
    @Override

    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View menuItemLayoutView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.image_cell, parent, false);
        return new ViewHolder(menuItemLayoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final ViewHolder holderr = (ViewHolder) holder;

        Picasso.get().load(urls.get(position)).error(R.drawable.bg).into(new com.squareup.picasso.Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                progressBar.setVisibility(View.GONE);
                bitmaps.add(bitmap);

                holderr.image.setImageBitmap(bitmap);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                System.out.println(e);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                progressBar.setVisibility(View.VISIBLE);
            }
        });

        holderr.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bitmaps.remove(position);
                urls.remove(position);
                notifyDataSetChanged();

            }
        });
    }

    @Override
    public int getItemCount() {
        return urls.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image, delete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageView);
            delete = itemView.findViewById(R.id.delete);
        }

    }
}
