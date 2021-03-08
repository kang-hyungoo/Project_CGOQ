package com.example.project_cgoq;


import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FindActivity extends AppCompatActivity {
    int totalSelected = 0;
    String node_name;
    String song_name;
    EditText editView;
    ListView listView;
    TextView textView;
    MyListAdapterMy listadapter;
    ArrayList<MyListItem> listAllItems=new ArrayList<MyListItem>();
    ArrayList<MyListItem> listDispItems=new ArrayList<MyListItem>();
    private String root= Environment.getExternalStorageDirectory().getAbsolutePath();
    private String CurPath=Environment.getExternalStorageDirectory().getAbsolutePath();
    private ArrayList<String> itemFiles = new ArrayList<String>();
    private ArrayList<String> pathFiles = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find);
        textView=(TextView)findViewById(R.id.textView_debug);
        setupList();
        setupAdapter();
        setupFilter();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        setupAdapter();
    }

    private void setupList() {
        listView=(ListView)findViewById(R.id.list1);
    }

    public class MyListItem {
        int selectedNumber;
        boolean checked;
        // for display
        String name;
        // for using ; path+name
        String path;
    }

    public class MyListAdapterMy extends MyArrayAdapter<MyListItem> {

        public MyListAdapterMy(Context context) {
            super(context,R.layout.list_item);
            totalSelected = 0;
            setSource(listDispItems);
        }
        @Override
        public void bindView(View view, MyListItem item) {
            TextView name = (TextView)view.findViewById(R.id.name_saved);
            name.setText(item.name);
            CheckBox cb = (CheckBox)view.findViewById(R.id.checkBox_saved);
            cb.setChecked(item.checked);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View retView = super.getView(position,convertView,parent);
            final int pos = position;
            final View parView = retView;
            CheckBox cb = (CheckBox)retView.findViewById(R.id.checkBox_saved);
            cb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MyListItem item = listDispItems.get(pos);
                    item.checked = !item.checked;
                    if( item.checked ) totalSelected++;
                    item.selectedNumber=totalSelected;
                    Toast.makeText(FindActivity.this,"1: Click "+pos+ "th " + item.checked + " "+totalSelected,Toast.LENGTH_SHORT).show();
                    printDebug();
                }
            });
            TextView name = (TextView)retView.findViewById(R.id.name_saved);
            name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MyListItem item = listDispItems.get(pos);
                    //item.checked = !item.checked;
                    //if( item.checked ) totalSelected++;
                    //item.selectedNumber=totalSelected;
                    //Toast.makeText(MainActivity.this,"2: Click "+pos+ "th " + item.checked + " "+totalSelected,Toast.LENGTH_SHORT).show();
                    //printDebug();
                    //bindView(parView, item);
                    itemClick(item.name,item.path);
                }
            });

            return retView;
        }
        public void fillter(String searchText) {
            listDispItems.clear();
            totalSelected = 0;
            for(int i = 0;i<listAllItems.size();i++){
                MyListItem item = listAllItems.get(i);
                item.checked = false;
                item.selectedNumber = 0;
            }
            if(searchText.length() == 0)
            {
                listDispItems.addAll(listAllItems);
            }
            else
            {
                for( MyListItem item : listAllItems)
                {
                    if(item.name.contains(searchText))
                    {
                        listDispItems.add(item);
                    }
                }
            }
            notifyDataSetChanged();
        }
    }


    public class AdapterAsyncTask extends AsyncTask<String,Void,String> {
        private ProgressDialog mDlg;
        Context mContext;

        public AdapterAsyncTask(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDlg = new ProgressDialog(mContext);
            mDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mDlg.setMessage( "loading" );
            mDlg.show();
        }
        @Override
        protected String doInBackground(String... strings) {
            // listAllItems MyListItem
            listAllItems.clear();
            listDispItems.clear();

            getDirInfo(CurPath);

            for(int i=0;i<itemFiles.size();i++){
                MyListItem item = new MyListItem();
                item.checked = false;
                item.name = itemFiles.get(i);
                item.path = pathFiles.get(i);
                File file = new File(item.path + item.name);
                if(item.name.endsWith("/"))
                {
                    listAllItems.add(item);
                }
                else
                {
                    if(file.getName().endsWith(".node"))
                    {
                        listAllItems.add(item);
                    }
                }
            }

            if (listAllItems != null) {
                Collections.sort(listAllItems, nameComparator);
            }
            listDispItems.addAll(listAllItems);
            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mDlg.dismiss();
            listadapter=new MyListAdapterMy(mContext);

            listView.setAdapter(listadapter);

            String searchText = editView.getText().toString();
            if( listadapter!=null ) listadapter.fillter(searchText);

            textView.setText("Location: " + CurPath);
        }
        private final Comparator<MyListItem> nameComparator
                = new Comparator<MyListItem>() {
            public final int
            compare(MyListItem a, MyListItem b) {
                return collator.compare(a.name, b.name);
            }
            private final Collator collator = Collator.getInstance();
        };
    }
    private void setupAdapter() {
        AdapterAsyncTask adaterAsyncTask = new AdapterAsyncTask(FindActivity.this);
        adaterAsyncTask.execute();
    }
    private void setupFilter() {
        editView=(EditText)findViewById(R.id.savedfilter);
        editView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                String searchText = editView.getText().toString();
                if( listadapter!=null ) listadapter.fillter(searchText);
            }
        });
    }

    private int getSelectedItemCount() {
        int checkcnt = 0;
        for(int i=0;i<listDispItems.size();i++){
            MyListItem item = listDispItems.get(i);
            if( item.checked ) checkcnt++;
        }
        return checkcnt;
    }

    private List<String> getSelectedItems() {
        List<String> ret = new ArrayList<String>();
        int count = 0;
        for(int i=0;i<listDispItems.size();i++){
            MyListItem item = listDispItems.get(i);
            if( item.checked ) {
                if( count < item.selectedNumber ){
                    count = item.selectedNumber;
                }
            }
        }
        for(int j=1;j<=count;j++) {
            for (int i = 0; i<listDispItems.size() ;i++ ){
                MyListItem item = listDispItems.get(i);
                if( item.checked && item.selectedNumber == j){
                    ret.add(item.name);
                }
            }
        }
        return ret;
    }
    private String getSelectedItem() {
        List<String> ret = new ArrayList<String>();
        for(int i=0;i<listDispItems.size();i++){
            MyListItem item = listDispItems.get(i);
            if( item.checked ) {
                return item.name;
            }
        }
        return "";
    }
    private void printDebug() {
        StringBuilder sb = new StringBuilder();
        sb.append("Count:"+getSelectedItemCount()+"\n");
        sb.append("getSelectedItem:"+getSelectedItem()+"\n");
        sb.append("getSelectedItems:");
        List<String> data = getSelectedItems();
        for(int i=0;i<data.size();i++){
            String item = data.get(i);
            sb.append(item+",");
        }
        //textView.setText(sb.toString());
    }

    private void getDirInfo(String dirPath)
    {
        if(!dirPath.endsWith("/")) dirPath = dirPath+"/";
        File f = new File(dirPath);
        File[] files = f.listFiles();
        if( files == null ) return;

        itemFiles.clear();
        pathFiles.clear();

        if( !root.endsWith("/") ) root = root+"/";
        if( !root.equals(dirPath) ) {
            itemFiles.add("../");
            pathFiles.add(f.getParent());
        }

        for(int i=0; i < files.length; i++){
            File file = files[i];
            pathFiles.add(file.getPath());

            if(file.isDirectory())
                itemFiles.add(file.getName() + "/");
            else
                itemFiles.add(file.getName());
        }
    }

    private void itemClick(String name, String path) {
        File file = new File(path);
        if (file.isDirectory())
        {
            if(file.canRead()) {
                CurPath = path;
                setupAdapter();
            }else{
                new AlertDialog.Builder(this)
                        .setTitle("[" + file.getName() + "] folder can't be read!")
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                }).show();
            }
        }
        else{
            if(file.getName().endsWith(".node"))
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                node_name = file.getParent() + "/" + file.getName();
                song_name = file.getParent() + "/" + file.getName().substring(0, file.getName().length()-5) + ".mp3";
                builder.setTitle("[" + node_name + "]");
                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        File song = new File(song_name);
                        if(song.exists())
                        {
                            SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("node_name", node_name);
                            editor.putString("song_name", song_name);
                            editor.commit();
                            Find_to_Game();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "mp3파일이 없습니다.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("[" + file.getPath() + file.getName() + "]");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }

        }
    }

    @Override
    public void onBackPressed(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("[ 메인 화면으로 되돌아 가시겠습니까? ]");
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                end();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    void Find_to_Game()
    {
        Intent intent = new Intent(this, GameActivity.class);
        intent.setFlags(Intent. FLAG_ACTIVITY_SINGLE_TOP );
        startActivity(intent);
        finish();
    }

    void end()
    {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent. FLAG_ACTIVITY_SINGLE_TOP );
        startActivity(intent);
        finish();
    }
}