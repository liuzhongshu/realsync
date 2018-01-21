package com.cloudtopo.tools;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;


public class RealSyncApp extends JFrame {
	
	private static final String CONFIG_FILE = "realsync.json";
	private static final String KEY_SYNC_DIR = "syncDirs";
	
	private Watcher watcher;
	private JTable tblSyncDir;
	private SyncDirModel syncModel;
	private List<SyncDir> syncDirs = new ArrayList<SyncDir>();
	private JButton btnAction;
	private boolean syncStarted;
	
	public RealSyncApp() throws IOException {

		
		setTitle("RealSync 1.0");
		
		loadSyncDir();
		syncModel = new SyncDirModel(syncDirs);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.NORTH);
		
		tblSyncDir = new JTable(syncModel);
		tblSyncDir.setBackground(Color.WHITE);
		tblSyncDir.setPreferredScrollableViewportSize(tblSyncDir.getPreferredSize());
		scrollPane.setViewportView(tblSyncDir);
		
		JTextArea txtrSyncLog = new JTextArea();
		txtrSyncLog.setFont(new Font("Monospaced", Font.PLAIN, 11));
		txtrSyncLog.setText("Sync log:");
		txtrSyncLog.setBackground(new Color(245, 245, 245));
		getContentPane().add(txtrSyncLog, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);
		
		JButton btnNew = new JButton("Add");
		btnNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				syncDirs.add(new SyncDir());
				syncModel.fireTableDataChanged();
				tblSyncDir.setPreferredScrollableViewportSize(tblSyncDir.getPreferredSize());
				revalidate();
				repaint();
			}
		});
		panel.add(btnNew);
		
		JButton btnDelete = new JButton("Remove");
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedRow = tblSyncDir.getSelectedRow();
				if (selectedRow >= 0 && selectedRow < syncDirs.size()) {
					syncDirs.remove(selectedRow);
					syncModel.fireTableDataChanged();
					tblSyncDir.setPreferredScrollableViewportSize(tblSyncDir.getPreferredSize());
					revalidate();
					repaint();
				}
			}
		});
		panel.add(btnDelete);
		
		btnAction = new JButton("Start");
		btnAction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (syncStarted) {
					watcher.stop();
					btnAction.setText("Start");
				}
				else {
					for (SyncDir dir : syncDirs) {
						if (dir.enable)
							watcher.register(dir);
					}
					btnAction.setText("Stop");
				}
				syncStarted = !syncStarted;
			}
		});
		panel.add(btnAction);

		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowClosing(WindowEvent we)
		    {
		    	saveSyncDir();
	            System.exit(0);
		    }
		});
		
		
		watcher = new Watcher(txtrSyncLog);
		syncStarted = false;
	}

	private void loadSyncDir() {
		if (new File(CONFIG_FILE).isFile()) {			
			try {
				String buf =  new String(Files.readAllBytes(Paths.get(CONFIG_FILE)), "utf-8");			
				Config cfg = new Gson().fromJson(buf, new TypeToken<Config>(){}.getType());
				if(cfg.syncDirs != null) {
					syncDirs = cfg.syncDirs;
				}
			}
			catch(Exception e){}
		}			
	}

	private void saveSyncDir() {
					
		try {
			Config cfg = new Config();
			cfg.syncDirs = syncDirs;
			String cfgJson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(cfg);
			FileUtils.write(new File(CONFIG_FILE), cfgJson, "utf-8");
		}
		catch(Exception e){}
					
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());					
					RealSyncApp frame = new RealSyncApp();
					frame.pack();
					frame.setVisible(true);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
					System.exit(1);
				}
			}
		});         

	}

}
