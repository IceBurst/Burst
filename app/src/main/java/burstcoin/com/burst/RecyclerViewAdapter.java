package burstcoin.com.burst;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.mViewHolder> {


    ArrayList<DataModel> dataModelArrayList;
    Context context;
    Dialog mainDialog;
    DatabaseHandler databaseHandler;
    SQLiteDatabase sqLiteDatabase;
    Activity mActivity;

    public RecyclerViewAdapter(Activity activity, ArrayList<DataModel> dataModelArrayList, Dialog mainDialog, DatabaseHandler databaseHandler) {
        this.mActivity = activity;
        this.dataModelArrayList = dataModelArrayList;
        this.mainDialog = mainDialog;
        this.databaseHandler = databaseHandler;
        sqLiteDatabase = databaseHandler.getWritableDatabase();
    }


    @Override
    public mViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_single, parent, false);
        mViewHolder viewHolder = new mViewHolder(mView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final mViewHolder holder, final int position) {
        holder.textView.setText(dataModelArrayList.get(position).getName());
        holder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.dialog_enter_pin);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                int[] colorList = new int[]{
                        context.getResources().getColor(R.color.colorAccentPressed),
                        context.getResources().getColor(R.color.colorAccent)
                };

                int[][] states = new int[][]{
                        new int[]{android.R.attr.state_pressed}, // enabled
                        new int[]{}  // pressed
                };
                ColorStateList csl = new ColorStateList(states, colorList);
                final EditText pin = (EditText) dialog.findViewById(R.id.edt_pin);
                AppCompatButton loadPin = (AppCompatButton) dialog.findViewById(R.id.load_phrase);
                loadPin.setSupportBackgroundTintList(csl);
                loadPin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (pin.getText().toString().equalsIgnoreCase(dataModelArrayList.get(position).getPin())) {
                            ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clipData = ClipData.newPlainText("passphrase", dataModelArrayList.get(position).getPhrase());
                            clipboardManager.setPrimaryClip(clipData);
                            Toast.makeText(context, "Your passphrase copied to clipboard.", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            mainDialog.dismiss();
                        } else {
                            Toast.makeText(context, "Please enter valid pin.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                dialog.show();

            }
        });

        holder.btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setTitle("Alert");
                builder.setMessage("Are you sure to delete your passphrase for "+dataModelArrayList.get(position).getName()+"? ");
                builder.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String id = dataModelArrayList.get(position).getId();
                                String where = "id="+id;
                                databaseHandler.deleteRecord(sqLiteDatabase, where);
                                dataModelArrayList.remove(position);
                                notifyDataSetChanged();
                            }
                        });
                builder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.setCancelable(false);
                alert.show();

            }
        });
    }

    @Override
    public int getItemCount() {
        return dataModelArrayList.size();
    }

    protected class mViewHolder extends RecyclerView.ViewHolder {

        private TextView textView;
        private ImageView btn_delete;

        public mViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.phrasename);
            btn_delete = (ImageView)itemView.findViewById(R.id.btn_delete);
        }
    }

}
