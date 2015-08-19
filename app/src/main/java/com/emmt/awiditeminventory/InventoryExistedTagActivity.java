package com.emmt.awiditeminventory;


import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.emmt.database.NewListDataSQL;
import com.emmt.plus.device.HandyDeviceHB;
import com.emmt.plus.device.ReaderErrorRespondException;
import com.emmt.plus.device.ReaderSleepingException;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TableRow.LayoutParams;
import android.widget.Toast;

public class InventoryExistedTagActivity extends Activity {
	private TableLayout mInventoryTable;
	private TableLayout mErrorInventoryTable;
	private TextView mTotalLoadedTagCount;
	private TextView mCheckedTagCount;
	private TextView mErrorTagCount;
	
	private SQLiteDatabase db; 
	private String db_name = "warehouse.db"; 
	private NewListDataSQL helper = new NewListDataSQL(InventoryExistedTagActivity.this, db_name); // ���U���W

	private List<String> mInventoryList = new ArrayList<String>();
	private List<String> mInventoryStateList = new ArrayList<String>();
	private List<String> mInventoryErrorTagList = new ArrayList<String>();
	private int mRowCount = 0;
	private final Handler h = new Handler();
	
	private Timer mUpdateTagTimer;
	
	private HandyDeviceHB mHBDevice;
    private Button mBtnInventory;
    private long mLastClickTime = 0;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		keepWindowScreenOn();
		setContentView(R.layout.activity_readtags);
		initViewComponent();
		initReader();
		initialDatabase();
		loadInventoryListFromDB();
		enableReaderTrigger();
	}

	private void keepWindowScreenOn() {
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
	
	private void initViewComponent() {
        mBtnInventory = (Button) findViewById(R.id.btn_inventory_exist);
		mInventoryTable = (TableLayout) this.findViewById(R.id.inventoryCheckedTagTable);
		mErrorInventoryTable = (TableLayout) this.findViewById(R.id.inventoryErrorTagTable);
		mTotalLoadedTagCount =  (TextView) this.findViewById(R.id.editTextTotalTagCount);
		mCheckedTagCount = (TextView) this.findViewById(R.id.editTextcheckedTagCount);
		mErrorTagCount = (TextView) this.findViewById(R.id.editTextErrorTagCount);
	}

	private void initReader() {
		GlobalVariable globalVariable = (GlobalVariable) getApplicationContext();
		mHBDevice = globalVariable.getHandDeviceHB();
		mHBDevice.switchToMultiTagMode(true);
        mHBDevice.enableHandyTrigger(false);
		try {
			mHBDevice.setPowerLevel(0);
		} catch (ReaderErrorRespondException e) {
			e.printStackTrace();
		} catch (ReaderSleepingException e) {
			e.printStackTrace();
		}
	}
	
	private void initialDatabase() {

		db = helper.getReadableDatabase();
	}

	private void loadInventoryListFromDB() {
		mInventoryList = queryEPC();
		mTotalLoadedTagCount.setText("" + mInventoryList.size());
		for(String tag : mInventoryList) {
			addNewRowToCheckedTable(tag);
			mInventoryStateList.add("N");
		}
	}
	
	private List<String> queryEPC() {
		List<String> list = new ArrayList<String>();
		Cursor c = db.rawQuery(
				"select epc from WarehouseInventoryTable ORDER BY _ID ASC ", null);

		if (c == null || c.getCount() == 0) {
			return list;
		}
		else {
			c.moveToFirst();
			do {
				list.add(c.getString(0));
			} while (c.moveToNext());
		}

		return list;
	}
	
	private void addNewRowToCheckedTable(String tag) {
		final TableRow row = new TableRow(InventoryExistedTagActivity.this);
		if (mRowCount % 2 == 0)
			row.setBackgroundColor(Color.GRAY);
		mRowCount++;

		TextView tagText = buildTextiew();
		tagText.setText(tag);

		TextView tagStateText = buildTextiew();
		tagStateText.setText("N");
		tagStateText.setTextColor(Color.RED);


		row.addView(tagText);
		row.addView(tagStateText);

		h.post(new Runnable() {
			@Override
			public void run() {
				mInventoryTable.addView(row, new TableLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			}
		});
	}
	
	private TextView buildTextiew() {
		TextView view = new TextView(InventoryExistedTagActivity.this);
		view.setPadding(10, 0, 0, 0);
		view.setTextSize(15);
		view.setTextColor(InventoryExistedTagActivity.this.getResources().getColor(R.color.blue));

		return view;
	}
	
	private void enableReaderTrigger() {
		mUpdateTagTimer = new Timer();
		mUpdateTagTimer.schedule(new AutoReceiveTagTimer(), 1000, 200);
	}

    public void onclick(View v) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        switch(v.getId()) {
            case R.id.btn_inventory_exist:
                processInventory();
                break;
        }
    }

    private void processInventory() {
        if (!mHBDevice.isRunningInventory()) {
            mHBDevice.startInventory();
            mBtnInventory.setText(getString(R.string.stop));
        }
        else {
            mHBDevice.stopAllAction();
            mBtnInventory.setText(getString(R.string.inventory));
        }
    }

    public class AutoReceiveTagTimer extends TimerTask {

		@Override
		public void run() {
			try {
				String[] tags = mHBDevice.getInventoryResult();
				for(String tag : tags) {
					if(mInventoryList.contains(tag)){
						processCheckedTag(tag);
					}
					else {
						processErrorTag(tag);
					}
				}
				
				calculateCheckedTagCount();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

		private void processCheckedTag(String tag) {
			int index = mInventoryList.indexOf(tag);
			if(mInventoryStateList.get(index).equals("N")) {
				updateInventoryTable(index);
				mInventoryStateList.set(index, "Y");
			}
		}

		private void updateInventoryTable(int index) {
			TableRow row = (TableRow) mInventoryTable.getChildAt(index + 1);
			final TextView state = (TextView) row.getChildAt(1);
			h.post(new Runnable() {
				@Override
				public void run() {
					state.setText("Y");
					state.setTextColor(Color.GREEN);
				}
			});
		}

		private void processErrorTag(String tag) {
			if(!(mInventoryErrorTagList.contains(tag))) {
				mInventoryErrorTagList.add(tag);
				addNewRowToErrorTable(tag);
			}
		}

		private void addNewRowToErrorTable(String tag) {
			final TableRow row = new TableRow(InventoryExistedTagActivity.this);
			if (mRowCount % 2 == 0)
				row.setBackgroundColor(Color.GRAY);
			mRowCount++;
			
			TextView tagText = buildTextiew();
			tagText.setText(tag);
			
			row.addView(tagText);
			
			h.post(new Runnable() {
				@Override
				public void run() {
					mErrorInventoryTable.addView(row, new TableLayout.LayoutParams(
							LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
					mErrorTagCount.setText("" + mInventoryErrorTagList.size());
				}
			});
		}
		
		private void calculateCheckedTagCount() {
			int count = 0;
			for(String state : mInventoryStateList) {
				if(state.equals("Y"))
					count++;
			}
			
			final int total = count;
			
			h.post(new Runnable() {
				@Override
				public void run() {
					mCheckedTagCount.setText("" + total);
				}
			});
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mUpdateTagTimer.cancel();
		mHBDevice.stopAllAction();
		//mHBDevice.stopInventory();
		mHBDevice.enableHandyTrigger(false);

		db.close();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if(mHBDevice.isRunningInventory() == true) {
				Toast.makeText(this, getString(R.string.close_inventory_status), Toast.LENGTH_SHORT).show();
				return true;
			}
			else {
				finish();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

}
