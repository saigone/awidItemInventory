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

import java.util.ArrayList;

public class DataRowCollection extends ArrayList<DataRow>{
    
    /**
     * DataRowCollection所屬的DataTable，唯讀
     */
    private DataTable Table;

    /**
     * DataRowCollection被建立時，一定要指定所屬的DataTable
     * @param table 
     */
    public DataRowCollection(DataTable table)
    {
     this.Table = table;
    
    }
    
    /**
     * 取得所屬的DataTable
     * @return DataTable
     */
    public DataTable getTable()
    {
     return this.Table;
    }
    
    
    
}
