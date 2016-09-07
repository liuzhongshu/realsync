package com.cloudtopo.tools;

import java.util.List;

import javax.swing.table.AbstractTableModel;


public class SyncDirModel extends AbstractTableModel {

	private List<SyncDir> models;
    
    private final String[] columnNames = new String[] {"enable", "source", "target", "sync delete", "exclude"};
    private final Class[] columnClass = new Class[] {Boolean.class, String.class, String.class, Boolean.class, String.class};
    
    public SyncDirModel(List<SyncDir> models) {
    	this.models = models;
    }
    
	@Override
	public int getColumnCount() {
		return columnClass.length;
	}
	
	@Override
	public String getColumnName(int col)
	{
		if (col < columnClass.length)
			return columnNames[col];
		else
			return null;
	}

	public Class getColumnClass(int col) {
		if (col < columnClass.length)
			return columnClass[col];
		else
			return null;
	}
	
	@Override
	public int getRowCount() {
		return models.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		SyncDir model = models.get(row);
        if(0 == col) {
            return model.enable;
        }
        else if(1 == col) {
            return model.srcDir;
        }
        else if(2 == col) {
            return model.tgtDir;
        }
        else if(3 == col ) {
            return model.syncDelete;
        }
        else if(4 == col ) {
            return model.excludePattern;
        }
        return null;	
	}

	@Override
    public void setValueAt(Object value, int row, int col)
    {
        SyncDir model = models.get(row);
        if(0 == col) {
        	model.enable = GetterUtil.get(value, true);        	
        }
        else if(1 == col) {
        	model.srcDir = GetterUtil.getString(value, "");        	
        }
        else if(2 == col) {
        	model.tgtDir = GetterUtil.getString(value, "");
        }
        else if(3 == col) {
        	model.syncDelete = GetterUtil.get(value, true);
        }
        else if (4 == col) {
        	model.excludePattern = GetterUtil.getString(value, "");
        }
        	
    }
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}
	
}
