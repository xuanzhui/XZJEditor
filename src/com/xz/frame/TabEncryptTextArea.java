package com.xz.frame;

import com.xz.encrypt.Encrypt;
import com.xz.encrypt.EncryptException;
import com.xz.io.DecryptInputDetachEncInfo;
import com.xz.io.EncryptOutputAttachEncInfo;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;

/**
 * Created by xuanzhui on 15/5/11.
 */
public class TabEncryptTextArea extends JPanel {

    private String fileName = "Untitled";
    private String fileBaseName = "Untitled";

    private boolean contentChanged = false;
    private boolean encTypeChanged = false;

    private JTabbedPane tabbedPane;
    private JTextArea jTextArea;
    private JCheckBox jCheckBox = new JCheckBox();
    private JComboBox<String> encType = new JComboBox<String>();
    private JComboBox<Integer> offset = new JComboBox<Integer>();

    public TabEncryptTextArea(final JTabbedPane tabbedPane){
        super();

        this.tabbedPane = tabbedPane;

        this.setLayout(new BorderLayout());

        jTextArea = new JTextArea(20,120);

        this.add(new JScrollPane(jTextArea), BorderLayout.CENTER);

        JToolBar tool = new JToolBar();
        this.add(tool, BorderLayout.NORTH);

        tool.addSeparator();
        tool.add(new JLabel("    Encrypt When Save"));
        tool.add(jCheckBox);

        jCheckBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                encTypeChanged = true;
            }
        });

        tool.add(new JLabel("    Encrypt Type"));
        encType.addItem("BitReverseEncrypt");
        encType.addItem("BitComplementEncrypt");
        encType.setMaximumSize(new Dimension(200, 30));
        encType.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED)
                    encTypeChanged = true;
            }
        });
        tool.add(encType);

        JLabel offsetLabel = new JLabel("    Offset");
        offsetLabel.setToolTipText("offset is only required for BitComplementEncrypt");
        tool.add(offsetLabel);
        for (int i=1; i<33; i++)
            offset.addItem(i);
        offset.setMaximumSize(new Dimension(100, 30));
        offset.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED)
                    encTypeChanged = true;
            }
        });
        tool.add(offset);

        jTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        jTextArea.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (!contentChanged) {
                    contentChanged = true;

                    //tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), "* "+fileName);
                    ((ButtonTabComponent) tabbedPane.getTabComponentAt(tabbedPane.getSelectedIndex())).
                            changeTabLabelTitle("* " + fileBaseName);
                }
            }
        });

    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
        fileBaseName = new File(fileName).getName();
    }

    public boolean isContentChanged() {
        return contentChanged;
    }

    public void setContentChanged(boolean contentChanged) {
        this.contentChanged = contentChanged;
    }

    public boolean isEncTypeChanged() {
        return this.encTypeChanged;
    }

    public JTextArea getjTextArea(){
        return this.jTextArea;
    }

    public void saveContent(String md5password, boolean echoTitle) throws IOException, EncryptException {
        if (fileName.equals("Untitled"))
            throw new IOException("set file name firstly before save!");
        if (!contentChanged && !encTypeChanged)
            return;

        String encSType = null;
        Integer ioffset = 0;

        if (jCheckBox.isSelected()){
            encSType = (String) encType.getSelectedItem();

            if (encSType != null){
                if (encSType.equals("BitReverseEncrypt")){
                    encSType = Encrypt.ENC_BR;
                } else if (encSType.equals("BitComplementEncrypt")){
                    encSType = Encrypt.ENC_BC;
                    ioffset = (Integer) offset.getSelectedItem();
                }
            }
        }

        if (encSType == null){
            FileWriter w = new FileWriter(fileName);
            jTextArea.write(w);
            w.close();
        } else {
            EncryptOutputAttachEncInfo eoei = new EncryptOutputAttachEncInfo(fileName, md5password, encSType, ioffset);
            fileName = eoei.encryptFromString(jTextArea.getText(), "UTF-8");
        }

        contentChanged = false;

        if (echoTitle)
            ((ButtonTabComponent)tabbedPane.getTabComponentAt(tabbedPane.getSelectedIndex())).
                    changeTabLabelTitle("  " + fileBaseName);
    }

    public void loadContent(String fileName, String md5password) throws IOException, EncryptException {
        if (fileName.endsWith(Encrypt.ENC_FILE_SUFF)){
            DecryptInputDetachEncInfo dide =
                    new DecryptInputDetachEncInfo(fileName, md5password);
            this.setFileName(fileName);
            jTextArea.setText(dide.decryptToString());

            if (dide.getEncType().equals(Encrypt.ENC_BR)) {
                jCheckBox.setSelected(true);
                encType.setSelectedItem("BitReverseEncrypt");
            } else if (dide.getEncType().equals(Encrypt.ENC_BC)){
                jCheckBox.setSelected(true);
                encType.setSelectedItem("BitComplementEncrypt");
                offset.setSelectedItem(dide.getOffset());
            }
        }
        else {
            FileReader r = new FileReader(fileName);
            this.setFileName(fileName);
            jTextArea.read(r, null);
            r.close();
        }
    }
}
