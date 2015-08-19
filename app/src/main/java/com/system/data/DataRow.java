/*
 * Description
 *
 *   A brief description of the class/interface.
 *
 * History
 *
 *   yyyy-mm-dd Author        
 *              What has been changed.
 *
 * Copyright notice
 */
package com.system.data;

import java.util.LinkedHashMap;

//讓DataRow成為一個Map字典物件，String為欄位(不會重覆)當做Key，Object當做要存放的值
public class DataRow extends LinkedHashMap<String, Object> {

	/**
	 * 在getValue()和setValue()時候，程式碼須透過此成員的欄位名稱來找出Map字典裡的物件
	 */
	private DataColumnCollection columns;

	/**
	 * DataRow被建立時，必須指定所屬的DataTable
	 * 
	 * @param DataRow所屬的DataTable
	 */
	public DataRow(DataTable table) {
		this.table = table;
		this.columns = table.Columns;
	}

	/**
	 * 此資料列所屬的DataTable，唯讀
	 */
	private DataTable table;

	/**
	 * 取得DataRow所屬的DataTable
	 * 
	 * @return DataTable
	 */
	public DataTable getTable() {
		return this.table;
	}

	/**
	 * 設定該列該行的值
	 * 
	 * @param columnindex
	 *            行索引(從0算起)
	 * @param value
	 *            要設定的值
	 */
	public void setValue(int columnindex, Object value) {
		setValue(this.columns.get(columnindex), value);
	}

	/**
	 * 設定該列該行的值
	 * 
	 * @param columnName
	 *           行名稱
	 * @param value
	 *           要設定的值
	 */
	public void setValue(String columnName, Object value) {
		this.put(columnName.toLowerCase(), value);
	}

	/**
	 * 設定該列該行的值
	 * 
	 * @param column
	 *            DataColumn物件
	 * @param value
	 *            要設定的值
	 */
	private void setValue(DataColumn column, Object value) {
		if (column != null) {
			String lowerColumnName = column.ColumnName.toLowerCase();
			if (this.containsKey(lowerColumnName))
				this.remove(lowerColumnName);
			this.put(lowerColumnName, value);
		}
	}

	/**
	 * 取得該列該行的值
	 * 
	 * @param columnIndex
	 *            行索引(從0算起)
	 * @return Object
	 */
	public Object getValue(int columnIndex) {
		String columnName = this.columns.get(columnIndex).ColumnName
				.toLowerCase();// ��oKey
		return this.get(columnName);
	}

	/**
	 * 取得該列該行的值
	 * 
	 * @param columnName
	 *            行名稱
	 * @return Object
	 */
	public Object getValue(String columnName) {
		return this.get(columnName.toLowerCase());
	}

	/**
	 * 取得該列該行的值
	 * 
	 * @param column
	 *            DataColumn物件
	 * @return Object
	 */
	public Object getValue(DataColumn column) {
		return this.get(column.ColumnName.toLowerCase());// 利用欄名(Key)來取值
	}

}
