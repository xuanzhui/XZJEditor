package com.xz.frame;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultEditorKit;

import com.xz.encrypt.EncryptException;

public class EditorFrame extends JFrame {
	private String md5password;

	private JTabbedPane area = new JTabbedPane();

	private java.util.Set<String> filesOpened = new HashSet<String>();

	private String fileOpenCache = System.getProperty("user.home")+"/.XZJEditor/filesOpened.dat";

	private JFileChooser dialog = new JFileChooser(System.getProperty("user.home"));

	private class SaveButtonTabComponent extends ButtonTabComponent{

		public SaveButtonTabComponent(JTabbedPane pane, String title) {
			super(pane, title);
		}

		@Override
		public int beforeCloseTabWhenWindowClosing() {
			TabEncryptTextArea tabEncryptTextArea = (TabEncryptTextArea) area.getComponentAt(area.indexOfTabComponent(this));
			String oriFilepath = tabEncryptTextArea.getFileName();
			int rep = beforeCloseTab();
			String filepath = tabEncryptTextArea.getFileName();

			if (!filepath.equals(oriFilepath)){
				filesOpened.remove(oriFilepath);
				filesOpened.add(filepath);
			}

			return rep;
		}

		@Override
		public int beforeCloseTabManually() {
			TabEncryptTextArea tabEncryptTextArea = (TabEncryptTextArea) area.getComponentAt(area.indexOfTabComponent(this));
			filesOpened.remove(tabEncryptTextArea.getFileName());
			return beforeCloseTab();
		}

		private int beforeCloseTab() {
			TabEncryptTextArea tabEncryptTextArea = (TabEncryptTextArea) area.getSelectedComponent();

			int rep = JOptionPane.NO_OPTION;
			if (tabEncryptTextArea.isContentChanged() || tabEncryptTextArea.isEncTypeChanged()){

				rep = JOptionPane.showConfirmDialog(EditorFrame.this, "save changes?");

				if (rep == JOptionPane.YES_OPTION) {
					try {
						saveFile(false);
					} catch (IOException ex) {
						Toolkit.getDefaultToolkit().beep();
						JOptionPane.showMessageDialog(EditorFrame.this, ex.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
					} catch (EncryptException ex) {
						Toolkit.getDefaultToolkit().beep();
						JOptionPane.showMessageDialog(EditorFrame.this, ex.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
					}
				}
			}

			return rep;
		}
	}

	private class NewAction extends AbstractAction {
		
		public NewAction(){
			super("New", new ImageIcon("icons/new.png"));
			this.putValue(SHORT_DESCRIPTION, "New Tab");
		}
		
		public void actionPerformed(ActionEvent e) {
			TabEncryptTextArea tabEncryptTextArea = new TabEncryptTextArea(area, md5password);
			String tabTitle = "Tab #" + (area.getTabCount() + 1);
			area.addTab(tabTitle, tabEncryptTextArea);

			area.setTabComponentAt(area.getTabCount() - 1, new SaveButtonTabComponent(area, tabTitle));
			area.setSelectedIndex(area.getTabCount() - 1);

			tabEncryptTextArea.getjTextArea().requestFocus();
		}
	}
	
	Action New = new NewAction();

	private class OpenAction extends AbstractAction {
		public OpenAction(){
			super("Open", new ImageIcon("icons/open.gif"));
			this.putValue(SHORT_DESCRIPTION, "Open File");
		}

		public void actionPerformed(ActionEvent e) {
			if (dialog.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				String filepath = dialog.getSelectedFile().getAbsolutePath();

				boolean found = false;
				if (filesOpened.contains(filepath)) {
					for (int i = 0; i < area.getTabCount() ; i++){
						TabEncryptTextArea tmp = (TabEncryptTextArea)area.getComponentAt(i);
						if (tmp.getFileName().equals(filepath)){
							area.setSelectedIndex(i);
							found = true;
							break;
						}
					}
				}

				if (found)
					return;
				else
					filesOpened.remove(filepath);

				try {
					TabEncryptTextArea tabEncryptTextArea=addTabWithFile(filepath);
					tabEncryptTextArea.getjTextArea().requestFocus();
				} catch (IOException ex) {
					Toolkit.getDefaultToolkit().beep();
					JOptionPane.showMessageDialog(EditorFrame.this, ex.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
				} catch (EncryptException ex) {
					Toolkit.getDefaultToolkit().beep();
					JOptionPane.showMessageDialog(EditorFrame.this, ex.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	Action Open = new OpenAction();

	Action Save = new AbstractAction("Save", new ImageIcon("icons/save.gif")) {
		public void actionPerformed(ActionEvent e) {
			TabEncryptTextArea tabEncryptTextArea = (TabEncryptTextArea) area.getSelectedComponent();
			String oriFilepath = tabEncryptTextArea.getFileName();
			try {
				String filepath = saveFile(true);

				if (!oriFilepath.equals(filepath)) {
					filesOpened.remove(oriFilepath);
					filesOpened.add(filepath);
				}
			} catch(IOException ex) {
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(EditorFrame.this, ex.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
			} catch (EncryptException ex) {
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(EditorFrame.this, ex.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
			}
		}
	};
	
	Action SaveAs = new AbstractAction("Save as...") {
		
		public void actionPerformed(ActionEvent e) {
			try {
				saveFileAs(true);
			} catch(IOException ex) {
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(EditorFrame.this, ex.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
			} catch (EncryptException ex) {
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(EditorFrame.this, ex.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
			}
		}
		
	};
	
	Action Quit = new AbstractAction("Quit") {
		public void actionPerformed(ActionEvent e) {
			saveBeforeCloseWindow();
		}
	};

	Action findReplace = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			((TabEncryptTextArea) area.getSelectedComponent()).toggleSearchReplaceTool();
		}
	};

	public EditorFrame(String md5password) {
		this.md5password = md5password;

//		TabEncryptTextArea tabEncryptTextArea = new TabEncryptTextArea(area);
//		area.addTab("Tab #1", tabEncryptTextArea);
//		area.setTabComponentAt(0, new SaveButtonTabComponent(area, "Tab #1"));

		add(area, BorderLayout.CENTER);

		area.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (area.getTabCount() > 0)
					setTitle(((TabEncryptTextArea)area.getSelectedComponent()).getFileName());
			}
		});
		
		JMenuBar JMB = new JMenuBar();
		setJMenuBar(JMB);
		JMenu file = new JMenu("File");
		JMenu edit = new JMenu("Edit");
		JMB.add(file); JMB.add(edit);

		file.add(New);file.add(Open);
		Save.putValue(AbstractAction.SHORT_DESCRIPTION, "Save File");
		file.add(Save);
		file.add(Quit);file.add(SaveAs);
		file.addSeparator();
		
		for(int i=0; i<4; i++)
			file.getItem(i).setIcon(null);

		/*
		ActionMap m = area.getActionMap();
		Action Cut = m.get(DefaultEditorKit.cutAction);
		Action Copy = m.get(DefaultEditorKit.copyAction);
		Action Paste = m.get(DefaultEditorKit.pasteAction);
		*/

		Action Cut = new DefaultEditorKit.CutAction();
		Cut.putValue(AbstractAction.SHORT_DESCRIPTION, "Cut");
		Action Copy = new DefaultEditorKit.CopyAction();
		Copy.putValue(AbstractAction.SHORT_DESCRIPTION, "Copy");
		Action Paste = new DefaultEditorKit.PasteAction();
		Paste.putValue(AbstractAction.SHORT_DESCRIPTION, "Paste");

		edit.add(Cut);edit.add(Copy);edit.add(Paste);
		//findReplace.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control F"));
		edit.add(findReplace);

		edit.getItem(0).setText("Cut");
		edit.getItem(1).setText("Copy");
		edit.getItem(2).setText("Paste");
		edit.getItem(3).setText("Find & Replace");
		
		JToolBar tool = new JToolBar();
		add(tool, BorderLayout.NORTH);

		tool.add(New);
		tool.add(Open);
		tool.add(Save);
		tool.addSeparator();
		
		JButton cut = tool.add(Cut), cop = tool.add(Copy),pas = tool.add(Paste);

		cut.setText(null);
		cut.setIcon(new ImageIcon("icons/cut.gif"));
		cop.setText(null);
		cop.setIcon(new ImageIcon("icons/copy.gif"));
		pas.setText(null);
		pas.setIcon(new ImageIcon("icons/paste.gif"));

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				saveBeforeCloseWindow();
			}
		});

		//stop window from closing
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		//setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();

		setTitle("Untitled");
		
		Dimension screen= Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation((int) (screen.getWidth() - this.getWidth()) / 2, (int) (screen.getHeight() - this.getHeight()) / 2);

		this.setExtendedState(MAXIMIZED_BOTH);
		setVisible(true);

		//show UI then focus
		//tabEncryptTextArea.getjTextArea().requestFocus();

		loadFileOpenedLastTime();
	}

	private void loadFileOpenedLastTime() {
		String files = null;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileOpenCache),"UTF-8"));
			files = br.readLine();
			br.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (files != null){
			for (String str:files.split(",")){
				if (Files.exists(Paths.get(str))){
					try {
						addTabWithFile(str);
					} catch (IOException ex) {
						Toolkit.getDefaultToolkit().beep();
						JOptionPane.showMessageDialog(EditorFrame.this, ex.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
					} catch (EncryptException ex) {
						Toolkit.getDefaultToolkit().beep();
						JOptionPane.showMessageDialog(EditorFrame.this, ex.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
					}
				}
			}

			((TabEncryptTextArea)area.getComponentAt(area.getTabCount()-1)).getjTextArea().requestFocus();
		}
	}

	private void saveBeforeCloseWindow(){
		//everytime a tab is closed, tab total count will decrease 1
		while (area.getTabCount() > 0) {
			area.setSelectedIndex(0);
			ButtonTabComponent buttonTabComponent = (ButtonTabComponent) area.getTabComponentAt(0);
			if (!buttonTabComponent.closeTabWhenWindowClosing())
				break;
		}

		if (area.getTabCount() == 0) {
			if (filesOpened.size() != 0) {
				String filestr = "";
				for (String str : filesOpened)
					filestr += str + ',';

				filestr = filestr.substring(0, filestr.length() - 1);

				try {
					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
							new FileOutputStream(fileOpenCache), "UTF-8"));
					bw.write(filestr);
					bw.flush();
					bw.close();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			System.exit(0);
		}
	}

	private TabEncryptTextArea addTabWithFile(String fileName) throws IOException, EncryptException {
		String fileBaseName = new File(fileName).getName();

		TabEncryptTextArea tabEncryptTextArea = new TabEncryptTextArea(area, md5password);

		area.addTab(fileBaseName, tabEncryptTextArea);
		area.setTabComponentAt(area.getTabCount() - 1, new SaveButtonTabComponent(area, fileBaseName));
		area.setSelectedIndex(area.getTabCount() - 1);

		filesOpened.add(fileName);

		tabEncryptTextArea.loadContent(fileName);

		setTitle(fileName);

		return tabEncryptTextArea;
	}

	//echoTitle false means the tab is closing
	private String saveFile(boolean echoTitle) throws IOException, EncryptException {
		TabEncryptTextArea tabEncryptTextArea = (TabEncryptTextArea) area.getSelectedComponent();
		return tabEncryptTextArea.saveContent(echoTitle);
	}

	private void saveFileAs(boolean echoTitle) throws IOException, EncryptException {
		if(dialog.showSaveDialog(null)==JFileChooser.APPROVE_OPTION) {
			TabEncryptTextArea tabEncryptTextArea = (TabEncryptTextArea) area.getSelectedComponent();
			filesOpened.remove(tabEncryptTextArea.getFileName());

			tabEncryptTextArea.setFileName(dialog.getSelectedFile().getAbsolutePath());
			filesOpened.add(tabEncryptTextArea.saveContent(echoTitle));

			setTitle(tabEncryptTextArea.getFileName());
		}
	}
}
