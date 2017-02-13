package xyz.sayangoswami.urbandictionary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by Sayan on 21/08/16.
 */

class DefinitionsAdapter extends RecyclerView.Adapter<DefinitionsAdapter.MyViewHolder> {

    private List<Definitions> definitionsList;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView definition, author, thumb_up, thumb_down;
        LinearLayout share_view;

        MyViewHolder(View view) {
            super(view);
            definition = (TextView) view.findViewById(R.id.definition);
            author = (TextView) view.findViewById(R.id.author);
            thumb_up = (TextView) view.findViewById(R.id.up);
            thumb_down = (TextView) view.findViewById(R.id.down);
            share_view = (LinearLayout) view.findViewById(R.id.share_def);

        }
    }


    DefinitionsAdapter(List<Definitions> definitionsList) {
        this.definitionsList = definitionsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.definition_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final Definitions definitions = definitionsList.get(position);
        holder.definition.setText(definitions.getDefinition());
        holder.author.setText(definitions.getAuthor());
        holder.thumb_up.setText(Integer.toString(definitions.getThumb_up()));
        holder.thumb_down.setText(Integer.toString(definitions.getThumb_down()));
        holder.itemView.setLongClickable(true);

        holder.thumb_up.setClickable(true);
        holder.thumb_down.setClickable(true);
        holder.share_view.setClickable(true);

        holder.thumb_up.setOnClickListener(new TextView.OnClickListener(){

            @Override
            public void onClick(View view) {
                String url = "http://api.urbandictionary.com/v0/vote?defid=" +
                        Integer.toString(definitions.getId()) + "&direction=up";

                if(definitions.getVote().equals("null") || definitions.getVote().equals("down")){

                    Intent mVote = new Intent(view.getContext() , VoteService.class);
                    mVote.putExtra("url",url);
                    view.getContext().startService(mVote);
                    definitions.setVote("up");

                    Vibrator v = (Vibrator) view.getContext().getSystemService(Context.VIBRATOR_SERVICE);
                    // Vibrate for 30 milliseconds
                    v.vibrate(30);

                    Toast.makeText(view.getContext(), "Upvoted! Changes take effect on reload.",Toast.LENGTH_SHORT).show();

                }
                else {
                    Toast.makeText(view.getContext(), "You've already voted once!",Toast.LENGTH_SHORT).show();
                }

            }
        });

        holder.thumb_down.setOnClickListener(new TextView.OnClickListener(){

            @Override
            public void onClick(View view) {
                String url = "http://api.urbandictionary.com/v0/vote?defid=" +
                        Integer.toString(definitions.getId()) + "&direction=down";

                if(definitions.getVote().equals("null") || definitions.getVote().equals("up")){

                    Intent mVote = new Intent(view.getContext() , VoteService.class);
                    mVote.putExtra("url",url);
                    view.getContext().startService(mVote);
                    definitions.setVote("down");

                    Vibrator v = (Vibrator) view.getContext().getSystemService(Context.VIBRATOR_SERVICE);
                    // Vibrate for 30 milliseconds
                    v.vibrate(30);

                    Toast.makeText(view.getContext(), "Downvoted! Changes take effect on reload.",Toast.LENGTH_SHORT).show();

                }
                else {
                    Toast.makeText(view.getContext(), "You've already voted once!",Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.share_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String def = definitions.getDefinition();
                String word = definitions.getWord();
                String promo = "Sent from Urban Dictionary! Download from https://play.google.com/store/apps/details?id=xyz.sayangoswami.urbandictionary now!";

                Vibrator v = (Vibrator) view.getContext().getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 30 milliseconds
                v.vibrate(30);

                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_TEXT,word + " is " + def + "\n" + promo);

                view.getContext().startActivity(sharingIntent);

            }
        });

    }


    @Override
    public int getItemCount() {
        return definitionsList.size();
    }

    void clearData() {
        int size = this.definitionsList.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                this.definitionsList.remove(0);
            }

            this.notifyItemRangeRemoved(0, size);
        }
    }

}
