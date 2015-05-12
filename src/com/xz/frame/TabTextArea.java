package com.xz.frame;

import com.xz.encrypt.Encrypt;
import com.xz.encrypt.EncryptException;
import com.xz.io.DecryptInputDetachEncInfo;
import com.xz.io.EncryptOutputAttachEncInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;

/**
 * Created by xuanzhui on 15/5/11.
 */
public class TabTextArea extends JTextArea {

    private String fileName = "Untitled";

    private boolean contentChanged = false;

    private JTabbedPane tabbedPane;

    public TabTextArea(final JTabbedPane tabbedPane){
        super(20,120);

        this.tabbedPane = tabbedPane;

        this.setFont(new Font("Monospaced", Font.PLAIN, 12));

        this.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (!contentChanged) {
                    contentChanged = true;

                    tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), "* "+fileName);
                }
            }
        });
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isContentChanged() {
        return contentChanged;
    }

    public void setContentChanged(boolean contentChanged) {
        this.contentChanged = contentChanged;
    }

    public void saveContent(String encType, int offset, String md5password) throws IOException, EncryptException {
        if (fileName.equals("Untitled"))
            throw new IOException("set file name firstly before save!");
        if (!contentChanged)
            return;

        if (encType == null){
            FileWriter w = new FileWriter(fileName);
            this.write(w);
            w.close();
        } else {
            EncryptOutputAttachEncInfo eoei = new EncryptOutputAttachEncInfo(fileName, md5password, encType, offset);
            fileName = eoei.encryptFromString(this.getText(), "UTF-8");
        }


        tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), new File(fileName).getName());
    }

    public void loadContent(String fileName, String md5password) throws IOException, EncryptException {
        if (fileName.endsWith(Encrypt.ENC_FILE_SUFF)){
            DecryptInputDetachEncInfo dide =
                    new DecryptInputDetachEncInfo(fileName, md5password);
            this.setFileName(fileName);
            this.setText(dide.decryptToString());
        }
        else {
            FileReader r = new FileReader(fileName);
            this.setFileName(fileName);
            this.read(r, null);
            r.close();
        }
        tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), new File(fileName).getName());
    }
}
