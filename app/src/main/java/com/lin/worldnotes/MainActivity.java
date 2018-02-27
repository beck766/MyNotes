package com.lin.worldnotes;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.lin.worldnotes.db.NotesDB;
import com.lin.worldnotes.menu.AboutActivity;
import com.lin.worldnotes.menu.HelpActivity;
import com.lin.worldnotes.view.SidingMenu;

public class MainActivity extends ListActivity implements OnClickListener {
    private SimpleCursorAdapter adapter = null;
    private NotesDB db;
    private SQLiteDatabase dbRead, dbWrite;
    private Button btnAddNotes;
    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_EDIT_NOTE = 2;
    private SidingMenu mLeftMenu;
    private TextView tvSetting, tvAbout, tvUpdate, tvHelp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
//		tvSetting = (TextView) findViewById(R.id.tvSetting);
        tvAbout = (TextView) findViewById(R.id.tvAbout);
        tvUpdate = (TextView) findViewById(R.id.tvUpdate);
        tvHelp = (TextView) findViewById(R.id.tvHelp);
//		tvSetting.setOnClickListener(this);
        tvAbout.setOnClickListener(this);
        tvUpdate.setOnClickListener(this);
        tvHelp.setOnClickListener(this);
        db = new NotesDB(this);
        dbRead = db.getReadableDatabase();
        dbWrite = db.getWritableDatabase();
        mLeftMenu = (SidingMenu) findViewById(R.id.id_menu);
        adapter = new SimpleCursorAdapter(this, R.layout.notes_list_cell, null,
                new String[]{NotesDB.COLUWM_NAME_NOTE_NAME,
                        NotesDB.COLUWM_NAME_NOTE_DATE}, new int[]{
                R.id.tvName, R.id.tvDate});
        setListAdapter(adapter);
        refreshListView();
        // 设置长按删除
        getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           final int position, long id) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("提醒")
                        .setMessage("你确定要删除么？")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        // TODO Auto-generated method stub
                                        Cursor c = adapter.getCursor();
                                        c.moveToPosition(position);
                                        int itemId = c.getInt(c
                                                .getColumnIndex(NotesDB.COLUMN_NAME_ID));
                                        dbWrite.delete(
                                                NotesDB.TABLE_NAME_NOTES,
                                                NotesDB.COLUMN_NAME_ID + "=?",
                                                new String[]{itemId + ""});
                                        refreshListView();
                                    }
                                }).show();

                return true;
            }
        });
        btnAddNotes = (Button) findViewById(R.id.btnAddNotes);
        // 点击添加日志跳转到编辑页面
        btnAddNotes.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this,
                        EditNotesActivity.class), REQUEST_CODE_ADD_NOTE);
            }
        });

    }

    public void toggleMenu(View view) {
        mLeftMenu.toggle();
    }

    // 接收点击保存事件
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_ADD_NOTE:
            case REQUEST_CODE_EDIT_NOTE:
                if (resultCode == Activity.RESULT_OK) {
                    refreshListView();
                }
                break;

            default:
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    // 点击日志某一列表跳转到编辑页面，并将标题和内容传过去
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Cursor c = adapter.getCursor();
        c.moveToPosition(position);
        Intent i = new Intent(MainActivity.this, EditNotesActivity.class);
        i.putExtra(EditNotesActivity.EXTRA_NOTE_ID,
                c.getInt(c.getColumnIndex(NotesDB.COLUMN_NAME_ID)));
        i.putExtra(EditNotesActivity.EXTRA_NOTE_NAME,
                c.getString(c.getColumnIndex(NotesDB.COLUWM_NAME_NOTE_NAME)));
        i.putExtra(EditNotesActivity.EXTRA_NOTE_CONTENT,
                c.getString(c.getColumnIndex(NotesDB.COLUWM_NAME_NOTE_CONTENT)));
        startActivityForResult(i, REQUEST_CODE_EDIT_NOTE);
        super.onListItemClick(l, v, position, id);
    }

    // 数据库查询，用于更新列表
    public void refreshListView() {
        adapter.changeCursor(dbRead.query(NotesDB.TABLE_NAME_NOTES, null, null,
                null, null, null, null));
    }

    @Override
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {
//		case R.id.tvSetting:
//			i = new Intent(this, SettingActivity.class);
//			startActivity(i);
//			break;
            case R.id.tvAbout:
                i = new Intent(this, AboutActivity.class);
                startActivity(i);
                break;
            case R.id.tvUpdate:
                Toast.makeText(MainActivity.this, "当前已是最新版本，无需更新", Toast.LENGTH_SHORT).show();
                break;
            case R.id.tvHelp:
                i = new Intent(this, HelpActivity.class);
                startActivity(i);
                break;
            default:
                break;
        }
    }
}
