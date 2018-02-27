package com.lin.worldnotes;

import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lin.worldnotes.db.NotesDB;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EditNotesActivity extends ListActivity implements OnClickListener {
    private int noteId = -1;//id=-1时表示此时进行的是添加日志，否则进行的是修改日志
    public static final String EXTRA_NOTE_ID = "noteId";
    public static final String EXTRA_NOTE_NAME = "noteName";
    public static final String EXTRA_NOTE_CONTENT = "noteContent";
    private EditText etName, etContent;
    private Button btnSave, btnCancel, btnAddPhoto, btnAddVideo;
    private MediaAdapter adapter;
    private NotesDB db;
    private SQLiteDatabase dbRead, dbWrite;
    private String currentPath = null;
    private Intent i;
    private File file;
    public static final int REQUEST_CODE_GET_PHOTO = 1;
    public static final int REQUEST_CODE_GET_VIDEO = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.edit_notes);
        adapter = new MediaAdapter(this);
        setListAdapter(adapter);
        db = new NotesDB(this);
        dbRead = db.getReadableDatabase();
        dbWrite = db.getWritableDatabase();
        etName = (EditText) findViewById(R.id.etName);
        etContent = (EditText) findViewById(R.id.etContent);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(this);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(this);
        btnAddPhoto = (Button) findViewById(R.id.btnAddPhoto);
        btnAddPhoto.setOnClickListener(this);
        btnAddVideo = (Button) findViewById(R.id.btnAddVideo);
        btnAddVideo.setOnClickListener(this);
//修改某条日志
        noteId = getIntent().getIntExtra(EXTRA_NOTE_ID, -1);
        if (noteId > -1) {
            etName.setText(getIntent().getStringExtra(EXTRA_NOTE_NAME));
            etContent.setText(getIntent().getStringExtra(EXTRA_NOTE_CONTENT));

            Cursor c = dbRead.query(NotesDB.TABLE_NAME_MEDIA, null,
                    NotesDB.COLUWM_NAME_MEDIA_OENER_NOTE_ID + "=?",
                    new String[]{noteId + ""}, null, null, null);
            while (c.moveToNext()) {
                adapter.add(new MediaListCellData(c.getString(c
                        .getColumnIndex(NotesDB.COLUWM_NAME_MEDIA_PATH)), c
                        .getInt(c.getColumnIndex(NotesDB.COLUMN_NAME_ID))));

            }
            adapter.notifyDataSetChanged();
        }

    }

    //点击媒体列表 跳转到媒体预览
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent i;
        MediaListCellData data = adapter.getItem(position);
        switch (data.type) {
            case MediaType.PHOTO:
                i = new Intent(this, PhotoViewActivity.class);
                i.putExtra(PhotoViewActivity.EXTRA_PATH, data.path);
                startActivity(i);
                break;
            case MediaType.VIDEO:
                i = new Intent(this, VideoViewActivity.class);
                i.putExtra(VideoViewActivity.EXTRA_PATH, data.path);
                startActivity(i);
                break;
            default:
                break;
        }

        super.onListItemClick(l, v, position, id);
    }

    @Override
    protected void onDestroy() {
        dbRead.close();
        dbWrite.close();
        super.onDestroy();
    }

    //将媒体内容显示到媒体列表
    static class MediaAdapter extends BaseAdapter {
        private Context context;
        private List<MediaListCellData> list = new ArrayList<EditNotesActivity.MediaListCellData>();

        public MediaAdapter(Context context) {
            this.context = context;
        }

        public void add(MediaListCellData data) {
            list.add(data);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public MediaListCellData getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(
                        R.layout.media_list_cell, null);
            }
            MediaListCellData data = getItem(position);
            ImageView ivIcon = (ImageView) convertView
                    .findViewById(R.id.ivIcon);
            TextView tvPath = (TextView) convertView.findViewById(R.id.tvPath);
            ivIcon.setImageResource(data.iconId);
            tvPath.setText(data.path);
            return convertView;
        }

    }

    static class MediaType {
        static final int PHOTO = 1;
        static final int VIDEO = 2;
    }

    static class MediaListCellData {
        int type = 0;
        int id = -1;
        String path = "";
        int iconId = R.drawable.ic_launcher;

        //设置媒体图标
        public MediaListCellData(String path) {
            this.path = path;
            if (path.endsWith(".jpg")) {
                iconId = R.drawable.photo;
                type = MediaType.PHOTO;
            } else if (path.endsWith(".mp4")) {
                iconId = R.drawable.video;
                type = MediaType.VIDEO;
            }
        }

        public MediaListCellData(String path, int id) {
            this(path);
            this.id = id;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSave:
                saveMedia(saveNote());
                setResult(RESULT_OK);
                finish();

                break;
            case R.id.btnCancel:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.btnAddPhoto:
                i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                file = new File(getMediaDir(), System.currentTimeMillis() + ".jpg");
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                currentPath = file.getAbsolutePath();
                i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                startActivityForResult(i, REQUEST_CODE_GET_PHOTO);
                break;
            case R.id.btnAddVideo:
                i = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                file = new File(getMediaDir(), System.currentTimeMillis() + ".mp4");
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                currentPath = file.getAbsolutePath();
                i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                startActivityForResult(i, REQUEST_CODE_GET_VIDEO);
                break;

            default:
                break;
        }

    }

    //接收拍照录像完成的数据
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_GET_PHOTO:
            case REQUEST_CODE_GET_VIDEO:
                if (resultCode == RESULT_OK) {
                    adapter.add(new MediaListCellData(currentPath));
                    adapter.notifyDataSetChanged();
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //创建保存媒体文件夹
    public File getMediaDir() {
        File dir = new File(Environment.getExternalStorageDirectory(),
                "NotesMedia");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public void saveMedia(int noteId) {
        MediaListCellData data;
        for (int i = 0; i < adapter.getCount(); i++) {
            data = adapter.getItem(i);
            if (data.id <= -1) {
                ContentValues cv = new ContentValues();
                cv.put(NotesDB.COLUWM_NAME_MEDIA_PATH, data.path);
                cv.put(NotesDB.COLUWM_NAME_MEDIA_OENER_NOTE_ID, noteId);
                dbWrite.insert(NotesDB.TABLE_NAME_MEDIA, null, cv);
            }
        }
    }

    public int saveNote() {
        ContentValues cv = new ContentValues();
        cv.put(NotesDB.COLUWM_NAME_NOTE_NAME, etName.getText().toString()
                .trim());
        cv.put(NotesDB.COLUWM_NAME_NOTE_CONTENT, etContent.getText().toString()
                .trim());
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        cv.put(NotesDB.COLUWM_NAME_NOTE_DATE, f.format(new Date()));
        if (noteId > -1) {
            dbWrite.update(NotesDB.TABLE_NAME_NOTES, cv, NotesDB.COLUMN_NAME_ID
                    + "=?", new String[]{noteId + ""});
            return noteId;
        } else {
            return (int) dbWrite.insert(NotesDB.TABLE_NAME_NOTES, null, cv);
        }

    }
}
